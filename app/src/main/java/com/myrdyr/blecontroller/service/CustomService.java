package com.myrdyr.blecontroller.service;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import com.myrdyr.blecontroller.GattHandler;

import java.util.UUID;

/**
 * Created by myrdyr on 27.02.14.
 */
public abstract class CustomService<T> {
    private final static String TAG = CustomService.class.getSimpleName();

    private static String CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    private T data;

    protected CustomService() {
    }

    public abstract String getName();

    public String getCharacteristicName(String uuid) {
        if (getDataUUID().equals(uuid))
            return getName() + " Data";
        else if (getConfigUUID().equals(uuid))
            return getName() + " Config";
        return "Unknown";
    }

    public abstract String getServiceUUID();
    public abstract String getDataUUID();
    public abstract String getConfigUUID();

    public boolean isConfigUUID(String uuid) {
        return false;
    }

     public T getData() {
        return data;
    }

    public abstract String getDataString();

    public void onCharacteristicChanged(BluetoothGattCharacteristic c) {
        data = parse(c);
    }

    public boolean onCharacteristicRead(BluetoothGattCharacteristic c) {
        return false;
    }

    protected byte[] getConfigValues(boolean enable) {
        return new byte[] { (byte)(enable ? 1 : 0) };
    }

    protected abstract T parse(BluetoothGattCharacteristic c);

    public GattHandler.ServiceAction[] enable(final boolean enable) {
        return new GattHandler.ServiceAction[] {
                write(getConfigUUID(), getConfigValues(enable)),
                notify(enable)
        };
    }

    public GattHandler.ServiceAction update() {
        return GattHandler.ServiceAction.NULL;
    }

    public GattHandler.ServiceAction read(final String uuid) {
        return new GattHandler.ServiceAction() {
            @Override
            public boolean execute(BluetoothGatt bluetoothGatt) {
                final BluetoothGattCharacteristic characteristic = getCharacteristic(bluetoothGatt, uuid);
                bluetoothGatt.readCharacteristic(characteristic);
                return false;
            }
        };
    }

    public GattHandler.ServiceAction write(final String uuid, final byte[] value) {
        return new GattHandler.ServiceAction() {
            @Override
            public boolean execute(BluetoothGatt bluetoothGatt) {
                final BluetoothGattCharacteristic characteristic = getCharacteristic(bluetoothGatt, uuid);
                characteristic.setValue(value);
                bluetoothGatt.writeCharacteristic(characteristic);
                return false;
            }
        };
    }

    public GattHandler.ServiceAction notify(final boolean start) {
        return new GattHandler.ServiceAction() {
            @Override
            public boolean execute(BluetoothGatt bluetoothGatt) {
                final UUID CCC = UUID.fromString(CHARACTERISTIC_CONFIG);

                final BluetoothGattCharacteristic dataCharacteristic = getCharacteristic(bluetoothGatt, getDataUUID());
                final BluetoothGattDescriptor config = dataCharacteristic.getDescriptor(CCC);
                if (config == null)
                    return true;

                // enable/disable locally
                bluetoothGatt.setCharacteristicNotification(dataCharacteristic, start);
                // enable/disable remotely
                config.setValue(start ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                bluetoothGatt.writeDescriptor(config);
                return false;
            }
        };
    }

    private BluetoothGattCharacteristic getCharacteristic(BluetoothGatt bluetoothGatt, String uuid) {
        final UUID serviceUuid = UUID.fromString(getServiceUUID());
        final UUID characteristicUuid = UUID.fromString(uuid);

        final BluetoothGattService service = bluetoothGatt.getService(serviceUuid);
        return service.getCharacteristic(characteristicUuid);
    }
}