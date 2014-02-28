package com.myrdyr.blecontroller.demo;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.myrdyr.blecontroller.GattHandler;
import com.myrdyr.blecontroller.R;
import com.myrdyr.blecontroller.service.CustomService;
import com.myrdyr.blecontroller.service.RobotService;

import java.util.UUID;

/**
 * Created by myrdyr on 27.02.14.
 */
public class RobotControllerActivity extends DemoActivity {
    private final static String TAG = RobotControllerActivity.class.getSimpleName();
    private Robot robot = new Robot();
    private RobotService robotService = new RobotService();
//    private String dataUuid;
//    UUID uuid;
//    private BluetoothGattCharacteristic cmdChar;

    private TextView viewText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_robotcontroller);

        getActionBar().setTitle(R.string.title_demo_robotcontroller);

        viewText = (TextView) findViewById(R.id.robot_cmd_status);

//        final Intent intent = getIntent();
//        dataUuid = intent.getStringExtra(EXTRAS_DATA_UUID);
//        uuid = UUID.fromString(dataUuid);
//        cmdChar = new BluetoothGattCharacteristic(uuid, 0x10, 0x11);
    }

    @Override
    public void onDataReceived(CustomService<?> customService, String text) {
        if (customService instanceof RobotService) {
            final RobotService robotService = (RobotService) customService;
            int values = robotService.getData();
            // Do something with values: renderer.setRotation(values);

            viewText.setText(text);
        }
    }

    public void onButtonClick(View v)
    {
        switch (v.getId())
        {
            case R.id.button_left:
                robot.updateCommand(Robot.COMMAND.LEFT);
                break;
            case R.id.button_right:
                robot.updateCommand(Robot.COMMAND.RIGHT);
                break;
            case R.id.button_up:
                robot.updateCommand(Robot.COMMAND.UP);
                break;
            case R.id.button_down:
                robot.updateCommand(Robot.COMMAND.DOWN);
                break;
        }
        final int cmd = robot.getCommand();
        viewText.setText(Integer.toString(cmd));
        robotService.setCommand(cmd);
        //GattHandler.ServiceAction update = robotService.update();
        updateService(robotService);
    }
}