package com.adafruit.bluefruit.le.BLEcom.app.Main.Activities;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.adafruit.bluefruit.le.BLEcom.R;
import com.adafruit.bluefruit.le.BLEcom.app.Main.Objects.BluetoothDeviceData;
import com.adafruit.bluefruit.le.BLEcom.ble.BleManager;

import java.util.List;
import java.util.UUID;

import static android.icu.lang.UCharacter.toLowerCase;

public class ServerInfoActivity extends AppCompatActivity {

    String TAG = ServerInfoActivity.class.getSimpleName();

    Button mRefreshButton;
    ScrollView mScrollView;
    TextView mInfoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_info);

        mRefreshButton = (Button) findViewById(R.id.refreshButton);
        mScrollView = (ScrollView) findViewById(R.id.scrollView);
        mInfoView = (TextView) findViewById(R.id.infoView);

        refreshScrollView();

        mRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshScrollView();
            }
        });

    }

    private void refreshScrollView(){

        String info = "";

        for(BluetoothDeviceData data : BleManager.myConnectedDeviceData){

            info += data.getName() + "\n\n";

            BluetoothGatt gatt = BleManager.myConnectedDeviceData.get(0).connection;
            List<BluetoothGattService> services = gatt.getServices();

            for(BluetoothGattService service : services){

                info += "service\n";
                info += "   Name : " + getUuidName(service.getUuid()) + "\n";
                info += "   UUID : " + service.getUuid().toString() + "\n";
                String serviceType = (service.getType() == 0) ? "Primary?" : "Secondary?";
                info += "   Type : " + serviceType + "\n";

                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();

                for(BluetoothGattCharacteristic characteristic : characteristics){

                    int p = characteristic.getPermissions();
                    String permissions = (p == 0) ? "Readable?" : (p == 1) ? "Writable?" : "Readable & Writable?";

                    info += "      characteristic\n";
                    info += "         Name : " + getUuidName(characteristic.getUuid()) + "\n";
                    info += "         UUID : " + characteristic.getUuid().toString() + "\n";
                    info += "         value : " + characteristic.getValue() + "\n";
                    info += "         value : " + permissions + "\n";

                    List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
                    for(BluetoothGattDescriptor descriptor : descriptors){

                        info += "            descriptor\n";
                        info += "               Name : " + getUuidName(descriptor.getUuid()) + "\n";
                        info += "               UUID : " + descriptor.getUuid().toString() + "\n";
                        info += "               value : "+descriptor.getValue() + "\n";
                    }
                }
            }

            info += "\n";
        }

        Log.v(TAG,info);
        mInfoView.setText(info);
    }

    private String getUuidName(UUID uuid) {

        String uuidStr = uuid.toString();

        String name = null;

        switch(uuidStr) {
            case "00001800-0000-1000-8000-00805f9b34fb" : name = "Generic Access";
                break;
            case "00002a00-0000-1000-8000-00805f9b34fb" : name = "Device Name";
                break;
            case "00002a01-0000-1000-8000-00805f9b34fb" : name = "Appearance";
                break;
            case "00001801-0000-1000-8000-00805f9b34fb" : name = "Generic Attribute Service - (Main Activity Class)";
                break;
            case "00002A05-0000-1000-8000-00805F9b34fb" : name = "Service Change Characteristic - (Main Activity Class)";
                break;
            case "00002902-0000-1000-8000-00805f9b34fb" : name = "Characteristic Config - (BleGattExecutor Class)";
                break;
            case "6e400001-b5a3-f393-e0a9-e50e24dcca9e" : name = "Service - (UartInterface Activity Class)";
                break;
            case "6e400003-b5a3-f393-e0a9-e50e24dcca9e" : name = "Receiving - (UartInterface Activity Class)";
                break;
            case "6e400002-b5a3-f393-e0a9-e50e24dcca9e" : name = "Transmitting - (UartInterface Activity Class)";
                break;
            case "00001530-1212-efde-1523-785feabcd123" : name = "Device Firmware Upgrade - (UartInterface Activity Class)";

        }

        if(name == null)
            return "Unknown";

        return name;
    }
}

