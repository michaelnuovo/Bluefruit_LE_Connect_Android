package com.adafruit.bluefruit.le.BLEcom.app.Main.Activities;

import android.os.Bundle;

import com.adafruit.bluefruit.le.BLEcom.R;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

import java.util.ArrayList;

public class ColorPickerActivity4Colors extends ColorPickerActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.activity_color_picker_4colors);
        className = this.getClass().toString();

        viewList = new ArrayList<>();
        viewList.add(findViewById(R.id.rgbColorViewOne));
        viewList.add(findViewById(R.id.rgbColorViewTwo));
        viewList.add(findViewById(R.id.rgbColorViewThree));
        viewList.add(findViewById(R.id.rgbColorViewFour));

        mColorPicker = (ColorPicker) findViewById(R.id.colorPicker);
        SaturationBar mSaturationBar = (SaturationBar) findViewById(R.id.saturationbar);
        ValueBar mValueBar = (ValueBar) findViewById(R.id.valuebar);
        if (mColorPicker != null) { // Prevents null reference error?
            mColorPicker.addSaturationBar(mSaturationBar);
            mColorPicker.addValueBar(mValueBar);
            mColorPicker.setOnColorChangedListener(this);
        }

        super.onCreate(savedInstanceState);

    }
}
