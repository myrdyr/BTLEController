package com.myrdyr.blecontroller;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.ExpandableListView;
import android.widget.TextView;

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

    private String deviceName;
    private String deviceAddress;
    private BtleService btleService;
    private boolean isConnected = false;


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

        }
    };

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
        //gattServicesList.setOnChildClickListener(servicesListClickListener);
        connectionState = (TextView) findViewById(R.id.connection_state);
        dataField = (TextView) findViewById(R.id.data_value);

        getActionBar().setTitle(deviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        final Intent gattServiceIntent = new Intent(this, BtleService.class);
        bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);
    }
}
