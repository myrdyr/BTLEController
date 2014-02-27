package com.myrdyr.blecontroller;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.myrdyr.blecontroller.service.CustomService;

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

    private final static String INTENT_PREFIX = BtleService.class.getPackage().getName();
    public final static String ACTION_GATT_CONNECTED = INTENT_PREFIX+".ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = INTENT_PREFIX+".ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = INTENT_PREFIX+".ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = INTENT_PREFIX+".ACTION_DATA_AVAILABLE";
    public final static String EXTRA_SERVICE_UUID = INTENT_PREFIX+".EXTRA_SERVICE_UUID";
    public final static String EXTRA_CHARACTERISTIC_UUID = INTENT_PREFIX+".EXTRA_CHARACTERISTIC_UUI";
    public final static String EXTRA_DATA = INTENT_PREFIX+".EXTRA_DATA";
    public final static String EXTRA_TEXT = INTENT_PREFIX+".EXTRA_TEXT";

    private final GattHandler gattHandler = new GattHandler() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                connectionState = State.CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Do a service discovery after successful connection.
                Log.i(TAG, "Initiating service discovery:" +
                        BtleService.this.gatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                connectionState = State.DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            super.onCharacteristicRead(gatt, characteristic, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
//                final TiSensor<?> sensor = TiSensors.getSensor(characteristic.getService().getUuid().toString());
//                if (sensor != null) {
//                    if (sensor.onCharacteristicRead(characteristic)) {
//                        return;
//                    }
//                }
                // @TODO: Figure out
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_SERVICE_UUID, characteristic.getService().getUuid().toString());
        intent.putExtra(EXTRA_CHARACTERISTIC_UUID, characteristic.getUuid().toString());
/* @TODO: Figure out */
        //final TiSensor<?> sensor = TiSensors.getSensor(characteristic.getService().getUuid().toString());
//        if (sensor != null) {
        if (false) {
//            sensor.onCharacteristicChanged(characteristic);
//            final String text = sensor.getDataString();
//            intent.putExtra(EXTRA_TEXT, text);
//            sendBroadcast(intent);
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_TEXT, new String(data) + "\n" + stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }

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
        close();
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

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (adapter == null || gatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        gatt.readCharacteristic(characteristic);
    }

//    public void updateSensor(CustomService<?> customService) {
//        if (customService == null)
//            return;
//
//        if (adapter == null || gatt == null) {
//            Log.w(TAG, "BluetoothAdapter not initialized");
//            return;
//        }
//
//        gattHandler.update(customService);
//        gattHandler.execute(gatt);
//    }
//
//    /**
//     * Enables or disables notification on a give characteristic.
//     *
//     * @param customService
//     * @param enabled If true, enable notification.  False otherwise.
//     */
//
//    public void enableSensor(CustomService<?> customService, boolean enabled) {
//        if (customService == null)
//            return;
//
//        if (adapter == null || gatt == null) {
//            Log.w(TAG, "BluetoothAdapter not initialized");
//            return;
//        }
//
//        gattHandler.enable(customService, enabled);
//        gattHandler.execute(gatt);
//    }
/* @TODO: Figure out */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (gatt == null) return null;

        return gatt.getServices();
    }


}
