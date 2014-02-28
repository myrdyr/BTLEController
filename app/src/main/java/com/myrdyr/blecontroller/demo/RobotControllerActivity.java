package com.myrdyr.blecontroller.demo;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.myrdyr.blecontroller.R;
import com.myrdyr.blecontroller.service.CustomService;
import com.myrdyr.blecontroller.service.RobotService;

/**
 * Created by myrdyr on 27.02.14.
 */
public class RobotControllerActivity extends DemoActivity {
    private final static String TAG = RobotControllerActivity.class.getSimpleName();
    private Robot robot = new Robot();

    private TextView viewText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_robotcontroller);

        getActionBar().setTitle(R.string.title_demo_robotcontroller);

        viewText = (TextView) findViewById(R.id.robot_cmd_status);

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
        final String cmd = Integer.toString(robot.getCommand());
        viewText.setText(cmd);
    }
}