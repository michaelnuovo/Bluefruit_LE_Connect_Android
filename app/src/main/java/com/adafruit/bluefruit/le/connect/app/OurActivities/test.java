package com.adafruit.bluefruit.le.connect.app.OurActivities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;

import com.adafruit.bluefruit.le.connect.R;

public class test extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_grid);

        View ll = findViewById(R.id.l1);
        ll.setBackgroundColor(Color.BLACK);


    }

}
