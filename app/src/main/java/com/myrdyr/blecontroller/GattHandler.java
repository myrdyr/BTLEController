package com.myrdyr.blecontroller;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;

import com.myrdyr.blecontroller.service.CustomService;

import java.util.LinkedList;

/**
 * Created by myrdyr on 23.02.14.
 */
public class GattHandler extends BluetoothGattCallback{

    public interface ServiceAction {
        public static final ServiceAction NULL = new ServiceAction() {
            @Override
            public boolean execute(BluetoothGatt bluetoothGatt) {
                return true;
            }
        };

        /***
         * Executes action.
         * @param bluetoothGatt
         * @return true - if action was executed instantly. false if action is waiting for
         *         feedback.
         */
        public boolean execute(BluetoothGatt bluetoothGatt);
    }

    private final LinkedList<GattHandler.ServiceAction> queue = new LinkedList<ServiceAction>();
    private volatile ServiceAction currentAction;

    public void update(final CustomService customService) {
        queue.add(customService.update());
    }

    public void enable(CustomService customService, boolean enable) {
        if(customService.getConfigUUID() != null)
        {
            final ServiceAction[] actions = customService.enable(enable);
            for ( ServiceAction action : actions ) {
                this.queue.add(action);
            }
        }
    }

    public void execute(BluetoothGatt gatt) {
        if (currentAction != null)
            return;

        boolean next = !queue.isEmpty();
        while (next) {
            final GattHandler.ServiceAction action = queue.pop();
            currentAction = action;
            if (!action.execute(gatt))
                break;

            currentAction = null;
            next = !queue.isEmpty();
        }
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);

        currentAction = null;
        execute(gatt);
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);

        currentAction = null;
        execute(gatt);
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            queue.clear();
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt,
                                     BluetoothGattCharacteristic characteristic,
                                     int status) {
        currentAction = null;
        execute(gatt);
    }
}
