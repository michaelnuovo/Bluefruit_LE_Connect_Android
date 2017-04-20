package com.adafruit.bluefruit.le.connect.app.OurActivities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.adafruit.bluefruit.le.connect.R;

public class TerminalActivity extends AppCompatActivity {

    MyEditText editText;
    Button sendButton;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal);

        editText = (MyEditText) findViewById(R.id.editText);
        sendButton = (Button) findViewById(R.id.sendButton);
        textView = (TextView) findViewById(R.id.textView);

        setListener(editText);
        setButtonListener(sendButton);
    }

    private void setListener(final MyEditText editText){
        editText.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                editText.setText("");
                editText.setCursorVisible(true);
                editText.setHint("");
                editText.clearFocus();

            }
        });
    }

    // Listens for when save button is pressed and performs routines
    private void setButtonListener(Button button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Gets edit text input
                String editTextInput = editText.getText().toString();

                // Gets ASCII binary array of editTextInput
                byte[] byteArrASCII = PacketUtils.stringToBytesASCII(editTextInput);

                // Gets the packet
                byte[] packet = PacketUtils.getPacket(byteArrASCII, "!T");

                // Gets formatted string of data from package
                String packetStats = PacketUtils.getPacketStats(packet);

                // Displays packet stats in text view
                textView.setText(packetStats);

                // Clears the edit text
                editText.getText().clear();

                // Closes soft keyboard
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);

                /**
                 * debugging
                 */

                Log.v("TAG","editTextInput is "+editTextInput);


                //editText.clearFocus();
                //imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                //this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            }
        });
    }
}
