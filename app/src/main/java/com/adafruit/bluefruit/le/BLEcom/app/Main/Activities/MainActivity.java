package com.adafruit.bluefruit.le.BLEcom.app.Main.Activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.nsd.NsdManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.adafruit.bluefruit.le.BLEcom.R;
import com.adafruit.bluefruit.le.BLEcom.app.BeaconActivity;
import com.adafruit.bluefruit.le.BLEcom.app.CommonHelpActivity;
import com.adafruit.bluefruit.le.BLEcom.app.ControllerActivity;
import com.adafruit.bluefruit.le.BLEcom.app.InfoActivity;
import com.adafruit.bluefruit.le.BLEcom.app.Main.Adapters.ExpandableListAdapter;
import com.adafruit.bluefruit.le.BLEcom.app.MainHelpActivity;
import com.adafruit.bluefruit.le.BLEcom.app.Main.Objects.BluetoothDeviceData;
import com.adafruit.bluefruit.le.BLEcom.app.Main.Packets.PacketUtils;
import com.adafruit.bluefruit.le.BLEcom.app.PinIOActivity;
import com.adafruit.bluefruit.le.BLEcom.app.UartActivity;
import com.adafruit.bluefruit.le.BLEcom.app.UartInterfaceActivity;
import com.adafruit.bluefruit.le.BLEcom.app.UriBeaconUtils;
import com.adafruit.bluefruit.le.BLEcom.app.neopixel.NeopixelActivity;
import com.adafruit.bluefruit.le.BLEcom.app.settings.SettingsActivity;
import com.adafruit.bluefruit.le.BLEcom.app.update.FirmwareUpdater;
import com.adafruit.bluefruit.le.BLEcom.app.update.LooperThread;
import com.adafruit.bluefruit.le.BLEcom.app.update.ReleasesParser;
import com.adafruit.bluefruit.le.BLEcom.ble.BleDevicesScanner;
import com.adafruit.bluefruit.le.BLEcom.ble.BleManager;
import com.adafruit.bluefruit.le.BLEcom.ble.BleUtils;
import com.adafruit.bluefruit.le.BLEcom.ui.utils.DialogUtils;
import com.adafruit.bluefruit.le.BLEcom.ui.utils.ExpandableHeightExpandableListView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;


// Testing our first commit
// Testing our fist commit to preview branch

public class MainActivity extends UartInterfaceActivity implements

        // Interfaces

        BleManager.BleManagerListener, // see "ble" package
        BleUtils.ResetBluetoothAdapterListener, // ble.BleUtils
        FirmwareUpdater.FirmwareUpdaterListener { // com.adafruit.bluefruit.le.connect.app.update.FirmwareUpdater;

    // Constants
    private final static String TAG = MainActivity.class.getSimpleName();
    private final static long kMinDelayToUpdateUI = 200;    // in milliseconds
    private static final String kGenericAttributeService = "00001801-0000-1000-8000-00805F9B34FB";
    private static final String kServiceChangedCharacteristic = "00002A05-0000-1000-8000-00805F9B34FB";

    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;

    private final static String kPreferences = "MainActivity_prefs";
    //private final static String kPreferences = "UartActivity_prefs";
    private final static String kPreferences_filtersPanelOpen = "filtersPanelOpen";

    // Components string IDs are of type int
    // see showChooseDeviceServiceDialog line 651 (ctr + G in windows)
    private final static int kComponentsNameIds[] = {
            //R.string.scan_connectservice_info,       // Info
            R.string.scan_connectservice_uart,       // UART
            //R.string.scan_connectservice_pinio,      // Pin I/O
            R.string.scan_connectservice_controller, // Controller
            //R.string.scan_connectservice_beacon,     // Beacon
            //R.string.scan_connectservice_neopixel,   // Neopixel
    };

    /**
     *     <string name="scan_connectservice_info">Info</string>
     <string name="scan_connectservice_uart">UART</string>
     <string name="scan_connectservice_pinio">Pin I/O</string>
     <string name="scan_connectservice_controller">Controller</string>
     <string name="scan_connectservice_beacon">Beacon</string>
     <string name="scan_connectservice_neopixel">Neopixel</string>
     */

    // Activity request codes (used for onActivityResult)
    private static final int kActivityRequestCode_EnableBluetooth = 1;
    private static final int kActivityRequestCode_Settings = 2;
    private static final int kActivityRequestCode_ConnectedActivity = 3;

    // UI
    private ExpandableHeightExpandableListView mScannedDevicesListView;
    private ExpandableListAdapter mScannedDevicesAdapter;
    private Button mScanButton;
    private long mLastUpdateMillis;
    private TextView mNoDevicesTextView;
    private ScrollView mDevicesScrollView;
    private SwipeRefreshLayout mSwipeRefreshLayout; // The SwipeRefreshLayout should be used whenever
                                                    // the user can refresh the contents of a view via
                                                    // a vertical swipe gesture.
    private AlertDialog mConnectingDialog;
    private View mFiltersPanelView;
    private ImageView mFiltersExpandImageView;
    private ImageButton mFiltersClearButton;
    private TextView mFiltersTitleTextView;
    private EditText mFiltersNameEditText;
    private SeekBar mFiltersRssiSeekBar; // A SeekBar is an extension of ProgressBar that adds a draggable thumb.
    private TextView mFiltersRssiValueTextView;
    private CheckBox mFiltersUnnamedCheckBox;
    private CheckBox mFiltersUartCheckBox;

    private Button p1;
    private Button p2;
    private Button p4;
    private Button p8;
    private Button userCommands;
    private Button androidClient;
    private Button sensorData;
    private Button patternPicker;

    private Switch mConnectButton;

    // Data
    private BleManager xmBleManager;
    private boolean mIsScanPaused = true;
    private BleDevicesScanner mScanner;
    private FirmwareUpdater mFirmwareUpdater;
    public PeripheralList mPeripheralList;

    public ArrayList<BluetoothDeviceData> mScannedDevices;
    private BluetoothDeviceData mSelectedDeviceData;
    private Class<?> mComponentToStartWhenConnected;
    private boolean mShouldEnableWifiOnQuit = false;
    private String mLatestCheckedDeviceAddress;

    private DataFragment mRetainedDataFragment;

    NsdManager.DiscoveryListener mDiscoveryListener;

    private LooperThread mLooper;

    private Handler mHandler;


    // Michael's variables
    public static Context ctx;
    public static ArrayList<BluetoothDeviceData> connectedDeviceData = new ArrayList<>();

    private void testViews(){

//                Intent intent = new Intent(MainActivity.this, ControllerActivity.class);
//        startActivityForResult(intent, 2);


//
//        Intent intent = new Intent(MainActivity.this, ColorPickerActivity1Color.class);
//        startActivityForResult(intent, 2);
//
//          Intent intent = new Intent(MainActivity.this, ColorPickerActivity2Colors.class);
//          startActivityForResult(intent, 2);
//
//          Intent intent = new Intent(MainActivity.this, ColorPickerActivity4Colors.class);
//          startActivityForResult(intent, 2);

//        Intent intent = new Intent(MainActivity.this, ColorPickerActivity8Colors.class);
//        startActivityForResult(intent, 2);

//
//        Intent intent = new Intent(MainActivity.this, TerminalActivity.class);
//        startActivityForResult(intent, 2);

                Intent intent = new Intent(MainActivity.this, TerminalActivity2.class);
        startActivityForResult(intent, 2);
//
//        Intent intent = new Intent(MainActivity.this, SocketActivity.class);
//        startActivityForResult(intent, 2);


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ctx = this;

        //testViews();
//        initializeDiscoveryListener();
        //discoverServices();

        // Init variables
        mBleManager = BleManager.getInstance(this);
        //mBleManager.setBleListener(this);
        //mBleManager.setBleListener(new UartActivity());
        //onServicesDiscovered();

        restoreRetainedDataFragment();
        mPeripheralList = new PeripheralList();

        // UI
        p1 = (Button) findViewById(R.id.p1);
        p2 = (Button) findViewById(R.id.p2);
        p4 = (Button) findViewById(R.id.p4);
        p8 = (Button) findViewById(R.id.p8);
        userCommands = (Button) findViewById(R.id.userCommands);
        androidClient = (Button) findViewById(R.id.androidClient);
        sensorData = (Button) findViewById(R.id.sensorData);
        patternPicker = (Button) findViewById(R.id.patternsActivity);

        p1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ColorPickerActivity1Color.class);
                startActivityForResult(intent, 2);
            }
        });

        p2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ColorPickerActivity2Colors.class);
                startActivityForResult(intent, 2);
            }
        });

        p4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ColorPickerActivity4Colors.class);
                startActivityForResult(intent, 2);
            }
        });

        p8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ColorPickerActivity8Colors.class);
                startActivityForResult(intent, 2);
            }
        });

        userCommands.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TerminalActivity2.class);
                startActivityForResult(intent, 2);
            }
        });

        androidClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SocketActivity.class);
                startActivityForResult(intent, 2);
            }
        });

        sensorData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SensorDataActivity.class);
                startActivityForResult(intent, 2);
            }
        });

        patternPicker.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PatternPickerActivity.class);
                startActivityForResult(intent, 2);
            }
        });

        // TODO setting the adapter to the expandable list view
        mScannedDevicesListView = (ExpandableHeightExpandableListView) findViewById(R.id.scannedDevicesListView);
        mScannedDevicesAdapter = new ExpandableListAdapter(this);
        mScannedDevicesListView.setAdapter(mScannedDevicesAdapter);
        mScannedDevicesListView.setExpanded(true);

        mScannedDevicesListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
            }
        });

        mScanButton = (Button) findViewById(R.id.scanButton);

        mNoDevicesTextView = (TextView) findViewById(R.id.nodevicesTextView);
        mDevicesScrollView = (ScrollView) findViewById(R.id.devicesScrollView);
        mDevicesScrollView.setVisibility(View.GONE);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        // Sets a listener that detects a swipe movement.
        mSwipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {

                mScannedDevices.clear(); // <-- the list is being cleared TODO mScannedDevices.clear()


                startScan(null);

                // Calls postDelayed method and passes it a Runnable.
                // Runnable is executed 500 milliseconds later.
                mSwipeRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // If the listener determines there should not be a refresh, it must call
                        // setRefreshing(false) to cancel any visual indication of a refresh
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 500);
            }
        });

        mFiltersPanelView = findViewById(R.id.filtersExpansionView);
        mFiltersExpandImageView = (ImageView) findViewById(R.id.filtersExpandImageView);
        mFiltersClearButton = (ImageButton) findViewById(R.id.filtersClearButton);
        mFiltersTitleTextView = (TextView) findViewById(R.id.filtersTitleTextView);
        mFiltersNameEditText = (EditText) findViewById(R.id.filtersNameEditText);
        mFiltersNameEditText.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                String text = s.toString();
                mPeripheralList.setFilterName(text);
                updateFilters();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        mFiltersRssiSeekBar = (SeekBar) findViewById(R.id.filtersRssiSeekBar);
        mFiltersRssiSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int rssiValue = -seekBar.getProgress();
                mPeripheralList.setFilterRssiValue(rssiValue);
                updateRssiValue();
                updateFilters();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mFiltersRssiValueTextView = (TextView) findViewById(R.id.filtersRssiValueTextView);
        mFiltersUnnamedCheckBox = (CheckBox) findViewById(R.id.filtersUnnamedCheckBox);
        mFiltersUnnamedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mPeripheralList.setFilterUnnamedEnabled(isChecked);
                updateFilters();
            }
        });

        mFiltersUartCheckBox = (CheckBox) findViewById(R.id.filtersUartCheckBox);
        mFiltersUartCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mPeripheralList.setFilterOnlyUartEnabled(isChecked);
                updateFilters();
            }
        });

        // Filters
        SharedPreferences preferences = getSharedPreferences(kPreferences, MODE_PRIVATE);
        boolean filtersIsPanelOpen = preferences.getBoolean(kPreferences_filtersPanelOpen, false);
        openFiltersPanel(filtersIsPanelOpen, false);
        updateFiltersTitle();
        mFiltersNameEditText.setText(mPeripheralList.getFilterName());
        setRssiSliderValue(mPeripheralList.getFilterRssiValue());
        mFiltersUnnamedCheckBox.setChecked(mPeripheralList.isFilterUnnamedEnabled());
        mFiltersUartCheckBox.setChecked(mPeripheralList.isFilterOnlyUartEnabled());

        // Setup when activity is created for the first time
        if (savedInstanceState == null) {
            // Read preferences
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            boolean autoResetBluetoothOnStart = sharedPreferences.getBoolean("pref_resetble", false);
            boolean disableWifi = sharedPreferences.getBoolean("pref_disableWifi", false);
            boolean updatesEnabled = sharedPreferences.getBoolean("pref_updatesenabled", true);

            // Update SoftwareUpdateManager
            if (updatesEnabled) {
                mFirmwareUpdater = new FirmwareUpdater(this, this);
                mFirmwareUpdater.refreshSoftwareUpdatesDatabase();
            }

            // Turn off wifi
            if (disableWifi) {
                final boolean isWifiEnabled = BleUtils.isWifiEnabled(this);
                if (isWifiEnabled) {
                    BleUtils.enableWifi(false, this);
                    mShouldEnableWifiOnQuit = true;
                }
            }

            // Check if bluetooth adapter is available
            final boolean wasBluetoothEnabled = manageBluetoothAvailability();
            final boolean areLocationServicesReadyForScanning = manageLocationServiceAvailabilityForScanning();

            // Reset bluetooth
            if (autoResetBluetoothOnStart && wasBluetoothEnabled && areLocationServicesReadyForScanning) {
                BleUtils.resetBluetoothAdapter(this, this);
            }
        }

        // Request Bluetooth scanning permissions
        requestLocationPermissionIfNeeded();

        // Start services
        //onServicesDiscovered();

    }



    // TODO onResume()
    @Override
    public void onResume() {
        super.onResume();

        // Set listener
        //mBleManager.setBleListener(this);

        // Autostart scan
        Log.v(TAG,"onResume()");
        autostartScan();

        // Update UI
        updateUI();

        Log.v("TAG","On Resume");

        Log.v("TAG","BleManager.getInstance(this).mDeviceAddress is "+BleManager.getInstance(this).mDeviceAddress);
    }

    private void autostartScan() {
        Log.v(TAG,"autostartScan() ");
        if (BleUtils.getBleStatus(this) == BleUtils.STATUS_BLE_ENABLED) {
            // If was connected, disconnect
            //mBleManager.disconnect(); // Disconnects the gatt service

            // Force restart scanning
            if (mScannedDevices != null) {      // Fixed a weird bug when resuming the app (this was null on very rare occasions even if it should not be)
                mScannedDevices.clear();
            }
            startScan(null);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_help) {
            startHelp();
            return true;
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, SettingsActivity.class);
            startActivityForResult(intent, kActivityRequestCode_Settings);
            return true;
        } else if (id == R.id.action_licenses) {
            Intent intent = new Intent(this, CommonHelpActivity.class);
            intent.putExtra("title", getString(R.string.licenses_title));
            intent.putExtra("help", "licenses.html");
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onPause() {
        // Stop scanning
        if (mScanner != null && mScanner.isScanning()) {
            mIsScanPaused = true;
            stopScanning();
        }

        super.onPause();
    }

    public void onStop() {
        if (mConnectingDialog != null) {
            mConnectingDialog.cancel();
            mConnectingDialog = null;
        }

        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (mShouldEnableWifiOnQuit) {
            mShouldEnableWifiOnQuit = false;
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.settingsaction_confirmenablewifi_title))
                    .setMessage(getString(R.string.settingsaction_confirmenablewifi_message))
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d(TAG, "enableNotification wifi");
                            BleUtils.enableWifi(true, MainActivity.this);
                            MainActivity.super.onBackPressed();
                        }

                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MainActivity.super.onBackPressed();
                        }

                    })
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        // Stop ble adapter reset if in progress
        BleUtils.cancelBluetoothAdapterReset(this);

        // Retain data
        saveRetainedDataFragment();

        // Clean
        if (mConnectingDialog != null) {
            mConnectingDialog.cancel();
        }

        super.onDestroy();
    }

    // region Permissions
    @TargetApi(Build.VERSION_CODES.M)
    private void requestLocationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can scan for Bluetooth peripherals");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
                    }
                });
                builder.show();
            }
        }
    }

    // ActivityCompat.OnRequestPermissionsResultCallback
    // https://developer.android.com/reference/android/support/v4/app/ActivityCompat.OnRequestPermissionsResultCallback.html
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_FINE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Location permission granted");
                    // Autostart scan
                    autostartScan();
                    // Update UI
                    updateUI();
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Bluetooth Scanning not available");
                    builder.setMessage("Since location access has not been granted, the app will not be able to scan for Bluetooth peripherals");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                break;
            }
            default:
                break;
        }
    }

    // endregion


    // region Filters
    private void openFiltersPanel(final boolean isOpen, boolean animated) {
        SharedPreferences.Editor preferencesEditor = getSharedPreferences(kPreferences, MODE_PRIVATE).edit();
        preferencesEditor.putBoolean(kPreferences_filtersPanelOpen, isOpen);
        preferencesEditor.apply();

        mFiltersExpandImageView.setImageResource(isOpen ? R.drawable.ic_expand_less_black_24dp : R.drawable.ic_expand_more_black_24dp);

        /*
        float paddingTop = MetricsUtils.convertDpToPixel(this, (float) (isOpen ? 200 : 44));
        mScannedDevicesListView.setPadding(0, (int) paddingTop, 0, 0);

        mFiltersPanelView.setVisibility(View.VISIBLE);
        HeightAnimation heightAnim = new HeightAnimation(mFiltersPanelView, isOpen?0:200, isOpen?200:0);
        heightAnim.setDuration(300);
        mFiltersPanelView.startAnimation(heightAnim);
*/

        mFiltersPanelView.setVisibility(isOpen ? View.VISIBLE : View.GONE);

        mFiltersPanelView.animate()
                .alpha(isOpen ? 1.0f : 0)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mFiltersPanelView.setVisibility(isOpen ? View.VISIBLE : View.GONE);
                    }
                });

    }

    public void onClickExpandFilters(View view) {
        SharedPreferences preferences = getSharedPreferences(kPreferences, MODE_PRIVATE);
        boolean filtersIsPanelOpen = preferences.getBoolean(kPreferences_filtersPanelOpen, false);

        openFiltersPanel(!filtersIsPanelOpen, true);
    }

    public void onClickRemoveFilters(View view) {
        mPeripheralList.setDefaultFilters();
        mFiltersNameEditText.setText(mPeripheralList.getFilterName());
        setRssiSliderValue(mPeripheralList.getFilterRssiValue());
        mFiltersUnnamedCheckBox.setChecked(mPeripheralList.isFilterUnnamedEnabled());
        mFiltersUartCheckBox.setChecked(mPeripheralList.isFilterOnlyUartEnabled());
        updateFilters();
    }

    public void onClickFilterNameSettings(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                boolean processed = true;
                switch (item.getItemId()) {
                    case R.id.scanfilter_name_contains:
                        mPeripheralList.setFilterNameExact(false);
                        break;
                    case R.id.scanfilter_name_exact:
                        mPeripheralList.setFilterNameExact(true);
                        break;
                    case R.id.scanfilter_name_sensitive:
                        mPeripheralList.setFilterNameCaseInsensitive(false);
                        break;
                    case R.id.scanfilter_name_insensitive:
                        mPeripheralList.setFilterNameCaseInsensitive(true);
                        break;
                    default:
                        processed = false;
                        break;
                }
                updateFilters();
                return processed;
            }
        });
        MenuInflater inflater = popup.getMenuInflater();
        Menu menu = popup.getMenu();
        inflater.inflate(R.menu.menu_scan_filters_name, menu);
        final boolean isFilterNameExact = mPeripheralList.isFilterNameExact();
        menu.findItem(isFilterNameExact ? R.id.scanfilter_name_exact : R.id.scanfilter_name_contains).setChecked(true);
        final boolean isFilterNameCaseInsensitive = mPeripheralList.isFilterNameCaseInsensitive();
        menu.findItem(isFilterNameCaseInsensitive ? R.id.scanfilter_name_insensitive : R.id.scanfilter_name_sensitive).setChecked(true);
        popup.show();
    }


    private void updateFiltersTitle() {
        final String filtersTitle = mPeripheralList.filtersDescription();
        mFiltersTitleTextView.setText(filtersTitle != null ? String.format(Locale.ENGLISH, getString(R.string.scan_filters_title_filter_format), filtersTitle) : getString(R.string.scan_filters_title_nofilter));
        mFiltersClearButton.setVisibility(mPeripheralList.isAnyFilterEnabled() ? View.VISIBLE : View.GONE);
    }

    private void updateFilters() {
        updateFiltersTitle();
        mScannedDevicesAdapter.notifyDataSetChanged();
    }

    private void setRssiSliderValue(int value) {
        mFiltersRssiSeekBar.setProgress(-value);
        updateRssiValue();
    }

    private void updateRssiValue() {
        final int value = -mFiltersRssiSeekBar.getProgress();
        mFiltersRssiValueTextView.setText(String.format(Locale.ENGLISH, getString(R.string.scan_filters_rssi_value_format), value));
    }

    // endregion

    private void resumeScanning() {
        if (mIsScanPaused) {
            startScan(null);
            mIsScanPaused = mScanner == null;
        }
    }



    private boolean manageBluetoothAvailability() {
        boolean isEnabled = true;

        // Check Bluetooth HW status
        int errorMessageId = 0;
        final int bleStatus = BleUtils.getBleStatus(getBaseContext());
        switch (bleStatus) {
            case BleUtils.STATUS_BLE_NOT_AVAILABLE:
                errorMessageId = R.string.dialog_error_no_ble;
                isEnabled = false;
                break;
            case BleUtils.STATUS_BLUETOOTH_NOT_AVAILABLE: {
                errorMessageId = R.string.dialog_error_no_bluetooth;
                isEnabled = false;      // it was already off
                break;
            }
            case BleUtils.STATUS_BLUETOOTH_DISABLED: {
                isEnabled = false;      // it was already off
                // if no enabled, launch settings dialog to enable it (user should always be prompted before automatically enabling bluetooth)
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, kActivityRequestCode_EnableBluetooth);
                // execution will continue at onActivityResult()
                break;
            }
        }
        if (errorMessageId != 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            AlertDialog dialog = builder.setMessage(errorMessageId)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            DialogUtils.keepDialogOnOrientationChanges(dialog);
        }

        return isEnabled;
    }

    private boolean manageLocationServiceAvailabilityForScanning() {

        boolean areLocationServiceReady = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {        // Location services are only needed to be enabled from Android 6.0
            int locationMode = Settings.Secure.LOCATION_MODE_OFF;
            try {
                locationMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            areLocationServiceReady = locationMode != Settings.Secure.LOCATION_MODE_OFF;

            if (!areLocationServiceReady) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                AlertDialog dialog = builder.setMessage(R.string.dialog_error_nolocationservices_requiredforscan_marshmallow)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                DialogUtils.keepDialogOnOrientationChanges(dialog);
            }
        }

        return areLocationServiceReady;
    }


    private void startHelp() {
        // Launch app help activity
        Intent intent = new Intent(this, MainHelpActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == kActivityRequestCode_ConnectedActivity) {
            if (resultCode < 0) {
                Toast.makeText(this, R.string.scan_unexpecteddisconnect, Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == kActivityRequestCode_EnableBluetooth) {
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth was enabled, resume scanning
                resumeScanning();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                AlertDialog dialog = builder.setMessage(R.string.dialog_error_no_bluetooth)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                DialogUtils.keepDialogOnOrientationChanges(dialog);

            }
        } else if (requestCode == kActivityRequestCode_Settings) {
            // Return from activity settings. Update app behaviour if needed
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            boolean updatesEnabled = sharedPreferences.getBoolean("pref_updatesenabled", true);
            if (updatesEnabled) {
                mLatestCheckedDeviceAddress = null;
                mFirmwareUpdater.refreshSoftwareUpdatesDatabase();
            } else {
                mFirmwareUpdater = null;
            }
        }
    }

    private void showGettingUpdateInfoState() {
        showConnectionStatus(false);
        showStatusDialog(true, R.string.scan_gettingupdateinfo);
    }



    // region Actions
    public void onClickScannedDevice(final View view) {
        final int groupPosition = (Integer) view.getTag();

        if (mScannedDevicesListView.isGroupExpanded(groupPosition)) {
            mScannedDevicesListView.collapseGroup(groupPosition);
        } else {
            mScannedDevicesListView.expandGroup(groupPosition, true);

            // Force scrolling to view the children
            mDevicesScrollView.post(new Runnable() {
                @Override
                public void run() {
                    mScannedDevicesListView.scrollToGroup(groupPosition, view, mDevicesScrollView);
                }
            });
        }
    }

    // TODO onClickDeviceConnect
    public void onClickDeviceConnect(int scannedDeviceIndex) {
        boolean printFlag = true;
        if(printFlag) Log.v("TAG","onClickDeviceConnect()");

        //stopScanning();// Stop scanning once connected to a device

        ArrayList<BluetoothDeviceData> filteredPeripherals = mPeripheralList.filteredPeripherals(false);

        // scannedDeviceIndex corresponds to the list index on the devices list drop down
        // this comparison just makes sure it's not out of bounds for some reason
        if (scannedDeviceIndex < filteredPeripherals.size()) {
            if(printFlag) Log.v("TAG","scannedDeviceIndex < filteredPeripherals.size() is true");
            BluetoothDeviceData datum = filteredPeripherals.get(scannedDeviceIndex); // BluetoothDeviceData wraps a device of type BluetoothDevice device
            //BluetoothDevice device = datum.device; // BluetoothDevice is defined in the Android API

            mBleManager.setBleListener(MainActivity.this);           // Force set listener (could be still checking for updates...)

            if (datum.type == BluetoothDeviceData.kType_Uart) {      // if is uart, show all the available activities (universal asynchronous receiver/transmitter)
                if(printFlag) Log.v("TAG","Connecting to UART device...");
                connect(datum);
            } else {                          // if no uart, then go directly to info
                if(printFlag) Log.d(TAG, "No UART service found. Go to InfoActivity");
                mComponentToStartWhenConnected = InfoActivity.class;
                connect(datum);
            }
        } else {
            if(printFlag) Log.w(TAG, "onClickDeviceConnect index does not exist: " + scannedDeviceIndex);
        }
    }

    public void onClickDeviceDisconnect(int scannedDeviceIndex){
        Log.v(TAG,"Disconnecting device");
        ArrayList<BluetoothDeviceData> filteredPeripherals = mPeripheralList.filteredPeripherals(false);
        BluetoothDeviceData deviceToDisconnect = filteredPeripherals.get(scannedDeviceIndex);
        BluetoothDevice device = deviceToDisconnect.device;
//        for(BluetoothGatt gatt : BleManager.getInstance().myGattConnections)
//            if(device.getAddress() == gatt.getDevice().getAddress()){
//                gatt.close();
//                BleManager.getInstance().myGattConnections.remove(gatt);
//            }
        Iterator<BluetoothDeviceData> it = BleManager.myConnectedDeviceData.iterator();
        while (it.hasNext()) {
            BluetoothDeviceData data = it.next();
            if(data.device.getAddress().equals(device.getAddress())){
                data.connection.close();
                mScannedDevices.remove(deviceToDisconnect);
                it.remove();
                Log.v(TAG,"GATT connection closed");
            }
        }
        //deviceToDisconnect.mConnectionState = 2;
        //connectedDeviceData.remove(deviceToDisconnect);
        updateUI();
    }

    private void connect(BluetoothDeviceData datum) {
        BluetoothDevice device = datum.device;
        boolean isConnecting = mBleManager.connect(this, datum);
        Log.d(TAG, "device.getAddress() is"+device.getAddress());
        if (isConnecting) {
            showConnectionStatus(true);

        }

        if(BleManager.getConnectionState() > 0) {
            addConnectedDeviceData(datum); // Add it if it's connecting or connected
            datum.mConnectionState = 2;
            updateUI(); // need to update the connection button
            listenForDeadConnections(); // listenForDeadConnections this "background service" one a device is connected.
            //onServicesDiscovered();
//
//            super.onServicesDiscovered();
//            enableRxNotifications();

            mBleManager.setBleListener(this);

//            Intent intent = new Intent(MainActivity.this, UartActivity.class);
//            startActivityForResult(intent, 2);
        }
        // TODO open chat activity


    }

    private void listenForDeadConnections(){ // TODO listenForDeadConnections()

        if(mHandler == null) mHandler = new Handler();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Log.v(TAG,"Executing message");
                removeDeadConnectionsAndUpdateUi();
                if(connectedDeviceData.size() > 0) listenForDeadConnections(); // Stops calling itself when the connected devices list is size zero
            }
        }, 2000);
    }

    private void removeDeadConnectionsAndUpdateUi(){

        Iterator<BluetoothDeviceData> itDatum = BleManager.myConnectedDeviceData.iterator();
        while(itDatum.hasNext()){
            BluetoothDeviceData datum = itDatum.next();
            //BluetoothGatt oldGatt = BleManager.getInstance().mGatt;
           // BleManager.getInstance().mGatt = datum.connection;
            if(datum.mConnectionState == 0){
                Log.v(TAG,"Connections states of "+datum.device.getAddress()+"is "+String.valueOf(BleManager.getConnectionState() ));
                removeDataFromList(datum.device.getAddress(),mScannedDevices);
                itDatum.remove();
                updateUI();
            }
           // BleManager.getInstance().mGatt = oldGatt;
        }

        /**
        Iterator<BluetoothGatt> itGatt = BleManager.getInstance().myGattConnections.iterator();
        Iterator<BluetoothDeviceData> itData = connectedDeviceData.iterator();
        while(itData.hasNext()){
            BluetoothDeviceData data = itData.next();
            while(itGatt.hasNext()){
                BluetoothGatt gatt = itGatt.next();
                //Log.v(TAG,"Addresses is "+data.device.getAddress().toString());
                //Log.v(TAG,"gatt.getDevice().getAddress() is "+gatt.getDevice().getAddress().toString());
                if(data.device.getAddress().equals(gatt.getDevice().getAddress())){
                    BluetoothGatt oldGatt = BleManager.getInstance().mGatt;
                    BleManager.getInstance().mGatt = gatt;
                    //Log.v(TAG,"Addresses match");
                    //Log.v(TAG,"BleManager.getConnectionState is "+String.valueOf(BleManager.getConnectionState()));
                    if(BleManager.getConnectionState() == 0){
                        itData.remove();
                        itGatt.remove();
                        //Log.v(TAG,"Removed device data and gat server");
                        removeDataFromList(data.device.getAddress(),mScannedDevices);
                        data.isConnected = false;
                        updateUI();
                    }
                    BleManager.getInstance().mGatt = oldGatt;
                } else {
                    //Log.v(TAG,"Addresses do not match");
                }

            }
        }**/
    }

    private void addConnectedDeviceData(BluetoothDeviceData datum){
        datum.mConnectionState = 2; // Set toggle state to true
        connectedDeviceData.add(datum); // Add device data to list of connected device data
        Log.v(TAG,"Adding connected device from connected devices list");
        Log.v(TAG,"Connected devices list has " + String.valueOf(connectedDeviceData.size()) + " devices");

    }

    // Shows status dialog
    private void showConnectionStatus(boolean enable) {
        showStatusDialog(enable, R.string.scan_connecting);
    }


    private void showStatusDialog(boolean show, int stringId) {
        if (show) {

            // Remove if a previous dialog was open (maybe because was clicked 2 times really quick)
            if (mConnectingDialog != null) {
                mConnectingDialog.cancel();
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(stringId);

            // Show dialog
            mConnectingDialog = builder.create();
            mConnectingDialog.setCanceledOnTouchOutside(false);

            mConnectingDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        mBleManager.disconnect();
                        mConnectingDialog.cancel();
                    }
                    return true;
                }
            });
            mConnectingDialog.show();
        } else {
            if (mConnectingDialog != null) {
                mConnectingDialog.cancel();
            }
        }
    }




    // We choose one bluetooth device, and then we choose which component we want to connect with
    private void showChooseDeviceServiceDialog(final BluetoothDeviceData deviceData) {

        // Get an AlterDialog box builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Get a string to use as a title for the dialog box
        String title = String.format(getString(R.string.scan_connectto_dialog_title_format), deviceData.getNiceName());

        // Creates an array items of length kComponentsNameIds.length
        String[] items = new String[kComponentsNameIds.length];

        // Populates the array with string value corresponding to the string IDs
        for (int i = 0; i < kComponentsNameIds.length; i++)
            items[i] = getString(kComponentsNameIds[i]);

        // Method chain
        // Set the title on the builder
        // Set list items
        // Pass in an event listener to be set on items
        // Set the component activity to listenForDeadConnections
        builder.setTitle(title)
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (kComponentsNameIds[which]) {
                            case R.string.scan_connectservice_info: {          // Info
                                mComponentToStartWhenConnected = InfoActivity.class;
                                break;
                            }
                            case R.string.scan_connectservice_uart: {           // Uart
                                mComponentToStartWhenConnected = UartActivity.class;
                                break;
                            }
                            case R.string.scan_connectservice_pinio: {        // PinIO
                                mComponentToStartWhenConnected = PinIOActivity.class;
                                break;
                            }
                            case R.string.scan_connectservice_controller: {    // Controller
                                mComponentToStartWhenConnected = ControllerActivity.class;
                                break;
                            }
                            case R.string.scan_connectservice_beacon: {        // Beacon
                                mComponentToStartWhenConnected = BeaconActivity.class;
                                break;
                            }
                            case R.string.scan_connectservice_neopixel: {       // Neopixel
                                mComponentToStartWhenConnected = NeopixelActivity.class;
                                break;
                            }
                        }

                        if (mComponentToStartWhenConnected != null) {
                            connect(deviceData);            // First connect to the device, and when connected go to selected activity
                        }
                    }
                });

        // Show dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void onClickScan(View view) {
        boolean isScanning = mScanner != null && mScanner.isScanning();
        if (isScanning) {
            stopScanning();
        } else {
            startScan(null);
        }
    }
    // endregion

    // TODO startScan()
    // region Scan
    private void startScan(final UUID[] servicesToScan) {
        Log.d(TAG, "startScan");

        // Stop current scanning (if needed)
        stopScanning();

        Log.v(TAG,"mScannedDevices.size is "+String.valueOf(mScannedDevices.size()));

        // Configure scanning
        BluetoothAdapter bluetoothAdapter = BleUtils.getBluetoothAdapter(getApplicationContext());
        if (BleUtils.getBleStatus(this) != BleUtils.STATUS_BLE_ENABLED) {
            Log.w(TAG, "startScan: BluetoothAdapter not initialized or unspecified address.");
        } else {

            // Adds devices to the list that are not connected but are pingable or maybe broadcasting
            mScanner = new BleDevicesScanner(bluetoothAdapter, servicesToScan, new BluetoothAdapter.LeScanCallback() {

                // As long as a device can be pinged, this called back will constantly be called for the device.
                // It returns information like signal intensity for example.
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {

                    // We need to check if a previously connected device has been disconnected.
                    // We need to update the UI immediately if it has.


                    //final String deviceName = device.getName();
                    //Log.d(TAG, "Discovered device: " + (deviceName != null ? deviceName : "<unknown>"));

                    BluetoothDeviceData previouslyScannedDeviceData = null;
                    if (mScannedDevices == null)
                        mScannedDevices = new ArrayList<>();       // Safeguard

                    // Check that the device was not previously found
                    for (BluetoothDeviceData deviceData : mScannedDevices) {
                        if (deviceData.device.getAddress().equals(device.getAddress())) {
                            previouslyScannedDeviceData = deviceData;
                            break;
                        }
                    }

                    BluetoothDeviceData deviceData;
                    if (previouslyScannedDeviceData == null) {
                        // Add it to the mScannedDevice list
                        deviceData = new BluetoothDeviceData();
                        mScannedDevices.add(deviceData); // <--- add the device the device data to the list
                    } else {
                        deviceData = previouslyScannedDeviceData;
                    }

                    // Update the device data in the list
                    deviceData.device = device;
                    deviceData.rssi = rssi;
                    deviceData.scanRecord = scanRecord;
                    decodeScanRecords(deviceData);

                    // Update device data
                    long currentMillis = SystemClock.uptimeMillis();
                    if (previouslyScannedDeviceData == null || currentMillis - mLastUpdateMillis > kMinDelayToUpdateUI) {          // Avoid updating when not a new device has been found and the time from the last update is really short to avoid updating UI so fast that it will become unresponsive
                        mLastUpdateMillis = currentMillis;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateUI(); // should update every 200 milliseconds
                            }
                        });
                    }

                }
            });

            // Start scanning
            Log.v(TAG,"mScanner.listenForDeadConnections()");
            mScanner.start(this);
        }

        // Adds to the display list devices that are connected (not broadcast)
        for(BluetoothDeviceData datum : BleManager.myConnectedDeviceData) {
            mScannedDevices.add(datum);
            Log.v(TAG, "Added connected device to scanned devices list");
            Log.v(TAG, "Connected devices list has " + String.valueOf(connectedDeviceData.size()) + " devices");
        }


        Log.v(TAG,"Updating UI");

        // Update UI
        updateUI();
    }


    public void removeDataFromList(String address, ArrayList<BluetoothDeviceData> arrayList){
        Iterator<BluetoothDeviceData> it = arrayList.iterator();
        while(it.hasNext()) {
            BluetoothDeviceData mData = it.next();
            if(mData.device.getAddress().equals(address)) {
                Log.v(TAG,"mData.device.getAddress() remove was "+String.valueOf(mData.device.getAddress() ));
                it.remove();
            }
        }
    }



    public void stopScanning() {
        // Stop scanning
        if (mScanner != null) {
            mScanner.stop();
            mScanner = null;
        }

        updateUI();
    }
    // endregion

    // TODO decodeScanRecords()
    private void decodeScanRecords(BluetoothDeviceData deviceData) {
        // based on http://stackoverflow.com/questions/24003777/read-advertisement-packet-in-android
        final byte[] scanRecord = deviceData.scanRecord;

        ArrayList<UUID> uuids = new ArrayList<>();
        byte[] advertisedData = Arrays.copyOf(scanRecord, scanRecord.length);
        int offset = 0;
        deviceData.type = BluetoothDeviceData.kType_Unknown;

        // Check if is an iBeacon ( 0x02, 0x0x1, a flag byte, 0x1A, 0xFF, manufacturer (2bytes), 0x02, 0x15)
        final boolean isBeacon = advertisedData[0] == 0x02 && advertisedData[1] == 0x01 && advertisedData[3] == 0x1A && advertisedData[4] == (byte) 0xFF && advertisedData[7] == 0x02 && advertisedData[8] == 0x15;

        // Check if is an URIBeacon
        final byte[] kUriBeaconPrefix = {0x03, 0x03, (byte) 0xD8, (byte) 0xFE};
        final boolean isUriBeacon = Arrays.equals(Arrays.copyOf(scanRecord, kUriBeaconPrefix.length), kUriBeaconPrefix) && advertisedData[5] == 0x16 && advertisedData[6] == kUriBeaconPrefix[2] && advertisedData[7] == kUriBeaconPrefix[3];

        if (isBeacon) {
            deviceData.type = BluetoothDeviceData.kType_Beacon;

            // Read uuid
            offset = 9;
            UUID uuid = BleUtils.getUuidFromByteArrayBigEndian(Arrays.copyOfRange(scanRecord, offset, offset + 16));
            uuids.add(uuid);
            offset += 16;

            // Skip major minor
            offset += 2 * 2;   // major, minor

            // Read txpower
            final int txPower = advertisedData[offset++];
            deviceData.txPower = txPower;
        } else if (isUriBeacon) {
            deviceData.type = BluetoothDeviceData.kType_UriBeacon;

            // Read txpower
            final int txPower = advertisedData[9];
            deviceData.txPower = txPower;
        } else {
            // Read standard advertising packet
            while (offset < advertisedData.length - 2) {
                // Length
                int len = advertisedData[offset++];
                if (len == 0) break;

                // Type
                int type = advertisedData[offset++];
                if (type == 0) break;

                // Data
//            Log.d(TAG, "record -> lenght: " + length + " type:" + type + " data" + data);

                switch (type) {
                    case 0x02:          // Partial list of 16-bit UUIDs
                    case 0x03: {        // Complete list of 16-bit UUIDs
                        while (len > 1) {
                            int uuid16 = advertisedData[offset++] & 0xFF;
                            uuid16 |= (advertisedData[offset++] << 8);
                            len -= 2;
                            uuids.add(UUID.fromString(String.format("%08x-0000-1000-8000-00805f9b34fb", uuid16)));
                        }
                        break;
                    }

                    case 0x06:          // Partial list of 128-bit UUIDs
                    case 0x07: {        // Complete list of 128-bit UUIDs
                        while (len >= 16) {
                            try {
                                // Wrap the advertised bits and order them.
                                UUID uuid = BleUtils.getUuidFromByteArraLittleEndian(Arrays.copyOfRange(advertisedData, offset, offset + 16));
                                uuids.add(uuid);

                            } catch (IndexOutOfBoundsException e) {
                                Log.e(TAG, "BlueToothDeviceFilter.parseUUID: " + e.toString());
                            } finally {
                                // Move the offset to read the next uuid.
                                offset += 16;
                                len -= 16;
                            }
                        }
                        break;
                    }

                    case 0x09: {
                        byte[] nameBytes = new byte[len - 1];
                        for (int i = 0; i < len - 1; i++) {
                            nameBytes[i] = advertisedData[offset++];
                        }

                        String name = null;
                        try {
                            name = new String(nameBytes, "utf-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        deviceData.advertisedName = name;
                        break;
                    }

                    case 0x0A: {        // TX Power
                        final int txPower = advertisedData[offset++];
                        deviceData.txPower = txPower;
                        break;
                    }

                    default: {
                        offset += (len - 1);
                        break;
                    }
                }
            }

            // Check if Uart is contained in the uuids
            boolean isUart = false;
            for (UUID uuid : uuids) {
                if (uuid.toString().equalsIgnoreCase(UartInterfaceActivity.UUID_SERVICE)) {
                    isUart = true;
                    break;
                }
            }
            if (isUart) {
                deviceData.type = BluetoothDeviceData.kType_Uart;
            }
        }

        deviceData.uuids = uuids;
    }


    public void updateUI() {
        // Scan button
        boolean isScanning = mScanner != null && mScanner.isScanning();
        mScanButton.setText(getString(isScanning ? R.string.scan_scanbutton_scanning : R.string.scan_scanbutton_scan));

        // Show list and hide "no devices" label
        final boolean isListEmpty = mScannedDevices == null || mScannedDevices.size() == 0;
        mNoDevicesTextView.setVisibility(isListEmpty ? View.VISIBLE : View.GONE);
        mDevicesScrollView.setVisibility(isListEmpty ? View.GONE : View.VISIBLE);

        // devices list
        Log.v(TAG,"Notifying data set changed");
        mScannedDevicesAdapter.notifyDataSetChanged();
    }

    // region ResetBluetoothAdapterListener
    @Override
    public void resetBluetoothCompleted() {
        Log.d(TAG, "Reset completed -> Resume scanning");
        resumeScanning();
    }
    // endregion

    private void launchComponentActivity() {
        Log.v(TAG,"launchComponentActivity");

        // Enable generic attribute service
        final BluetoothGattService genericAttributeService = mBleManager.getGattService(kGenericAttributeService);
        if (genericAttributeService != null) {
            Log.d(TAG, "kGenericAttributeService found. Check if kServiceChangedCharacteristic exists");

            final UUID characteristicUuid = UUID.fromString(kServiceChangedCharacteristic);
            final BluetoothGattCharacteristic dataCharacteristic = genericAttributeService.getCharacteristic(characteristicUuid);
            if (dataCharacteristic != null) {
                Log.d(TAG, "kServiceChangedCharacteristic exists. Enable indication");
                mBleManager.enableIndication(genericAttributeService, kServiceChangedCharacteristic, true);
            } else {
                Log.d(TAG, "Skip enable indications for kServiceChangedCharacteristic. Characteristic not found");
            }
        } else {
            Log.d(TAG, "Skip enable indications for kServiceChangedCharacteristic. kGenericAttributeService not found");
        }

        // Launch activity
        showConnectionStatus(false);
        if (mComponentToStartWhenConnected != null) {
            Log.d(TAG, "Start component:" + mComponentToStartWhenConnected);
            Intent intent = new Intent(MainActivity.this, mComponentToStartWhenConnected);
            if (mComponentToStartWhenConnected == BeaconActivity.class && mSelectedDeviceData != null) {
                intent.putExtra("rssi", mSelectedDeviceData.rssi);
            }
            startActivityForResult(intent, kActivityRequestCode_ConnectedActivity);
        }
    }

    // region BleManagerListener
    @Override
    public void onConnected() {
    }

    @Override
    public void onConnecting() {
    }

    @Override
    public void onDisconnected() {
        Log.d(TAG, "MainActivity onDisconnected");
        showConnectionStatus(false);
    }

    @Override
    public void onServicesDiscovered() {

        super.onServicesDiscovered();
        enableRxNotifications();

        Log.d(TAG, "services discovered");

        // Check if there is a failed installation that was stored to retry
        boolean isFailedInstallationDetected = FirmwareUpdater.isFailedInstallationRecoveryAvailable(this, mBleManager.getConnectedDeviceAddress());
        if (isFailedInstallationDetected) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Failed installation detected");
                    // Ask user if should update
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.scan_failedupdatedetected_title)
                            .setMessage(R.string.scan_failedupdatedetected_message)
                            .setPositiveButton(R.string.scan_failedupdatedetected_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    showConnectionStatus(false);        // hide current dialogs because software update will display a dialog
                                    stopScanning();

                                    mFirmwareUpdater.startFailedInstallationRecovery(MainActivity.this);
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    FirmwareUpdater.clearFailedInstallationRecoveryParams(MainActivity.this);
                                    launchComponentActivity();
                                }
                            })
                            .setCancelable(false)
                            .show();
                }
            });
        } else {
            // Check if a firmware update is available
            boolean isCheckingFirmware = false;
            if (mFirmwareUpdater != null) {
                // Don't bother the user waiting for checks if the latest connected device was this one too
                String deviceAddress = mBleManager.getConnectedDeviceAddress();
                if (!deviceAddress.equals(mLatestCheckedDeviceAddress)) {
                    mLatestCheckedDeviceAddress = deviceAddress;

                    // Check if should update device software
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showGettingUpdateInfoState();
                        }
                    });
                    mFirmwareUpdater.checkFirmwareUpdatesForTheCurrentConnectedDevice();        // continues asynchronously in onFirmwareUpdatesChecked
                    isCheckingFirmware = true;
                } else {
                    Log.d(TAG, "Updates: Device already checked previously. Skipping...");
                }
            }

            if (!isCheckingFirmware) {
                onFirmwareUpdatesChecked(false, null, null, null);
            }
        }
    }

    @Override
    public synchronized void onDataAvailable(BluetoothGattCharacteristic characteristic) { // TODO onDataAvailable()
        Log.v(TAG,"onDataAvailable()");


        if (characteristic.getService().getUuid().toString().equalsIgnoreCase(UUID_SERVICE)) {
            if (characteristic.getUuid().toString().equalsIgnoreCase(UUID_RX)) {

                final byte[] packet = characteristic.getValue();

                Log.v(TAG,"Packet to String");
                String stringyPacket = PacketUtils.packetToString(packet);
                Log.v(TAG,stringyPacket);

                Intent intent = new Intent(this, ColorPickerActivity1Color.class);
                int r = packet[3];
                int g = packet[4];
                int b = packet[5];
                intent.putExtra("values",new int[]{r,g,b});
                startActivity(intent);


            }
        }

        Log.v(TAG,"END");
    }

    @Override
    public void onDataAvailable(BluetoothGattDescriptor descriptor) {
    }

    @Override
    public void onReadRemoteRssi(int rssi) {

    }
    // endregion

    // region SoftwareUpdateManagerListener
    @Override
    public void onFirmwareUpdatesChecked(boolean isUpdateAvailable, final ReleasesParser.FirmwareInfo
            latestRelease, FirmwareUpdater.DeviceInfoData deviceInfoData, Map<String,
            ReleasesParser.BoardInfo> allReleases) {
        mBleManager.setBleListener(this);           // Restore listener

        if (isUpdateAvailable) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Ask user if should update
                    String message = String.format(getString(R.string.scan_softwareupdate_messageformat), latestRelease.version);
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.scan_softwareupdate_title)
                            .setMessage(message)
                            .setPositiveButton(R.string.scan_softwareupdate_install, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    showConnectionStatus(false);        // hide current dialogs because software update will display a dialog
                                    stopScanning();
                                    //BluetoothDevice device = mBleManager.getConnectedDevice();
                                    mFirmwareUpdater.downloadAndInstall(MainActivity.this, latestRelease);
                                }
                            })
                            .setNeutralButton(R.string.scan_softwareupdate_notnow, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    launchComponentActivity();
                                }
                            })
                            .setNegativeButton(R.string.scan_softwareupdate_dontask, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mFirmwareUpdater.ignoreVersion(latestRelease.version);
                                    launchComponentActivity();
                                }
                            })
                            .setCancelable(false)
                            .show();
                }
            });
        } else {
            Log.d(TAG, "onFirmwareUpdatesChecked: No software update available");
            launchComponentActivity();
        }
    }

    @Override
    public void onUpdateCancelled() {
        Log.d(TAG, "Software version installation cancelled");

        mLatestCheckedDeviceAddress = null;

        mScannedDevices.clear();
        startScan(null);
    }

    @Override
    public void onUpdateCompleted() {
        Log.d(TAG, "Software version installation completed successfully");

        Toast.makeText(this, R.string.scan_softwareupdate_completed, Toast.LENGTH_LONG).show();

        mScannedDevices.clear();
        startScan(null);
    }

    @Override
    public void onUpdateFailed(boolean isDownloadError) {
        Log.d(TAG, "Software version installation failed");
        Toast.makeText(this, isDownloadError ? R.string.scan_softwareupdate_downloaderror : R.string.scan_softwareupdate_updateerror, Toast.LENGTH_LONG).show();

        mLatestCheckedDeviceAddress = null;

        mScannedDevices.clear();
        startScan(null);
    }

    @Override
    public void onUpdateDeviceDisconnected() {

        // Update UI
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onDisconnected();

                mLatestCheckedDeviceAddress = null;

                mScannedDevices.clear();
                startScan(null);
            }
        });
    }


    // endregion

    // region Helpers
    // TODO BluetoothDeviceData class

    //endregion

    // region Peripheral List
    public class PeripheralList {
        // Constants
        private final static int kMaxRssiValue = -100;

        private final static String kPreferences = "PeripheralList_prefs";
        private final static String kPreferences_filtersName = "filtersName";
        private final static String kPreferences_filtersIsNameExact = "filtersIsNameExact";
        private final static String kPreferences_filtersIsNameCaseInsensitive = "filtersIsNameCaseInsensitive";
        private final static String kPreferences_filtersRssi = "filtersRssi";
        private final static String kPreferences_filtersUnnamedEnabled = "filtersUnnamedEnabled";
        private final static String kPreferences_filtersUartEnabled = "filtersUartEnabled";

        // Data
        private String mFilterName;
        private boolean mIsFilterNameExact;
        private boolean mIsFilterNameCaseInsensitive;
        private int mRssiFilterValue;
        private boolean mIsUnnamedEnabled;
        private boolean mIsOnlyUartEnabled;
        private ArrayList<BluetoothDeviceData> mCachedFilteredPeripheralList;
        private boolean mIsFilterDirty;

        private SharedPreferences.Editor preferencesEditor = getSharedPreferences(kPreferences, MODE_PRIVATE).edit();

        PeripheralList() {
            mIsFilterDirty = true;
            mCachedFilteredPeripheralList = new ArrayList<>();

            SharedPreferences preferences = getSharedPreferences(kPreferences, MODE_PRIVATE);
            mFilterName = preferences.getString(kPreferences_filtersName, null);
            mIsFilterNameExact = preferences.getBoolean(kPreferences_filtersIsNameExact, false);
            mIsFilterNameCaseInsensitive = preferences.getBoolean(kPreferences_filtersIsNameCaseInsensitive, true);
            mRssiFilterValue = preferences.getInt(kPreferences_filtersRssi, kMaxRssiValue);
            mIsUnnamedEnabled = preferences.getBoolean(kPreferences_filtersUnnamedEnabled, true);
            mIsOnlyUartEnabled = preferences.getBoolean(kPreferences_filtersUartEnabled, true); // TODO mIsOnlyUartEnabled
        }

        String getFilterName() {
            return mFilterName;
        }

        void setFilterName(String name) {
            mFilterName = name;
            mIsFilterDirty = true;

            preferencesEditor.putString(kPreferences_filtersName, name);
            preferencesEditor.apply();
        }

        boolean isFilterNameExact() {
            return mIsFilterNameExact;
        }

        void setFilterNameExact(boolean exact) {
            mIsFilterNameExact = exact;
            mIsFilterDirty = true;

            preferencesEditor.putBoolean(kPreferences_filtersIsNameExact, exact);
            preferencesEditor.apply();
        }

        boolean isFilterNameCaseInsensitive() {
            return mIsFilterNameCaseInsensitive;
        }

        void setFilterNameCaseInsensitive(boolean caseInsensitive) {
            mIsFilterNameCaseInsensitive = caseInsensitive;
            mIsFilterDirty = true;

            preferencesEditor.putBoolean(kPreferences_filtersIsNameCaseInsensitive, caseInsensitive);
            preferencesEditor.apply();
        }

        int getFilterRssiValue() {
            return mRssiFilterValue;
        }

        void setFilterRssiValue(int value) {
            mRssiFilterValue = value;
            mIsFilterDirty = true;

            preferencesEditor.putInt(kPreferences_filtersRssi, value);
            preferencesEditor.apply();
        }

        boolean isFilterUnnamedEnabled() {
            return mIsUnnamedEnabled;
        }

        void setFilterUnnamedEnabled(boolean enabled) {
            mIsUnnamedEnabled = enabled;
            mIsFilterDirty = true;

            preferencesEditor.putBoolean(kPreferences_filtersUnnamedEnabled, enabled);
            preferencesEditor.apply();
        }


        boolean isFilterOnlyUartEnabled() {
            return mIsOnlyUartEnabled;
        }

        void setFilterOnlyUartEnabled(boolean enabled) {
            mIsOnlyUartEnabled = enabled;
            mIsFilterDirty = true;

            preferencesEditor.putBoolean(kPreferences_filtersUartEnabled, enabled);
            preferencesEditor.apply();
        }


        void setDefaultFilters() {
            mFilterName = null;
            mIsFilterNameExact = false;
            mIsFilterNameCaseInsensitive = true;
            mRssiFilterValue = kMaxRssiValue;
            mIsUnnamedEnabled = true;
            mIsOnlyUartEnabled = false; // TODO UART filter
        }

        boolean isAnyFilterEnabled() {
            return (mFilterName != null && !mFilterName.isEmpty()) || mRssiFilterValue > kMaxRssiValue || mIsOnlyUartEnabled || !mIsUnnamedEnabled;
        }

        public ArrayList<BluetoothDeviceData> filteredPeripherals(boolean forceUpdate) {
            if (mIsFilterDirty || forceUpdate) {
                mCachedFilteredPeripheralList = calculateFilteredPeripherals();
                mIsFilterDirty = false;
            }
            return mCachedFilteredPeripheralList;
        }

        // returns the filtered list
        private ArrayList<BluetoothDeviceData> calculateFilteredPeripherals() {

            // Clones the original list and filters it by removing its elements
            ArrayList<BluetoothDeviceData> peripherals = (ArrayList<BluetoothDeviceData>) mScannedDevices.clone();

            // Sort devices alphabetically
            Collections.sort(peripherals, new Comparator<BluetoothDeviceData>() {
                @Override
                public int compare(BluetoothDeviceData o1, BluetoothDeviceData o2) {
                    // This routine returns -1, 0, or 1 as values
                    return o1.getNiceName().compareToIgnoreCase(o2.getNiceName());
                }
            });

            // Apply filters
            if (mIsOnlyUartEnabled) {
                for (Iterator<BluetoothDeviceData> it = peripherals.iterator(); it.hasNext(); ) {
                    if (it.next().type != BluetoothDeviceData.kType_Uart) {
                        it.remove();
                    }
                }
            }

            if (!mIsUnnamedEnabled) {
                for (Iterator<BluetoothDeviceData> it = peripherals.iterator(); it.hasNext(); ) {
                    if (it.next().getName() == null) {
                        it.remove();
                    }
                }
            }

            if (mFilterName != null && !mFilterName.isEmpty()) {
                for (Iterator<BluetoothDeviceData> it = peripherals.iterator(); it.hasNext(); ) {
                    String name = it.next().getName();
                    boolean testPassed = false;
                    if (name != null) {
                        if (mIsFilterNameExact) {
                            if (mIsFilterNameCaseInsensitive) {
                                testPassed = name.compareToIgnoreCase(mFilterName) == 0;
                            } else {
                                testPassed = name.compareTo(mFilterName) == 0;
                            }
                        } else {
                            if (mIsFilterNameCaseInsensitive) {
                                testPassed = name.toLowerCase().contains(mFilterName.toLowerCase());
                            } else {
                                testPassed = name.contains(mFilterName);
                            }
                        }
                    }
                    if (!testPassed) {
                        it.remove();
                    }
                }
            }

            for (Iterator<BluetoothDeviceData> it = peripherals.iterator(); it.hasNext(); ) {
                if (it.next().rssi < mRssiFilterValue) {
                    it.remove();
                }
            }

            return peripherals;
        }

        String filtersDescription() {
            String filtersTitle = null;

            if (mFilterName != null && !mFilterName.isEmpty()) {
                filtersTitle = mFilterName;
            }

            if (mRssiFilterValue > kMaxRssiValue) {
                String rssiString = String.format(Locale.ENGLISH, getString(R.string.scan_filters_name_rssi_format), mRssiFilterValue);
                if (filtersTitle != null && !filtersTitle.isEmpty()) {
                    filtersTitle = filtersTitle + ", " + rssiString;
                } else {
                    filtersTitle = rssiString;
                }
            }

            if (!mIsUnnamedEnabled) {
                String namedString = getString(R.string.scan_filters_name_named);
                if (filtersTitle != null && !filtersTitle.isEmpty()) {
                    filtersTitle = filtersTitle + ", " + namedString;
                } else {
                    filtersTitle = namedString;
                }
            }

            if (mIsOnlyUartEnabled) {
                String uartString = getString(R.string.scan_filters_name_uart);
                if (filtersTitle != null && !filtersTitle.isEmpty()) {
                    filtersTitle = filtersTitle + ", " + uartString;
                } else {
                    filtersTitle = uartString;
                }
            }

            return filtersTitle;
        }
    }

    // endregion


    // region adapters
    // TODO ExpandableListAdapter


    //endregion

    // region DataFragment
    public static class DataFragment extends Fragment {
        private ArrayList<BluetoothDeviceData> mScannedDevices;
        private Class<?> mComponentToStartWhenConnected;
        private boolean mShouldEnableWifiOnQuit;
        private FirmwareUpdater mFirmwareUpdater;
        private String mLatestCheckedDeviceAddress;
        private BluetoothDeviceData mSelectedDeviceData;
        //private PeripheralList mPeripheralList;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }

    }

    private void restoreRetainedDataFragment() {
        // find the retained fragment
        FragmentManager fm = getFragmentManager();
        mRetainedDataFragment = (DataFragment) fm.findFragmentByTag(TAG);

        if (mRetainedDataFragment == null) {
            // Create
            mRetainedDataFragment = new DataFragment();
            fm.beginTransaction().add(mRetainedDataFragment, TAG).commitAllowingStateLoss();        // http://stackoverflow.com/questions/7575921/illegalstateexception-can-not-perform-this-action-after-onsaveinstancestate-h

            mScannedDevices = new ArrayList<>();
            // mPeripheralList = new PeripheralList();

        } else {
            // Restore status
            mScannedDevices = mRetainedDataFragment.mScannedDevices;
            mComponentToStartWhenConnected = mRetainedDataFragment.mComponentToStartWhenConnected;
            mShouldEnableWifiOnQuit = mRetainedDataFragment.mShouldEnableWifiOnQuit;
            mFirmwareUpdater = mRetainedDataFragment.mFirmwareUpdater;
            mLatestCheckedDeviceAddress = mRetainedDataFragment.mLatestCheckedDeviceAddress;
            mSelectedDeviceData = mRetainedDataFragment.mSelectedDeviceData;
            //mPeripheralList = mRetainedDataFragment.mPeripheralList;

            if (mFirmwareUpdater != null) {
                mFirmwareUpdater.changedParentActivity(this);       // set the new activity
            }
        }
    }

    private void saveRetainedDataFragment() {
        mRetainedDataFragment.mScannedDevices = mScannedDevices;
        mRetainedDataFragment.mComponentToStartWhenConnected = mComponentToStartWhenConnected;
        mRetainedDataFragment.mShouldEnableWifiOnQuit = mShouldEnableWifiOnQuit;
        mRetainedDataFragment.mFirmwareUpdater = mFirmwareUpdater;
        mRetainedDataFragment.mLatestCheckedDeviceAddress = mLatestCheckedDeviceAddress;
        mRetainedDataFragment.mSelectedDeviceData = mSelectedDeviceData;
        //mRetainedDataFragment.mPeripheralList = mPeripheralList;
    }
    // endregion
}