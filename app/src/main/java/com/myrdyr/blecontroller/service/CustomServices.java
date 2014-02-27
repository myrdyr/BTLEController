package com.myrdyr.blecontroller.service;

import java.util.HashMap;

/**
 * Created by myrdyr on 27.02.14.
 */
public class CustomServices {
    private static HashMap<String, CustomService<?>> CUSTOM_SERVICES = new HashMap<String, CustomService<?>>();

    static {
        final RobotService robotService = new RobotService();

        CUSTOM_SERVICES.put(robotService.getServiceUUID(), robotService);
    }

    public static CustomService<?> getCustomService(String uuid) {
        return CUSTOM_SERVICES.get(uuid);
    }
}
