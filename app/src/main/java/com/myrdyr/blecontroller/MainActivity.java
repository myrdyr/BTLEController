package com.myrdyr.blecontroller;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.myrdyr.blecontroller.adapters.BtleDevicesAdapter;

public class MainActivity extends ListActivity {

    //private static enum Activities {REQUEST_BT_ENABLE}
    private static final int REQUEST_BTLE_ENABLE = 1;

    private BtleDevicesAdapter btleDevicesAdapter;
    private BluetoothAdapter bluetoothAdapter;
    private Scanner scanner;

    private void init_btle() {
        if (btleDevicesAdapter == null) {
            btleDevicesAdapter = new BtleDevicesAdapter(getBaseContext());
            setListAdapter(btleDevicesAdapter);
        }

        if (scanner == null) {
            scanner = new Scanner(bluetoothAdapter, mLeScanCallback);
            scanner.startScanning();
        }

        invalidateOptionsMenu();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setTitle(R.string.app_titlebar);
        //setContentView(R.layout.activity_main);

        /* Check for BTLE support */
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(this, "BTLE not supported!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        /* Ask user to enable bluetooth and get the adapter */
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if(bluetoothAdapter == null){
            Toast.makeText(this, "Could not init bluetooth", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.scanning, menu);
        /* Not scanning, hide stop button */
        if (scanner == null || !scanner.isScanning()){
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        }
        else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_scan:
                btleDevicesAdapter.clear();
                if(scanner == null){
                    scanner = new Scanner(bluetoothAdapter, mLeScanCallback);
                    scanner.startScanning();
                    invalidateOptionsMenu(); /* Refresh menu */
                }
                break;
            case R.id.menu_stop:
                if (scanner != null){
                    scanner.stopScanning();
                    scanner = null;
                    invalidateOptionsMenu(); /* Refresh menu */
                }
                break;
        }
        return true;
    }

    @Override
    protected void onResume(){
        super.onResume();

        if(!bluetoothAdapter.isEnabled()){
            final Intent enableBTLEIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTLEIntent, REQUEST_BTLE_ENABLE);
            return;
        }
        init_btle();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_BTLE_ENABLE) {
            if (resultCode == Activity.RESULT_CANCELED) {
                finish();
            } else {
                init_btle();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (scanner != null) {
            scanner.stopScanning();
            scanner = null;
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final BluetoothDevice device = btleDevicesAdapter.getDevice(position);
        if (device == null)
            return;

        final Intent intent = new Intent(this, DeviceControllerActivity.class);
        intent.putExtra(DeviceControllerActivity.EXTRAS_DEVICE_NAME, device.getName());
        intent.putExtra(DeviceControllerActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
        startActivity(intent);
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btleDevicesAdapter.addDevice(device, rssi);
                            btleDevicesAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };
}
