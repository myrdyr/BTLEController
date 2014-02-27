package com.myrdyr.blecontroller.service;

/**
 * Created by myrdyr on 27.02.14.
 */
public abstract class InfoService {
    private final static String TAG = InfoService.class.getSimpleName();

    protected InfoService() {
    }

    public abstract String getUUID();
    public abstract String getName();
    public abstract String getCharacteristicName(String uuid);
}
