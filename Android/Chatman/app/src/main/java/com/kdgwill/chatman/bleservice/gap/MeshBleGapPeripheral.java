package com.kdgwill.chatman.bleservice.gap;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.support.annotation.NonNull;
import android.util.Log;

import com.kdgwill.chatman.bleservice.MeshBleService;

import java.lang.ref.WeakReference;

/**
 * Created by kylewilliams on 5/11/16.
 */
public class MeshBleGapPeripheral extends AdvertiseCallback {
    protected String logTag = "Chatman/MeshBleGapPeripheral :=";
    private final WeakReference<MeshBleService> mService;
    private WeakReference<BluetoothLeAdvertiser> mBluetoothLeAdvertiser;
    private WeakReference<AdvertiseCallback> advCallBack;//ONLY ONE ADVERTISER AT A TIME
    private AdvertiseSettings mSettings;
    private AdvertiseData mData;

    public MeshBleGapPeripheral(@NonNull MeshBleService service) {
        mService = new WeakReference<>(service);
    }

    public BluetoothLeAdvertiser getAdvertiser() {
        if (mBluetoothLeAdvertiser == null) {
            if (mService.get() != null) {
                BluetoothAdapter bla = mService.get().getBluetoothAdapter();
                mBluetoothLeAdvertiser = new WeakReference<>(bla.getBluetoothLeAdvertiser());
            } else {
                //TODO: What to do if can't access advertiser before BluetoothControllerFragment handles it
            }
        }
        return mBluetoothLeAdvertiser.get();
    }

    /**
     * TODO:
     * Settings are source of battery drain for BLE Use step up and step down of power
     * level to control discoverability. If can connect to at least 1 person then
     * Mesh should allow balanced
     */
    private AdvertiseSettings getSettings(){
        if(mSettings ==null){
            mSettings = new AdvertiseSettings.Builder()
                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                    .setConnectable(true)
                    //Have to be same as handle so forget it
//                    .setTimeout(BLES_Constants.ADVERTISE_PERIOD)
                    .build();
        }
        return mSettings;
    }
    private AdvertiseData getData(){
        if(mData ==null){
            mData = new AdvertiseData.Builder()
                    .setIncludeDeviceName(false)
                    .setIncludeTxPowerLevel(false)
                    .addServiceUuid(mService.get().getMacModule().getMeshAppUUID())
                    .addServiceUuid(mService.get().getMacModule().getMACUUID())
                    .build();
        }
        return mData;
    }

    public void startAdvertise(){
        startAdvertise(getData());
    }

    public void startAdvertise(AdvertiseData advertiseData){
        startAdvertise(advertiseData, getSettings(),this);
    }

    public void startAdvertise(AdvertiseData advertiseData, AdvertiseSettings advertiseSettings,
                          final AdvertiseCallback advertiseCallback){
        if(advCallBack!=null){stopAdvertise(advCallBack.get());}
        getAdvertiser().startAdvertising(advertiseSettings,advertiseData,advertiseCallback);
        advCallBack = new WeakReference<>(advertiseCallback);
    }

    public void stopAdvertise(){
       stopAdvertise(this);
    }

    public void stopAdvertise(final AdvertiseCallback advertiseCallback){
        if(advCallBack!=null && advCallBack.get().equals(advertiseCallback)){
            getAdvertiser().stopAdvertising(advertiseCallback);
            advCallBack.clear();
            advCallBack = null;
        }
    }

    ////////////////////////////////////AdvertiseCallback//////////////////////////

    @Override
    public void onStartSuccess(AdvertiseSettings settingsInEffect) {
//        Log.d(logTag, "ADVERTISE_SUCCESS");
    }

    @Override
    public void onStartFailure(int errorCode) {
        switch (errorCode) {
            case AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED:
                Log.e(logTag, "ADVERTISE_FAILED_ALREADY_STARTED");
                break;
            case AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE:
                Log.e(logTag, "ADVERTISE_FAILED_DATA_TOO_LARGE");
                break;
            case AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                Log.e(logTag, "ADVERTISE_FAILED_FEATURE_UNSUPPORTED");
                break;
            case AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR:
                Log.e(logTag, "ADVERTISE_FAILED_INTERNAL_ERROR");
                break;
            case AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                Log.e(logTag, "ADVERTISE_FAILED_TOO_MANY_ADVERTISERS");
                break;
        }
    }



}
