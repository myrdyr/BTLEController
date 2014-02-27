package com.myrdyr.blecontroller;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import com.myrdyr.blecontroller.adapters.ServiceAdapter;

import java.util.List;

/**
 * Created by myrdyr on 23.02.14.
 */
public class DeviceControllerActivity extends Activity{
    private final static String TAG = DeviceControllerActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView connectionState;
    private TextView dataField;
    private ExpandableListView gattServicesList;
    private ServiceAdapter gattServiceAdapter;

    private String deviceName;
    private String deviceAddress;
    private BtleService btleService;
    private boolean isConnected = false;


    /* BtleService manager */
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            btleService = ((BtleService.LocalBinder) service).getService();
            if (!btleService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            btleService.connect(deviceAddress); /* Connect immediately */
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            btleService = null;
        }
    };

    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BtleService.ACTION_GATT_CONNECTED.equals(action)) {
                isConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BtleService.ACTION_GATT_DISCONNECTED.equals(action)) {
                isConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BtleService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(btleService.getSupportedGattServices());
            } else if (BtleService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BtleService.EXTRA_TEXT));
            }
        }
    };

    private final ExpandableListView.OnChildClickListener servicesListClickListener =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    if (gattServiceAdapter == null)
                        return false;

                    final BluetoothGattCharacteristic characteristic = gattServiceAdapter.getChild(groupPosition, childPosition);
                    //final TiSensor<?> sensor = TiSensors.getSensor(characteristic.getService().getUuid().toString());

//                    if (activeSensor != null)
//                        bleService.enableSensor(activeSensor, false);

//                    if (sensor == null) {
                    if(true)
                        btleService.readCharacteristic(characteristic);
                        return true;
                    }

//                    if (sensor == activeSensor)
//                        return true;
//
//                    activeSensor = sensor;
//                    bleService.enableSensor(sensor, true);
//                    return true;
//                }
            };

//    private final TiServicesAdapter.OnServiceItemClickListener demoClickListener = new TiServicesAdapter.OnServiceItemClickListener() {
//        @Override
//        public void onDemoClick(BluetoothGattService service) {
//            final TiSensor<?> sensor = TiSensors.getSensor(service.getUuid().toString());
//            if (sensor == null)
//                return;
//
//            final Class<? extends DemoSensorActivity> demoClass;
//            if (sensor instanceof TiAccelerometerSensor)
//                demoClass = DemoAccelerometerSensorActivity.class;
//            else if (sensor instanceof TiGyroscopeSensor)
//                demoClass = DemoGyroscopeSensorActivity.class;
//            else
//                return;
//
//            final Intent demoIntent = new Intent();
//            demoIntent.setClass(DeviceServicesActivity.this, demoClass);
//            demoIntent.putExtra(DemoSensorActivity.EXTRAS_DEVICE_ADDRESS, deviceAddress);
//            demoIntent.putExtra(DemoSensorActivity.EXTRAS_SENSOR_UUID, service.getUuid().toString());
//            startActivity(demoIntent);
//        }
//
//        @Override
//        public void onServiceEnabled(BluetoothGattService service, boolean enabled) {
//            if (gattServiceAdapter == null)
//                return;
//
//            final TiSensor<?> sensor = TiSensors.getSensor(service.getUuid().toString());
//            if (sensor == null)
//                return;
//
//            if (sensor == activeSensor)
//                return;
//
//            if (activeSensor != null)
//                bleService.enableSensor(activeSensor, false);
//            activeSensor = sensor;
//            bleService.enableSensor(sensor, true);
//        }
//
//        @Override
//        public void onServiceUpdated(BluetoothGattService service) {
//            final TiSensor<?> sensor = TiSensors.getSensor(service.getUuid().toString());
//            if (sensor == null)
//                return;
//
//            bleService.updateSensor(sensor);
//        }
//    };

    private void clearUI() {
        gattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        dataField.setText(R.string.no_data);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.controller);

        /* Fetch the intent of this activity and its params */
        final Intent intent = getIntent();
        deviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        deviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        /* Set up UI */
        ((TextView) findViewById(R.id.device_address)).setText(deviceAddress);
        gattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        gattServicesList.setOnChildClickListener(servicesListClickListener);
        connectionState = (TextView) findViewById(R.id.connection_state);
        dataField = (TextView) findViewById(R.id.data_value);

        getActionBar().setTitle(deviceName);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.device_controller, menu);
        if (isConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                btleService.connect(deviceAddress);
                return true;
            case R.id.menu_disconnect:
                btleService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectionState.setText(resourceId);
            }
        });
    }

    private void displayData(String data) {
        if (data != null) {
            dataField.setText(data);
        }
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null)
            return;

        gattServiceAdapter = new ServiceAdapter(this, gattServices);
        //gattServiceAdapter.setServiceListener(demoClickListener); //@TODO
        gattServicesList.setAdapter(gattServiceAdapter);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BtleService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BtleService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BtleService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BtleService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
