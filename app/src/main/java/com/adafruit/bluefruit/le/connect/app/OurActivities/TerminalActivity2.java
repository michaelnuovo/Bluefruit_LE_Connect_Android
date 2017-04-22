package com.adafruit.bluefruit.le.connect.app.OurActivities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import com.adafruit.bluefruit.le.connect.R;
import com.adafruit.bluefruit.le.connect.app.UartInterfaceActivity;

public class TerminalActivity2 extends UartInterfaceActivity {

    EditText editText1;
    EditText editText2;
    EditText editText3;
    EditText editText4;
    int methodCallCounter=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal2);

        editText1 = (EditText) findViewById(R.id.editText1);
        editText2 = (EditText) findViewById(R.id.editText2);
        editText3 = (EditText) findViewById(R.id.editText3);
        editText4 = (EditText) findViewById(R.id.editText4);

        setTextChangeListener(editText1);

        // On enter key event, shifts focus to next edit text
        setEnterKeyKeyListener(editText1);
        setEnterKeyKeyListener(editText2);
        setEnterKeyKeyListener(editText3);
        setEnterKeyKeyListener(editText4);

        //setBackSpaceKeyListener(editText4, editText3);
        //setBackSpaceKeyListener(editText3, editText2);
        //setBackSpaceKeyListener(editText2, editText1);
    }

    private void setEnterKeyKeyListener(final EditText editText){
        editText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                // OnKey is fired twice: the first time for key down, and the second time for key up, so you have to filter:
                // http://stackoverflow.com/questions/3802887/why-is-onkey-called-twice
                if (event.getAction()!=KeyEvent.ACTION_DOWN){
                    Log.v("TAG","XXXXXXX");
                    // Enter key event
                    if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                        if(editText.getId() == editText1.getId()){
                            editText2.setVisibility(View.VISIBLE);
                            editText2.requestFocus();
                            Log.v("TAG","AAAAAAAA");
                        }
                        if(editText.getId() == editText2.getId()){
                            editText3.setVisibility(View.VISIBLE);
                            editText3.requestFocus();
                            Log.v("TAG","BBBBBBBB");
                        }
                        if(editText.getId() == editText3.getId()){
                            editText4.setVisibility(View.VISIBLE);
                            editText4.requestFocus();
                            Log.v("TAG","CCCCCCCC");
                        }
                    }
                    // Delete key event
                    if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                        boolean empty = editText.getText().toString().equals("");
                        if(empty && editText.getId() != editText1.getId()){
                            if(editText.getId() == editText2.getId()){
                                editText1.requestFocus();
                                Log.v("TAG","DDDDDDD");
                            }
                            if(editText.getId() == editText3.getId()){
                                editText2.requestFocus();
                                Log.v("TAG","EEEEEEEEE");
                            }
                            if(editText.getId() == editText4.getId()){
                                editText3.requestFocus();
                                Log.v("TAG","FFFFFFFFF");
                            }
                            editText.setVisibility(View.GONE);
                        }
                    }
                }

                return false; // return false if you don't want the widget host to handle the key
            }
        });
    }

    private void setBackSpaceKeyListener(final EditText below, final EditText above){
        below.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                    Log.v("TAG","herslksldfk");
                    boolean empty = below.getText().toString().equals("");
                    Log.v("TAG","empty val is "+String.valueOf(empty));
                    if(empty){
                        above.requestFocus();
                        below.setVisibility(View.GONE);
                    }
                }
                return false;
            }
        });
    }

    private void setTextChangeListener(EditText editText1){
        editText1.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
//                EditText secondLine = (EditText) findViewById(R.id.editText2);
//                String result = s.toString().replaceAll("\n", "");
//                if (!s.toString().equals(result)) {
//                    editText1.setText(result);
//                    editText1.setSelection(result.length());
//                    secondLine.setVisibility(View.VISIBLE);
//                    secondLine.requestFocus();
//                }
            }
        });
    }
}
