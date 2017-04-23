package com.adafruit.bluefruit.le.connect.app.OurActivities;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.adafruit.bluefruit.le.connect.R;
import com.adafruit.bluefruit.le.connect.app.UartInterfaceActivity;
import com.adafruit.bluefruit.le.connect.ble.BleManager;

import java.util.ArrayList;

public class TerminalActivity2 extends UartInterfaceActivity {

    TextView textView;

    EditText editText1;
    EditText editText2;
    EditText editText3;
    EditText editText4;

    Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal2);

        mBleManager = BleManager.getInstance(this);

        textView = (TextView) findViewById(R.id.textView);
        editText1 = (EditText) findViewById(R.id.editText1);
        editText2 = (EditText) findViewById(R.id.editText2);
        editText3 = (EditText) findViewById(R.id.editText3);
        editText4 = (EditText) findViewById(R.id.editText4);
        saveButton = (Button) findViewById(R.id.saveButton);

        // Adapt the command line history to the list view
        ListView listView = (ListView) findViewById(R.id.history);
        ArrayList<UserCommand> history = loadFromPreferences("userCommandHistory");
        ArrayAdapter<UserCommand> arrayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.activity_list_item,
                history );
        listView.setAdapter(arrayAdapter);

        // On enter key event, shifts focus to next edit text
        setEnterKeyKeyListener(editText1);
        setEnterKeyKeyListener(editText2);
        setEnterKeyKeyListener(editText3);
        setEnterKeyKeyListener(editText4);
        setSaveButtonListener(saveButton);



        // Start services
        onServicesDiscovered();
    }

    // Listens for when save button is pressed and performs routines
    private void setSaveButtonListener(Button button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserCommand command = new UserCommand(
                        editText1.getText().toString(),
                        editText2.getText().toString(),
                        editText3.getText().toString(),
                        editText4.getText().toString());
                String text = command.toStringShowLineFeeds();
                Log.v("TAG","text is "+text);
                textView.setText(text);
                refreshHistory();
            }
        });
    }

    private static void refreshHistory(){
        history.
    }

    private void setEnterKeyKeyListener(final EditText editText){
        editText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                // OnKey is fired twice: the first time for key down, and the second time for key up, so you have to filter:
                // http://stackoverflow.com/questions/3802887/why-is-onkey-called-twice
                if (event.getAction()!=KeyEvent.ACTION_DOWN){
                    // Enter key event
                    if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                        if(editText.getId() == editText1.getId()){
                            editText2.setVisibility(View.VISIBLE);
                            editText2.requestFocus();
                        }
                        if(editText.getId() == editText2.getId()){
                            editText3.setVisibility(View.VISIBLE);
                            editText3.requestFocus();
                        }
                        if(editText.getId() == editText3.getId()){
                            editText4.setVisibility(View.VISIBLE);
                            editText4.requestFocus();
                        }
                    }
                    // Delete key event
                    if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                        boolean empty = editText.getText().toString().equals("");
                        if(empty && editText.getId() != editText1.getId()){
                            if(editText.getId() == editText2.getId()){
                                editText1.requestFocus();
                            }
                            if(editText.getId() == editText3.getId()){
                                editText2.requestFocus();
                            }
                            if(editText.getId() == editText4.getId()){
                                editText3.requestFocus();
                            }
                            editText.setVisibility(View.GONE);
                        }
                    }
                }
                return false; // return false if you don't want the widget host to handle the key
            }
        });
    }
}
