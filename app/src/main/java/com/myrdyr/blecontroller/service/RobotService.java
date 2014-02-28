package com.myrdyr.blecontroller.service;

import android.bluetooth.BluetoothGattCharacteristic;

import com.myrdyr.blecontroller.GattHandler;

/**
 * Created by myrdyr on 27.02.14.
 */
public class RobotService extends CustomService<Integer>{
    // 1ee57080-9fef-11e3-9635-0002a5d5c51b
    private static final String UUID_SERVICE = "1ee51000-9fef-11e3-9635-0002a5d5c51b";
    private static final String UUID_DATA = "1ee52000-9fef-11e3-9635-0002a5d5c51b";

    private int command = 0;

    public RobotService() {
        super();
    }

    @Override
    public String getName() {
        return "Robot Controller";
    }

    @Override
    public String getServiceUUID() {
        return UUID_SERVICE;
    }

    @Override
    public String getDataUUID() {
        return UUID_DATA;
    }

    @Override
    public String getConfigUUID() {
        return null;
    }

    @Override
    public String getDataString() {
        return getData().toString();
    }

    @Override
    protected Integer parse(BluetoothGattCharacteristic c) {
        return null;
    }

    @Override
    public GattHandler.ServiceAction update() {
        return write(UUID_DATA, new byte[]{(byte) command});
    }

    public void setCommand(int cmd) {
        command = cmd;
    }
}
