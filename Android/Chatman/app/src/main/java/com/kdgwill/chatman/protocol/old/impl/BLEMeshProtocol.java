package com.kdgwill.chatman.protocol.old.impl;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.os.ParcelUuid;
import android.text.TextUtils;

import java.lang.ref.WeakReference;
import java.util.UUID;

/**
 * Created by kylewilliams on 5/10/16.
 */
public abstract class BLEMeshProtocol implements MeshProtocol {

    private WeakReference<MeshActivity_OLD> activity;
    private WeakReference<BluetoothManager> blueMan;
    public final ParcelUuid BROADCAST_UUID;  //The unique device identifier for broadcasting
    public final ParcelUuid MAC_UUID;  //The unique device identifier
    public final MAC_MODE macMODE;
    public static final MAC_MODE DEFAULT_MAC_MODE = MAC_MODE.THIRTY_TWO_BIT;

//    static{
//        BROADCAST_UUID = createBroadcastUUID();
//        MAC_UUID = createMACUUID();
//    }

    private enum MAC_MODE{
        _default,
        SIXTEEN_BIT,
        THIRTY_TWO_BIT
    }

    public BLEMeshProtocol(MeshActivity_OLD activity){
        this(activity,DEFAULT_MAC_MODE);
    }

    public BLEMeshProtocol(MeshActivity_OLD activity, MAC_MODE macMode){
        this(activity,null,macMode);
    }
    public BLEMeshProtocol(MeshActivity_OLD activity, BluetoothManager bluetoothManager){
        this(activity,bluetoothManager,DEFAULT_MAC_MODE);
    }

    public BLEMeshProtocol(MeshActivity_OLD activity, BluetoothManager bluetoothManager,
                           MAC_MODE macMode){
        setActivity(activity);
        setBluetoothManager(bluetoothManager);
        this.macMODE = macMode;
        BROADCAST_UUID = createBroadcastUUID();
        MAC_UUID = createMACUUID();
    }

    public void setActivity(MeshActivity_OLD activity) {
        this.activity = new WeakReference<MeshActivity_OLD>(activity);
    }

    protected MeshActivity_OLD getActivity(){
        return activity.get();
    }

    public void setBluetoothManager(BluetoothManager bluetoothManager) {
        blueMan = new WeakReference<BluetoothManager>(bluetoothManager);
    }

    protected BluetoothManager getBluetoothManager(){
        return blueMan.get();
    }
    protected BluetoothAdapter getBluetoothAdapter(){
        return blueMan.get().getAdapter();
    }
    protected BluetoothLeScanner getBLEScanner(){
        return blueMan.get().getAdapter().getBluetoothLeScanner();
    }
    protected BluetoothLeAdvertiser getBLEAdvertiser(){
        return blueMan.get().getAdapter().getBluetoothLeAdvertiser();
    }

    private ParcelUuid createBroadcastUUID(){
        // creating UUID
        UUID uid = null;
        switch (macMODE){
            case SIXTEEN_BIT:
                uid = UUID.fromString("0000ffff-0000-1000-8000-00805f9b34fb");
                break;
            case THIRTY_TWO_BIT:
                uid = UUID.fromString("ffffffff-0000-1000-8000-00805f9b34fb");
                break;
            case _default:
                uid = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
        }
//        return uuid
        return new ParcelUuid(uid);
    }

    private ParcelUuid createMACUUID(){
        //Create Special UUID for session
        String temp = UUID.randomUUID().toString().toLowerCase();
        //Convert Upper so can follow 32-bit transfer
        String s = TextUtils.split(temp,"-")[0];
        String newUUID = null; //"00000000-0000-1000-8000-00805f9b34fb";
        switch(macMODE){
            case SIXTEEN_BIT:
                //Append SIG generic postfix so that it only sends 16bits instead of 128bits
                String t = s.substring(s.length()/2);
                newUUID = "0000" + t + "-0000-1000-8000-00805f9b34fb";
                break;
            case THIRTY_TWO_BIT:
                //Append SIG generic postfix so that it only sends 32bits instead of 128bits
                newUUID = s + "-0000-1000-8000-00805f9b34fb";
                break;
            case _default:
                newUUID = temp;
        }
        //return UUID
        return new ParcelUuid(UUID.fromString(newUUID));
    }
}
