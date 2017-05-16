package com.adafruit.bluefruit.le.BLEcom.app.Main.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.adafruit.bluefruit.le.BLEcom.R;
import com.adafruit.bluefruit.le.BLEcom.app.Main.Packets.Constants;
import com.adafruit.bluefruit.le.BLEcom.app.Main.Packets.Pattern;
import com.adafruit.bluefruit.le.BLEcom.app.UartInterfaceActivity;

public class PatternPickerActivity extends UartInterfaceActivity {

    SeekBar patBar;
    SeekBar secBar;
    SeekBar gamBar;
    
    int idVal = 1;
    int secVal = 10;
    int gamVal = 8;

    String idPrefix = "ID: ";
    String secPrefix = "SECONDS: ";
    String gamPrefix = "GAMMA: ";

    TextView patView;
    TextView secView;
    TextView gamView;

    Button sendButton;

    byte[] mPacket;

    private String getStringValue(String prefix, int param){
        return prefix + String.valueOf(param);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern_picker);

        // UI seekbars
        patBar = (SeekBar) findViewById(R.id.patId);
        secBar = (SeekBar) findViewById(R.id.patSec);
        gamBar = (SeekBar) findViewById(R.id.gamma);

        // UI textviews
        patView = (TextView) findViewById(R.id.patIdVal);
        secView = (TextView) findViewById(R.id.patSecVal);
        gamView = (TextView) findViewById(R.id.gammaVal);

        // UI send button
        sendButton = (Button) findViewById(R.id.sendButton);

        // Set default text fot text views
        patView.setText(getStringValue(idPrefix, idVal));
        secView.setText(getStringValue(secPrefix, secVal));
        gamView.setText(getStringValue(gamPrefix, gamVal));

        // Set max values for seekbars
        patBar.setMax(Constants.PATTERN_MAX);
        secBar.setMax(Constants.SECONDS_MAX);
        gamBar.setMax(Constants.GAMMA_MAX);

        patBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            /**
             * seekBar   The SeekBar whose progress has changed
             * progress  The current progress level. This will be in the range 0..max where max was set by setMax(int). (The default value for max is 100.)
             * fromUser  True if the progress change was initiated by the user.
             */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                idVal = progress;
                patView.setText(getStringValue(idPrefix, idVal));
            }
        });

        secBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            /**
             * seekBar   The SeekBar whose progress has changed
             * progress  The current progress level. This will be in the range 0..max where max was set by setMax(int). (The default value for max is 100.)
             * fromUser  True if the progress change was initiated by the user.
             */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                secVal = progress;
                secView.setText(getStringValue(secPrefix, secVal));
            }
        });

        gamBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            /**
             * seekBar   The SeekBar whose progress has changed
             * progress  The current progress level. This will be in the range 0..max where max was set by setMax(int). (The default value for max is 100.)
             * fromUser  True if the progress change was initiated by the user.
             */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                gamVal = progress;
                gamView.setText(getStringValue(gamPrefix, gamVal));
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create a new packet
                // send the packet with sendDataCRC()

                mPacket = new Pattern(idVal, secVal, gamVal).packet;
                sendDataWithCRC(mPacket);
            }
        });
    }
}
