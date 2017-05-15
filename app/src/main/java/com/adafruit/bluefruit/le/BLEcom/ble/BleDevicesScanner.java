package com.adafruit.bluefruit.le.BLEcom.ble;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.adafruit.bluefruit.le.BLEcom.app.Main.Activities.MainActivity;
import com.adafruit.bluefruit.le.BLEcom.app.Main.Objects.BluetoothDeviceData;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static com.adafruit.bluefruit.le.BLEcom.app.Main.Activities.MainActivity.connectedDeviceData;

public class BleDevicesScanner {
    private static final String TAG = BleDevicesScanner.class.getSimpleName();
    private static final long kScanPeriod = 20 * 1000; // scan period in milliseconds

    // Data
    private final BluetoothAdapter mBluetoothAdapter;
    private volatile boolean mIsScanning = false;
    private Handler mHandler;
    private List<UUID> mServicesToDiscover;
    private final Handler mMainThreadHandler = new Handler(Looper.getMainLooper());
    private final LeScansPoster mLeScansPoster; // imlplements runnable

    //
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                    synchronized (mLeScansPoster) {
                        // disjoint Returns true if the two specified collections have no elements in common.
                        // if there are no services, or if there are no overlapping services
                        if (mServicesToDiscover == null || !Collections.disjoint(parseUuids(scanRecord), mServicesToDiscover)) {       // only process the devices with uuids in mServicesToDiscover
                            mLeScansPoster.set(device, rssi, scanRecord);
                            mMainThreadHandler.post(mLeScansPoster); // posts the runnable
                        }
                    }
                }
            };

    // constructor
    public BleDevicesScanner(BluetoothAdapter adapter, UUID[] servicesToDiscover, BluetoothAdapter.LeScanCallback callback) {
        mBluetoothAdapter = adapter;
        mServicesToDiscover = servicesToDiscover == null ? null : Arrays.asList(servicesToDiscover);
        mLeScansPoster = new LeScansPoster(callback);

        mHandler = new Handler();
    }

    public void start(final MainActivity main) {

        // Re-scans every 20 seconds
        if (kScanPeriod > 0) {
            // Stops scanning after a pre-defined scan period.
            // post a new runnable to the handler every 20 seconds
            // the handler just call this method again every 20 second
            // and post itself to the hanlder again.
            // the idea is to reset the blue tooth adapter listener with the call back
            // every 20 seconds.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mIsScanning) {
                        Log.d(TAG, "Scan timer expired. Restart scan");
                        //ifDisconnected(main);
                        stop(); // Stops itself from scanning
                        start(main); // and then calls its self recursively to start again
                    }
                }
            }, 20000);
        }

        mIsScanning = true;
        Log.d(TAG, "start scanning");
        mBluetoothAdapter.startLeScan(mLeScanCallback);

    }

    private void ifDisconnected(MainActivity main){
        Log.v(TAG,"ifDisconnected");
        // Remove devices from scanned devices data list that have disconnected GATT servers, maybe device went out of range ect.
        // We might as well remove the GATT server from the gat server list
        // We have to use an iterator to remove elements or we can get a concurrent modification error.

        Iterator<BluetoothGatt> itGatt = BleManager.getInstance().myGattConnections.iterator();
        Iterator<BluetoothDeviceData> itData = connectedDeviceData.iterator();
        while(itData.hasNext()){
            BluetoothDeviceData data = itData.next();
            while(itGatt.hasNext()){
                BluetoothGatt gatt = itGatt.next();
                Log.v(TAG,"Addresses is "+data.device.getAddress().toString());
                Log.v(TAG,"gatt.getDevice().getAddress() is "+gatt.getDevice().getAddress().toString());
                if(data.device.getAddress().equals(gatt.getDevice().getAddress())){
                    BluetoothGatt oldGatt = BleManager.getInstance().mGatt;
                    BleManager.getInstance().mGatt = gatt;
                    Log.v(TAG,"Addresses match");
                    Log.v(TAG,"BleManager.getConnectionState is "+String.valueOf(BleManager.getConnectionState()));
                    if(BleManager.getConnectionState() == 0){
                        itData.remove();
                        itGatt.remove();
                        Log.v(TAG,"Removed device data and gat server");
                        main.removeDataFromList(data.device.getAddress(),main.mScannedDevices);
                        data.isConnected = false;
                        main.updateUI();
                    }
                    BleManager.getInstance().mGatt = oldGatt;
                } else {
                    Log.v(TAG,"Addresses do not match");
                }

            }
        }
    }

    public void stop() {
        if (mIsScanning) {
            mHandler.removeCallbacksAndMessages(null);      // cancel pending calls to stop
            mIsScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            Log.d(TAG, "stop scanning");
        }
    }

    public boolean isScanning() {
        return mIsScanning;
    }



    private static class LeScansPoster implements Runnable {
        private final BluetoothAdapter.LeScanCallback leScanCallback;

        private BluetoothDevice device;
        private int rssi;
        private byte[] scanRecord;

        private LeScansPoster(BluetoothAdapter.LeScanCallback leScanCallback) {
            this.leScanCallback = leScanCallback;
        }

        public void set(BluetoothDevice device, int rssi, byte[] scanRecord) {
            this.device = device;
            this.rssi = rssi;
            this.scanRecord = scanRecord;
        }

        @Override
        public void run() {
            leScanCallback.onLeScan(device, rssi, scanRecord);
        }
    }

    // Filtering by custom UUID is broken in Android 4.3 and 4.4, see:
    //   http://stackoverflow.com/questions/18019161/startlescan-with-128-bit-uuids-doesnt-work-on-native-android-ble-implementation?noredirect=1#comment27879874_18019161
    // This is a workaround function from the SO thread to manually parse advertisement data.
    private List<UUID> parseUuids(byte[] advertisedData) {
        List<UUID> uuids = new ArrayList<>();

        ByteBuffer buffer = ByteBuffer.wrap(advertisedData).order(ByteOrder.LITTLE_ENDIAN);
        while (buffer.remaining() > 2) {
            byte length = buffer.get();
            if (length == 0) break;

            byte type = buffer.get();
            switch (type) {
                case 0x02: // Partial list of 16-bit UUIDs
                case 0x03: // Complete list of 16-bit UUIDs
                    while (length >= 2) {
                        uuids.add(UUID.fromString(String.format("%08x-0000-1000-8000-00805f9b34fb", buffer.getShort())));
                        length -= 2;
                    }
                    break;

                case 0x06: // Partial list of 128-bit UUIDs
                case 0x07: // Complete list of 128-bit UUIDs
                    while (length >= 16) {
                        long lsb = buffer.getLong();
                        long msb = buffer.getLong();
                        uuids.add(new UUID(msb, lsb));
                        length -= 16;
                    }
                    break;

                default:
                    buffer.position(buffer.position() + length - 1);
                    break;
            }
        }

        return uuids;
    }
}