package com.kdgwill.chatman;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;


public class BluetoothControllerFragment extends Fragment {

    private String msg = "Chatman/Basic =";
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;

    public BluetoothManager getBluetoothManager(){return bluetoothManager;}
    public BluetoothAdapter getBluetoothAdapter(){return bluetoothAdapter;}

    //Bluetooth LE Request Codes
    private enum RequestCodes {
        __default,//need an offset since request code cannot be 0
        ENABLE_BT,
        BLUETOOTH_PERMISSION,
        LOCATION_PERMISSION
    }

    public BluetoothControllerFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);


//        setContentView(R.layout.activity_main);
        //Ultimately un-needed since have uses function for BLE in manifest
        if (!this.getActivity().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this.getActivity(), R.string.no_ble_support, Toast.LENGTH_LONG).show();
            this.getActivity().finishAndRemoveTask();
        }
        // Initializes Bluetooth adapter.
        bluetoothManager = (BluetoothManager)
                this.getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
    }


    @Override
    public void onStart() {
        super.onStart();
        boolean blue = checkBluetoothPermissions();
        boolean loc = checkLocationPermissions();
        if (blue && loc){
            bluetoothAdapter.enable();
            checkExtendedBluetoothSupport();//Called if already on anyway
        }else{
            checkBluetoothEnabled();
        }
        //Need To Make Sure bluetooth always on
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.getActivity().registerReceiver(bluetoothState, filter);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Unregister broadcast listeners
        this.getActivity().unregisterReceiver(bluetoothState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check which request we're responding to
        RequestCodes value = RequestCodes.values()[requestCode];
        switch (value) {
            case ENABLE_BT:
                if (resultCode == this.getActivity().RESULT_OK) {
                    Log.d(msg, "Bluetooth Enabled");
                    Toast.makeText(this.getActivity(), "Bluetooth Enabled", Toast.LENGTH_SHORT).show();
                    // Initializes Bluetooth adapter if nil.
                    if (bluetoothManager==null || bluetoothAdapter == null) {
                        bluetoothManager = (BluetoothManager) this.getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
                        bluetoothAdapter = bluetoothManager.getAdapter();
                    }
                } else {
                    showEnableBluetoothDialog();
                }
                break;
            default:
        }
    }

    //    TODO: This is above usable version and not tested and as such not fully implemented
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        RequestCodes value = RequestCodes.values()[requestCode];
        switch (value) {
            case BLUETOOTH_PERMISSION:
                for (int i = 0; i < permissions.length; i++) {
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.length>0){
                        Log.d(msg, "Bluetooth Permission Cancelled: " + permissions[i]);
                    }else{
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Log.d(msg, "Bluetooth Permission Granted: " + permissions[i]);
                        } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                            Log.d(msg, "Bluetooth Permission Denied: " + permissions[i]);
                        }
                    }
                }
                break;
            case LOCATION_PERMISSION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(msg, "Bluetooth Permission Granted: " + permissions[0]);
                } else {
                    Log.d(msg, "Bluetooth Permission Denied: " + permissions[0]);
                }
                break;
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    private boolean checkBluetoothPermissions() {
        int bl = ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.BLUETOOTH);
        int bla = ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.BLUETOOTH_ADMIN);
        boolean granted = (bl == bla && bla == PackageManager.PERMISSION_GRANTED);
        //Basically if both bl or bla are not granted permission
        if (!granted) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this.getActivity(),
                    Manifest.permission.BLUETOOTH)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this.getActivity(),
                    Manifest.permission.BLUETOOTH_ADMIN)) {
                /*
                 TODO
                 Show an explanation to the user *asynchronously* -- don't block
                 this thread waiting for the user's response! After the user
                 sees the explanation, try again to request the permission.
                */
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this.getActivity(),
                        new String[]{Manifest.permission.BLUETOOTH,
                                Manifest.permission.BLUETOOTH_ADMIN},
                        RequestCodes.BLUETOOTH_PERMISSION.ordinal());
            }
        }
        return granted;
    }

    private boolean checkLocationPermissions() {
        int cl = ContextCompat.checkSelfPermission(this.getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION);
        boolean granted = (cl == PackageManager.PERMISSION_GRANTED);
        if (!granted) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this.getActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                /*
                 TODO
                 Show an explanation to the user *asynchronously* -- don't block
                 this thread waiting for the user's response! After the user
                 sees the explanation, try again to request the permission.
                */
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this.getActivity(),
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,},
                        RequestCodes.LOCATION_PERMISSION.ordinal());
            }
        }
        return granted;
    }

    private void checkBluetoothEnabled() {
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, RequestCodes.ENABLE_BT.ordinal());
        }
    }

    private AlertDialog enableBluetoothDialog;

    private void showEnableBluetoothDialog() {
        if (enableBluetoothDialog == null) {
            enableBluetoothDialog = new AlertDialog.Builder(this.getActivity())
                    .setTitle("Please Enable Bluetooth")
                    .setMessage(R.string.enable_bluetooth)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    checkBluetoothEnabled();
                                }
                            })
                    .setNegativeButton("Leave Application",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    BluetoothControllerFragment.this.getActivity().finishAndRemoveTask();
                                }
                            }).setCancelable(false).create();
        }
        enableBluetoothDialog.show();
    }

    private void checkExtendedBluetoothSupport(){
        //If bluetooth is on and multiple advertisement not supported
        if((bluetoothAdapter != null && bluetoothAdapter.isEnabled()) &&
                !BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported() ) {
            Toast.makeText( this.getActivity(), R.string.no_extended_ble_support,
                    Toast.LENGTH_LONG ).show();
            this.getActivity().finishAndRemoveTask();
        }
    }

    private final BroadcastReceiver bluetoothState = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        showEnableBluetoothDialog();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        checkExtendedBluetoothSupport();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                    case BluetoothAdapter.STATE_TURNING_ON:
                    default:
                        break;
                }
            }
        }
    };


}
