package com.adafruit.bluefruit.le.connect.app.OurActivities;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.adafruit.bluefruit.le.connect.R;
import com.adafruit.bluefruit.le.connect.app.UartInterfaceActivity;
import com.adafruit.bluefruit.le.connect.ble.BleManager;

public class TerminalActivity extends UartInterfaceActivity {

    EditText editText;
    Button saveButton;
    Button sendButton;
    TextView textView;
    byte[] packet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal);

        mBleManager = BleManager.getInstance(this);

        editText = (EditText) findViewById(R.id.editText);
        saveButton = (Button) findViewById(R.id.saveButton);
        textView = (TextView) findViewById(R.id.textView);
        sendButton = (Button) findViewById(R.id.sendButton);

        setEditTextListener(editText);
        setSaveButtonListener(saveButton);
        setSendButtonListener(sendButton);

        // Start services
        onServicesDiscovered();


        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                //Your query to fetch Data
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.v("TAG","s is " + s);
            }

            @Override
            public void afterTextChanged(Editable s) {
                String result = s.toString().replaceAll(" ", "");
                if (!s.toString().equals(result)) {
                    editText.setText(result);
                    editText.setSelection(result.length());
                    // alert the user
                }
            }
        });
    }

    private void setSendButtonListener(final Button sendButton){
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(packet != null)
                    sendDataWithCRC(packet);

            }
        });
    }

    private void setEditTextListener(final EditText editText){
        editText.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //editText.setText("");
                editText.setCursorVisible(true);
                editText.setHint("");
                editText.clearFocus();

                InputMethodManager imm = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);

            }
        });
    }

    // Listens for when save button is pressed and performs routines
    private void setSaveButtonListener(Button button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Gets edit text input
                String editTextInput = editText.getText().toString();

                // Gets ASCII binary array of editTextInput
                byte[] byteArrASCII = PacketUtils.stringToBytesASCII(editTextInput);

                // Gets the packet
                packet = PacketUtils.byteArrayToPacket(byteArrASCII, PacketUtils.PacketTypes.USER_COMMAND);

                // Adds line feed to end of packet if there isn't one already
                if(packet[packet.length-1] != 10) packet = PacketUtils.addTerminalLineFeed(packet);

                // Gets formatted string of data from package
                String packetStats = PacketUtils.getPacketStats(packet);

                // Displays packet stats in text view
                textView.setText(packetStats);

                // Clears the edit text
                editText.getText().clear();

                // Closes soft keyboard
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
            }
        });
    }



}
