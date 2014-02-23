package com.myrdyr.blecontroller;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

/**
 * Created by myrdyr on 23.02.14.
 */
public class BtleService extends Service {
    private final static String TAG = BtleService.class.getSimpleName();

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter adapter;
    private String deviceAddress;
    private BluetoothGatt gatt;
    private State connectionState = State.DISCONNECTED;

    private static enum State {DISCONNECTED, CONNECTING, CONNECTED}

    private final GattHandler gattHandler = new GattHandler(); /* @TODO: More code */

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

//    private void broadcastUpdate(final String action,
//                                 final BluetoothGattCharacteristic characteristic) {
//        final Intent intent = new Intent(action);
//        intent.putExtra(EXTRA_SERVICE_UUID, characteristic.getService().getUuid().toString());
//        intent.putExtra(EXTRA_CHARACTERISTIC_UUID, characteristic.getUuid().toString());
//
//        final TiSensor<?> sensor = TiSensors.getSensor(characteristic.getService().getUuid().toString());
//        if (sensor != null) {
//            sensor.onCharacteristicChanged(characteristic);
//            final String text = sensor.getDataString();
//            intent.putExtra(EXTRA_TEXT, text);
//            sendBroadcast(intent);
//        } else {
//            // For all other profiles, writes the data formatted in HEX.
//            final byte[] data = characteristic.getValue();
//            if (data != null && data.length > 0) {
//                final StringBuilder stringBuilder = new StringBuilder(data.length);
//                for (byte byteChar : data)
//                    stringBuilder.append(String.format("%02X ", byteChar));
//                intent.putExtra(EXTRA_TEXT, new String(data) + "\n" + stringBuilder.toString());
//            }
//        }
//        sendBroadcast(intent);
//    }

    public class LocalBinder extends Binder {
        public BtleService getService() {
            return BtleService.this;
        }
    }
    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent){
        // @TODO: close();
        return super.onUnbind(intent);
    }

    public boolean initialize() {
        /* Get adapter through BluetoothManager */
        if (bluetoothManager == null) {
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if(bluetoothManager == null) {
                Log.e(TAG, "Failed to init BluetoothManager!");
                return false;
            }
        }

        adapter = bluetoothManager.getAdapter();
        if (adapter == null) {
            Log.e(TAG, "Failed to obtain BluetoothAdapter!");
            return false;
        }

        return true;
    }

    public boolean connect(final String address) {
        if (adapter == null || address == null){
            Log.w(TAG, "Missing address or uninitialized BluetoothAdapter");
            return false;
        }

        /* We know this device from before, so reconnect */
        if (deviceAddress != null && address.equals(deviceAddress) && gatt != null) {
            Log.d(TAG, "Reusing previous BluetoothGatt");
            if (gatt.connect()) {
                connectionState = State.CONNECTING;
            }
            else {
                return false;
            }

        }
        /* New device. Connect to it with our local gatt executor as callback */
        final BluetoothDevice device = adapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found");
            return false;
        }
        gatt = device.connectGatt(this, false, gattHandler);
        Log.d(TAG, "Connecting to: " + address);
        deviceAddress = address;
        connectionState = State.CONNECTING;
        return true;
    }

    public void disconnect() {
        if (adapter == null || gatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or no connection");
            return;
        }
        gatt.disconnect();
    }

    public void close() {
        if (gatt == null){
            return;
        }
        gatt.close();
        gatt = null;
    }

    public List<BluetoothGattService> getSupportedGattServices() {
        if (gatt == null) return null;

        return gatt.getServices();
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (adapter == null || gatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        gatt.readCharacteristic(characteristic);
    }
}
