package com.myrdyr.blecontroller;

import android.bluetooth.BluetoothAdapter;

public class Scanner extends Thread{
    private final BluetoothAdapter bluetoothAdapter;
    private final BluetoothAdapter.LeScanCallback mLeScanCallback;
    private static final int SCAN_TIME = 500;

    private volatile boolean isScanning = false;

    Scanner(BluetoothAdapter adapter, BluetoothAdapter.LeScanCallback callback) {
        bluetoothAdapter = adapter;
        mLeScanCallback = callback;
    }

    public boolean isScanning() {
        return isScanning;
    }

    public void startScanning() {
        synchronized (this) {
            isScanning = true;
            start();
        }
    }

    public void stopScanning() {
        synchronized (this) {
            isScanning = false;
            bluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                synchronized (this) {
                    if (!isScanning)
                        break;

                    bluetoothAdapter.startLeScan(mLeScanCallback);
                }

                sleep(SCAN_TIME);

                synchronized (this) {
                    bluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }
        } catch (InterruptedException ignore) {
        } finally {
            bluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }
}
