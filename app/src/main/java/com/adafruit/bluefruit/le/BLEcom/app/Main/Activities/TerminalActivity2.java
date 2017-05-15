package com.adafruit.bluefruit.le.BLEcom.app.Main.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.adafruit.bluefruit.le.BLEcom.R;
import com.adafruit.bluefruit.le.BLEcom.app.Main.Adapters.CommandLineAdapter;
import com.adafruit.bluefruit.le.BLEcom.app.Main.Packets.UserCommand;
import com.adafruit.bluefruit.le.BLEcom.app.UartInterfaceActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class TerminalActivity2 extends UartInterfaceActivity {

    private final static String classPrefs = ColorPickerActivity8Colors.class.getName();

    //TextView textView;
    ListView listView;

    EditText editText1;
    EditText editText2;
    EditText editText3;
    EditText editText4;

    TextView saveButton;

    ArrayList<UserCommand> history;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal2);

        //mBleManager = BleManager.getInstance(this);

        //textView = (TextView) findViewById(R.id.textView);
        editText1 = (EditText) findViewById(R.id.editText1);
        editText2 = (EditText) findViewById(R.id.editText2);
        editText3 = (EditText) findViewById(R.id.editText3);
        editText4 = (EditText) findViewById(R.id.editText4);
        saveButton = (TextView) findViewById(R.id.saveButton);
        listView = (ListView) findViewById(R.id.history);

        // Return the history
        history = getHistory();

        // TODO learn how to save an array list of type UserCommand as a json object and save it and load it to prefs

        // Adapt the command line history to the list view

        CommandLineAdapter adapter = new CommandLineAdapter(
                this,
                R.layout.history_list_item,
                history);

        listView.setAdapter(adapter);

        // On enter key event, shifts focus to next edit text
        setEnterKeyKeyListener(editText1);
        setEnterKeyKeyListener(editText2);
        setEnterKeyKeyListener(editText3);
        setEnterKeyKeyListener(editText4);
        setSaveButtonListener(saveButton);


        // Start services
        //onServicesDiscovered();
    }

    // Returns an array list of UserCommand objects (the command line history)
    private ArrayList<UserCommand> getHistory(){
        SharedPreferences preferences = getPrefs();
        String jsonString = preferences.getString("history","");
        Type type = new TypeToken<ArrayList<UserCommand>>(){}.getType();
        ArrayList<UserCommand> userCommands;
        // If the json string is empty initialize to default history
        if(jsonString.equals(""))
            userCommands = getDefaultHistory();
        // else initialize to deserialized json string
        else
            userCommands = new Gson().fromJson(jsonString, type);
        return userCommands;
    }

    private ArrayList<UserCommand> getDefaultHistory(){
        //UserCommand defaultCommand = new UserCommand("xyz","","","");
        UserCommand defaultCommand = UserCommand.testCommand1();
        ArrayList<UserCommand> defaultHistory = new ArrayList<>();
        defaultHistory.add(defaultCommand);
        return defaultHistory;
    }

    private SharedPreferences getPrefs(){
        return getSharedPreferences(classPrefs, Context.MODE_PRIVATE);
    }

    //http://stackoverflow.com/questions/28439003/use-parcelable-to-store-item-as-sharedpreferences
    //private void saveJson

    private void saveToPreferences(String stringHandle, int intVal){
        SharedPreferences preferences = getSharedPreferences(classPrefs, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(stringHandle, intVal);
        editor.apply();
    }

    //
    private int loadFromPreferences(String stringHandle){
        SharedPreferences preferences = getSharedPreferences(classPrefs, Context.MODE_PRIVATE);
        return preferences.getInt(stringHandle,-1);
    }


    // Listens for when save button is pressed and performs routines
    private void setSaveButtonListener(TextView button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserCommand command = new UserCommand(
                        editText1.getText().toString(),
                        editText2.getText().toString(),
                        editText3.getText().toString(),
                        editText4.getText().toString());

                Log.v("TAG","command to string is"+command.toString());

                history.add(command);
                saveHistory();
                clearEditTexts();
                collapseEditTexts();
                hideSoftKeyboard();
            }
        });
    }

    private void clearEditTexts(){
        editText1.setText("");
        editText2.setText("");
        editText3.setText("");
        editText4.setText("");
    }

    private void collapseEditTexts(){
        //editText1.setVisibility(View.GONE);
        editText2.setVisibility(View.GONE);
        editText3.setVisibility(View.GONE);
        editText4.setVisibility(View.GONE);
    }

    //http://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
    private void hideSoftKeyboard(){
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void saveHistory(){
        String jsonString = new Gson().toJson(history);
        saveStringToPreferences("history", jsonString);
    }

    private void saveStringToPreferences(String handle, String string){
        SharedPreferences preferences = getSharedPreferences(classPrefs, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(handle, string);
        editor.apply();
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

    private static void sendData(){

    }
}
