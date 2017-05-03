package com.adafruit.bluefruit.le.BLEcom.app.OurActivities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.adafruit.bluefruit.le.BLEcom.R;

public class test extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_grid);

        View ll = findViewById(R.id.l1);
        ll.setBackgroundColor(Color.BLACK);


    }

}
