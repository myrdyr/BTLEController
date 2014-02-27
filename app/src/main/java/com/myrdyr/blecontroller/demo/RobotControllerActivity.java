package com.myrdyr.blecontroller.demo;

import android.os.Bundle;
import android.widget.TextView;

import com.myrdyr.blecontroller.R;
import com.myrdyr.blecontroller.service.CustomService;
import com.myrdyr.blecontroller.service.RobotService;

/**
 * Created by myrdyr on 27.02.14.
 */
public class RobotControllerActivity extends DemoActivity {
    private final static String TAG = RobotControllerActivity.class.getSimpleName();

    private TextView viewText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_robotcontroller);

        getActionBar().setTitle(R.string.title_demo_robotcontroller);

//        viewText = (TextView) findViewById(R.id.text);

    }

    @Override
    public void onDataRecieved(CustomService<?> customService, String text) {
        if (customService instanceof RobotService) {
            final RobotService robotService = (RobotService) customService;
            int values = robotService.getData();
            // Do something with values: renderer.setRotation(values);

            viewText.setText(text);
        }
    }
}