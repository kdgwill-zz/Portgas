package com.kdgwill.chatman.protocol.old.impl;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.kdgwill.chatman.BluetoothControllerFragment;
import com.kdgwill.chatman.R;

import java.util.UUID;

/**
 * Created by kylewilliams on 5/10/16.
 */
public class MeshActivity_OLD extends AppCompatActivity {
    protected String logTag = "Chatman/MeshActivity_OLD := ";
    protected BluetoothControllerFragment bcf;
    protected ParcelUuid ChatMan_UUID;//The device type Identifier

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //First see if it is already created
        bcf = (BluetoothControllerFragment)getSupportFragmentManager()
                .findFragmentByTag("bluetoothController");
        if(bcf == null) {
            bcf = new BluetoothControllerFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(bcf, "bluetoothController").commit();
        }
        //Set UUIDs
        ChatMan_UUID = new ParcelUuid(UUID.fromString(getString(R.string.UUID_CHATMAN)));
    }

    public ParcelUuid getMeshAppUUID(){return ChatMan_UUID;}
    public BluetoothManager getBluetoothManager(){return bcf.getBluetoothManager();}
    public BluetoothAdapter getBluetoothAdapter(){return bcf.getBluetoothAdapter();}
}
