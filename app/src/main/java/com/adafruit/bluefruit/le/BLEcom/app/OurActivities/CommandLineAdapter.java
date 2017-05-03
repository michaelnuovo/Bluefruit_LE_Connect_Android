package com.adafruit.bluefruit.le.BLEcom.app.OurActivities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.adafruit.bluefruit.le.BLEcom.R;
import com.adafruit.bluefruit.le.BLEcom.app.OurActivities.PacketWrappers.UserCommand;

import java.util.ArrayList;

/**
 * Created by michael on 4/22/17.
 */

public class CommandLineAdapter extends ArrayAdapter<UserCommand> {

    public CommandLineAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public CommandLineAdapter(Context context, int resource, ArrayList<UserCommand> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.history_list_item, null);

        }

        UserCommand userCommand = getItem(position);

        if (userCommand != null) {

            TextView textView = (TextView) v.findViewById(R.id.text);
            TextView sendButton = (TextView) v.findViewById(R.id.sendButton);

            if (textView != null) textView.setText(userCommand.toString().replace("\n","\\n"));
            if (sendButton != null) setSendButtonListener(sendButton, userCommand);

        }

        return v;
    }

    private void setSendButtonListener(TextView sendButton, final UserCommand userCommand){
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TerminalActivity2.sendDataWithCRC(userCommand.toPacket());
            }
        });
    }

}


