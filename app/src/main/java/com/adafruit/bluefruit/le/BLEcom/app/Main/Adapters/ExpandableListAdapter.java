package com.adafruit.bluefruit.le.BLEcom.app.Main.Adapters;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.adafruit.bluefruit.le.BLEcom.R;
import com.adafruit.bluefruit.le.BLEcom.app.Main.Activities.MainActivity;
import com.adafruit.bluefruit.le.BLEcom.app.Main.Activities.ServerInfoActivity;
import com.adafruit.bluefruit.le.BLEcom.app.Main.Objects.BluetoothDeviceData;
import com.adafruit.bluefruit.le.BLEcom.app.UriBeaconUtils;
import com.adafruit.bluefruit.le.BLEcom.ble.BleUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static android.content.Context.CLIPBOARD_SERVICE;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    MainActivity mMainActivity;
    String TAG = ExpandableListAdapter.class.getSimpleName();

    public ExpandableListAdapter(MainActivity mainActivity) {

        this.mMainActivity = mainActivity;
    }

    private ArrayList<BluetoothDeviceData> mFilteredPeripherals;

    private class GroupViewHolder {
        TextView nameTextView;
        TextView descriptionTextView;
        ImageView rssiImageView;
        TextView rssiTextView;
        Switch connectButton;
    }

    @Override
    public int getGroupCount() {
        mFilteredPeripherals = mMainActivity.mPeripheralList.filteredPeripherals(true);
        return mFilteredPeripherals.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mFilteredPeripherals.get(groupPosition);
    }

    @Override
    public Spanned getChild(int groupPosition, int childPosition) {
        BluetoothDeviceData deviceData = mFilteredPeripherals.get(groupPosition);

        String text;
        switch (deviceData.type) {
            case BluetoothDeviceData.kType_Beacon:
                text = getChildBeacon(deviceData);
                break;

            case BluetoothDeviceData.kType_UriBeacon:
                text = getChildUriBeacon(deviceData);
                break;

            default:
                text = getChildCommon(deviceData);
                break;
        }

        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(text);
        }
        return result;
    }


    private String getChildUriBeacon(BluetoothDeviceData deviceData) {
        StringBuilder result = new StringBuilder();

        String name = deviceData.getName();
        if (name != null) {
            result.append(mMainActivity.getString(R.string.scan_device_localname)).append(": <b>").append(name).append("</b><br>");
        }

        String address = deviceData.device.getAddress();
        result.append(mMainActivity.getString(R.string.scan_device_address) + ": <b>" + (address == null ? "" : address) + "</b><br>");

        String uri = UriBeaconUtils.getUriFromAdvertisingPacket(deviceData.scanRecord) + "</b><br>";
        result.append(mMainActivity.getString(R.string.scan_device_uribeacon_uri)).append(": <b>").append(uri);

        result.append(mMainActivity.getString(R.string.scan_device_txpower)).append(": <b>").append(deviceData.txPower).append("</b>");

        return result.toString();
    }


    private String getChildCommon(BluetoothDeviceData deviceData) {
        StringBuilder result = new StringBuilder();

        String name = deviceData.getName();
        if (name != null) {
            result.append(mMainActivity.getString(R.string.scan_device_localname)).append(": <b>").append(name).append("</b><br>");
        }
        String address = deviceData.device.getAddress();
        result.append(mMainActivity.getString(R.string.scan_device_address)).append(": <b>").append(address == null ? "" : address).append("</b><br>");

        StringBuilder serviceText = new StringBuilder();
        if (deviceData.uuids != null) {
            int i = 0;
            for (UUID uuid : deviceData.uuids) {
                if (i > 0) serviceText.append(", ");
                serviceText.append(uuid.toString().toUpperCase());
                i++;
            }
        }
        if (!serviceText.toString().isEmpty()) {
            result.append(mMainActivity.getString(R.string.scan_device_services)).append(": <b>").append(serviceText).append("</b><br>");
        }
        result.append(mMainActivity.getString(R.string.scan_device_txpower)).append(": <b>").append(deviceData.txPower).append("</b>");

        return result.toString();
    }

    private String getChildBeacon(BluetoothDeviceData deviceData) {
        StringBuilder result = new StringBuilder();

        String name = deviceData.getName();
        if (name != null) {
            result.append(mMainActivity.getString(R.string.scan_device_localname)).append(": <b>").append(name).append("</b><br>");
        }
        String address = deviceData.device.getAddress();
        result.append(mMainActivity.getString(R.string.scan_device_address)).append(": <b>").append(address == null ? "" : address).append("</b><br>");

        final byte[] manufacturerBytes = {deviceData.scanRecord[6], deviceData.scanRecord[5]};      // Little endan
        String manufacturer = BleUtils.bytesToHex(manufacturerBytes);

        // Check if the manufacturer is known, and replace the id for a name
        String kKnownManufacturers[] = mMainActivity.getResources().getStringArray(R.array.beacon_manufacturers_ids);
        int knownIndex = Arrays.asList(kKnownManufacturers).indexOf(manufacturer);
        if (knownIndex >= 0) {
            String kManufacturerNames[] = mMainActivity.getResources().getStringArray(R.array.beacon_manufacturers_names);
            manufacturer = kManufacturerNames[knownIndex];
        }

        result.append(mMainActivity.getString(R.string.scan_device_beacon_manufacturer)).append(": <b>").append(manufacturer == null ? "" : manufacturer).append("</b><br>");

        StringBuilder text = new StringBuilder();
        if (deviceData.uuids != null && deviceData.uuids.size() == 1) {
            UUID uuid = deviceData.uuids.get(0);
            text.append(uuid.toString().toUpperCase());
        }
        result.append(mMainActivity.getString(R.string.scan_device_uuid)).append(": <b>").append(text).append("</b><br>");

        final byte[] majorBytes = {deviceData.scanRecord[25], deviceData.scanRecord[26]};           // Big endian
        String major = BleUtils.bytesToHex(majorBytes);
        result.append(mMainActivity.getString(R.string.scan_device_beacon_major)).append(": <b>").append(major).append("</b><br>");

        final byte[] minorBytes = {deviceData.scanRecord[27], deviceData.scanRecord[28]};           // Big endian
        String minor = BleUtils.bytesToHex(minorBytes);
        result.append(mMainActivity.getString(R.string.scan_device_beacon_minor)).append(": <b>").append(minor).append("</b><br>");

        result.append(mMainActivity.getString(R.string.scan_device_txpower)).append(": <b>").append(deviceData.txPower).append("</b>");

        return result.toString();
    }


    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    // TODO list item layout_scan_item_title
    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        GroupViewHolder holder;

        View myView = mMainActivity.getLayoutInflater().inflate(R.layout.layout_scan_item_title, parent, false);
//
//            if (convertView == null) {
//                convertView = getLayoutInflater().inflate(R.layout.layout_scan_item_title, parent, false);


        holder = new GroupViewHolder();

        holder.nameTextView = (TextView) myView.findViewById(R.id.nameTextView);
        holder.descriptionTextView = (TextView) myView.findViewById(R.id.descriptionTextView);
        holder.rssiImageView = (ImageView) myView.findViewById(R.id.rssiImageView);
        holder.rssiTextView = (TextView) myView.findViewById(R.id.rssiTextView);
        holder.connectButton = (Switch) myView.findViewById(R.id.connectionSwitch);
//
//                convertView.setTag(R.string.scan_tag_id, holder);
//
//            } else {
//                holder = (GroupViewHolder) convertView.getTag(R.string.scan_tag_id);
//            }
//
//            convertView.setTag(groupPosition);
//            holder.connectButton.setTag(groupPosition);
//
//            convertView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    onClickScannedDevice(v);
//                }
//            });

//            holder.connectButton.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//
//                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                        onClickDeviceConnect(groupPosition);
//                        return true;
//                    }
//                    return false;
//                }
//            });

        //ArrayList<BluetoothDeviceData> filteredPeripherals = mPeripheralList.filteredPeripherals(false);
        //mSelectedDeviceData = mFilteredPeripherals.get(groupPosition);
        final BluetoothDeviceData deviceData = mFilteredPeripherals.get(groupPosition);

        boolean buttonState;
        buttonState = (deviceData.mConnectionState == 2) ? true : false;
        holder.connectButton.setChecked(buttonState);



        // onLongClick() - This returns a boolean to indicate whether you have consumed the
        // event and it should not be carried further. That is, return true to indicate that you
        // have handled the event and it should stop here; return false if you have not handled
        // it and/or the event should continue to any other on-click listeners.
        myView.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {

                Log.v(TAG,"Long press");

                return true;
            }
        });

        myView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.v(TAG,"on click");


                // connect
                if(deviceData.mConnectionState == 0){

                    mMainActivity.onClickDeviceConnect(groupPosition); // Connect to the ble device

//                    Intent intent = new Intent(mMainActivity, ServerInfoActivity.class);
//                    mMainActivity.startActivity(intent);

                    // disconnect
                } else {

                    mMainActivity.onClickDeviceDisconnect(groupPosition); // Disconnect from the ble device
                }
            }
        });


//            holder.connectButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                    // do something, the isChecked will be
//                    // true if the switch is in the On position
//
//                    if(isChecked == true) {
//                        onClickDeviceConnect(groupPosition); // Connect to the ble device
//                    } else {
//                        onClickDeviceDisconnect(groupPosition); // Disconnect from the ble device
//                    }
//                }
//            });

        //BluetoothDeviceData deviceData = mFilteredPeripherals.get(groupPosition);
        holder.nameTextView.setText(deviceData.getNiceName());

        holder.descriptionTextView.setVisibility(deviceData.type != BluetoothDeviceData.kType_Unknown ? View.VISIBLE : View.INVISIBLE);
        holder.descriptionTextView.setText(mMainActivity.getResources().getStringArray(R.array.scan_devicetypes)[deviceData.type]);
        holder.rssiTextView.setText(deviceData.rssi == 127 ? mMainActivity.getString(R.string.scan_device_rssi_notavailable) : String.valueOf(deviceData.rssi));

        int rrsiDrawableResource = getDrawableIdForRssi(deviceData.rssi);
        holder.rssiImageView.setImageResource(rrsiDrawableResource);

        return myView;
    }

    private int getDrawableIdForRssi(int rssi) {
        int index;
        if (rssi == 127 || rssi <= -84) {       // 127 reserved for RSSI not available
            index = 0;
        } else if (rssi <= -72) {
            index = 1;
        } else if (rssi <= -60) {
            index = 2;
        } else if (rssi <= -48) {
            index = 3;
        } else {
            index = 4;
        }

        final int kSignalDrawables[] = {
                R.drawable.signalstrength0,
                R.drawable.signalstrength1,
                R.drawable.signalstrength2,
                R.drawable.signalstrength3,
                R.drawable.signalstrength4};
        return kSignalDrawables[index];
    }

    @Override
    public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mMainActivity.getLayoutInflater().inflate(R.layout.layout_scan_item_child, parent, false);
        }

        // We don't expect many items so for clarity just find the views each time instead of using a ViewHolder
        TextView textView = (TextView) convertView.findViewById(R.id.dataTextView);
        Spanned text = getChild(groupPosition, childPosition);
        textView.setText(text);

        Button rawDataButton = (Button) convertView.findViewById(R.id.rawDataButton);
        rawDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<BluetoothDeviceData> filteredPeripherals = mMainActivity.mPeripheralList.filteredPeripherals(false);
                if (groupPosition < filteredPeripherals.size()) {
                    final BluetoothDeviceData deviceData = filteredPeripherals.get(groupPosition);
                    final byte[] scanRecord = deviceData.scanRecord;
                    final String packetText = BleUtils.bytesToHexWithSpaces(scanRecord);
                    final String clipboardLabel = mMainActivity.getString(R.string.scan_device_advertising_title);

                    new AlertDialog.Builder(mMainActivity)
                            .setTitle(R.string.scan_device_advertising_title)
                            .setMessage(packetText)
                            .setPositiveButton(android.R.string.ok, null)
                            .setNeutralButton(android.R.string.copy, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ClipboardManager clipboard = (ClipboardManager) mMainActivity.getSystemService(CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText(clipboardLabel, packetText);
                                    clipboard.setPrimaryClip(clip);
                                }
                            })
                            .show();
                }

            }
        });

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}