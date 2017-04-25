package com.adafruit.bluefruit.le.connect.app.OurActivities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.adafruit.bluefruit.le.connect.R;
import com.adafruit.bluefruit.le.connect.app.OurActivities.PacketWrappers.UserCommand;

import java.util.ArrayList;

/**
 * Created by michael on 4/22/17.
 */

public class CommandLineAdapter extends ArrayAdapter<UserCommand> {


    public CommandLineAdapter(Context context, ArrayList<UserCommand> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        UserCommand command = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.commandline_history_list_item, parent, false);
        }
        // Lookup view for data population
        TextView text = (TextView) convertView.findViewById(R.id.sendButton);
        TextView send = (TextView) convertView.findViewById(R.id.sendButton);
        TextView edit = (TextView) convertView.findViewById(R.id.editButton);
        // Populate the data into the template view using the data object
        text.setText(command.toStringShowLineFeeds());
        // Return the completed view to render on screen
        return convertView;
    }

}


