package com.myrdyr.blecontroller.demo;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;

import com.myrdyr.blecontroller.BtleService;
import com.myrdyr.blecontroller.GattHandler;
import com.myrdyr.blecontroller.service.CustomService;
import com.myrdyr.blecontroller.service.CustomServices;

/**
 * Created by myrdyr on 27.02.14.
 */
public abstract class DemoActivity extends Activity{
    private final static String TAG = DemoActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String EXTRAS_SENSOR_UUID = "SERVICE_UUID";
    public static final String EXTRAS_DATA_UUID = "EXTRAS_DATA_UUID";

    private BtleService btleService;
    private String serviceUuid;
    private String deviceAddress;
    private String dataUuid;

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BtleService.ACTION_GATT_DISCONNECTED.equals(action)) {
                //TODO: show toast
                finish();
            } else if (BtleService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                final CustomService<?> customService = CustomServices.getCustomService(serviceUuid);
                btleService.enableService(customService, true);
            } else if (BtleService.ACTION_DATA_AVAILABLE.equals(action)) {
                final CustomService<?> sensor = CustomServices.getCustomService(serviceUuid);
                final String text = intent.getStringExtra(BtleService.EXTRA_TEXT);
                onDataReceived(sensor, text);
            }
        }
    };

    // Code to manage Service lifecycle.
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            btleService = ((BtleService.LocalBinder) service).getService();
            if (!btleService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            btleService.connect(deviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            btleService = null;
            //TODO: show toast
            finish();
        }
    };

    public abstract void onDataReceived(CustomService<?> customService, String text);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        deviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        serviceUuid = intent.getStringExtra(EXTRAS_SENSOR_UUID);
        dataUuid = intent.getStringExtra(EXTRAS_DATA_UUID);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        final Intent gattServiceIntent = new Intent(this, BtleService.class);
        bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());
        if (btleService != null) {
            final boolean result = btleService.connect(deviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        btleService = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BtleService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BtleService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BtleService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public void updateService(CustomService<?> service) {
        btleService.updateService(service);
    }
}
