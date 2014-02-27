package com.myrdyr.blecontroller.service;

import java.util.HashMap;

/**
 * Created by myrdyr on 27.02.14.
 */
public class InfoServices {
    private static HashMap<String, InfoService> SERVICES = new HashMap<String, InfoService>();

    static {
        final GattService gattService = new GattService();
        final GapService gapService = new GapService();
        final DeviceInfoService deviceInfoService = new DeviceInfoService();

        SERVICES.put(gapService.getUUID(), gapService);
        SERVICES.put(gattService.getUUID(), gattService);
        SERVICES.put(deviceInfoService.getUUID(), deviceInfoService);
    }

    public static InfoService getService(String uuid) {
        return SERVICES.get(uuid);
    }
}
