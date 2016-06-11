package com.kdgwill.chatman.bleservice.gap;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Bundle;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.util.Log;

import com.kdgwill.chatman.bleservice.BLES_Constants;
import com.kdgwill.chatman.bleservice.DeviceSetEntry;
import com.kdgwill.chatman.bleservice.MeshBleService;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by kylewilliams on 5/11/16.
 */
public class MeshBleGapCentral extends ScanCallback {
    public static final String DEBUG_KEY_MAC_ADDRESSES = "KEY_MAC_ADDRESSES";

    protected String logTag = "Chatman/MeshBleGapCentral :=";

    private final WeakReference<MeshBleService> mService;
    private WeakReference<BluetoothLeScanner> mBluetoothLeScanner;
    private WeakReference<ScanCallback> scnCallback;//For the time being only one callback at a time
    private List<ScanFilter> mFilters;
    private ScanSettings mSettings;
    private ConcurrentLinkedQueue<DeviceSetEntry> deviceQueue;

    public MeshBleGapCentral(@NonNull MeshBleService service) {
        mService = new WeakReference<>(service);
    }

    public BluetoothLeScanner getScanner() {
        if (mBluetoothLeScanner == null) {
            if (mService.get() != null) {
                BluetoothAdapter bla = mService.get().getBluetoothAdapter();
                mBluetoothLeScanner = new WeakReference<>(bla.getBluetoothLeScanner());
            } else {
                //TODO: What to do if can't access scanner before BluetoothControllerFragment handles it
            }
        }
        return mBluetoothLeScanner.get();
    }

    /**
     * TODO:
     * Settings are source of battery drain for BLE Use step up and step down of power
     * level to control discoverability. If can connect to at least 1 person then
     * Mesh should allow balanced
     */
    private ScanSettings getSettings() {
        if (mSettings == null) {
            mSettings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
        }
        return mSettings;
    }

    private List<ScanFilter> getFilter() {
        if (mFilters == null) {
            mFilters = new ArrayList<>();
            mFilters.add(
                    new ScanFilter.Builder()
                            .setServiceUuid(mService.get().getMacModule().getMeshAppUUID())
                            .build()
            );
        }
        return mFilters;
    }
    private ConcurrentLinkedQueue<DeviceSetEntry> getDeviceQueue() {
        if (deviceQueue == null) {
            deviceQueue = new ConcurrentLinkedQueue<>();
        }
        return deviceQueue;
    }


    public synchronized boolean enqueueDevice(DeviceSetEntry entry){
        if(!getDeviceQueue().contains(entry)){
            return getDeviceQueue().add(entry);
        }
        return false;
    }

    public boolean getQueueIsEmpty(){return getDeviceQueue().isEmpty();}
    public DeviceSetEntry dequeueDevice(){
        return getDeviceQueue().poll();
    }

    public void startScan() {
        startScan(this);
    }

    public void startScan(final ScanCallback callback) {
        startScan(callback, getSettings());
    }

    public void startScan(final ScanCallback callback, ScanSettings scanSettings) {
        startScan(callback, scanSettings, getFilter());
    }

    public void startScan(final ScanCallback callback, List<ScanFilter> filters) {
        startScan(callback, getSettings(), filters);
    }

    public void startScan(final ScanCallback callback, ScanSettings settings,
                          List<ScanFilter> filters) {
        if(scnCallback!=null){stopScan(scnCallback.get());}
        getScanner().startScan(filters, settings, callback);
        scnCallback = new WeakReference<>(callback);
    }

    public void stopScan() {
        stopScan(this);
    }

    public void stopScan(final ScanCallback callback) {
        if(scnCallback!=null && scnCallback.get().equals(callback)){
            getScanner().stopScan(callback);
            scnCallback.clear();
            scnCallback = null;
        }
    }

////////////////////////////////////ScanCallback//////////////////////////

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        super.onScanResult(callbackType, result);
        if (result == null || result.getDevice() == null) {
            return;
        }
        List<ParcelUuid> ls = result.getScanRecord().getServiceUuids();
        if (ls.size() != 2) {
            return;
        }

        //They are in no particular order let's make sure they arrived in correct order
        ParcelUuid appUUID = ls.get(0);
        ParcelUuid macUUID = ls.get(1);
        if (appUUID.equals(mService.get().getMacModule().getMeshAppUUID())) {
            //Nothing the order we want
        } else if (macUUID.equals(mService.get().getMacModule().getMeshAppUUID())) {
//            ParcelUuid temp2 = macUUID;
            macUUID = appUUID;//Only Care about user mac
//            appUUID = temp2;
        } else {
            //Not our advertisement packet
            return;
        }

        long timestamp = BLES_Constants.getCurrentTime();
        //TimeUnit.NANOSECONDS.toMillis(result.getTimestampNanos());
        //If device was already picked up but not processed then ignore this
        if(!enqueueDevice(new DeviceSetEntry(macUUID,result.getDevice(),timestamp))){
            return;
        }

//        StringBuilder builder = new StringBuilder();
//        builder.append(result.getDevice().getAddress()).append("\n");
//        builder.append("Chatman UUID: ").append(appUUID).append("\n");
//        builder.append("Remote-Host UUID: ").append(macUUID).append("\n");
//        String str = builder.toString();

        Message msg = Message.obtain(null, BLES_Constants.BLES_MSG_DEVICE_FOUND);
        if (msg != null) {
            Bundle bundle = new Bundle();
            bundle.putString(DEBUG_KEY_MAC_ADDRESSES,macUUID.toString());
            msg.setData(bundle);
            mService.get().sendBLESMessage(msg);
        }
//        Log.d(logTag, "Found " + macUUID + " !");
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        super.onBatchScanResults(results);
    }

    @Override
    public void onScanFailed(int errorCode) {
        switch (errorCode) {
            case ScanCallback.SCAN_FAILED_ALREADY_STARTED:
                Log.e(logTag, "SCAN_FAILED_ALREADY_STARTED");
                break;
            case ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                Log.e(logTag, "SCAN_FAILED_APPLICATION_REGISTRATION_FAILED");
                break;
            case ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED:
                Log.e(logTag, "SCAN_FAILED_FEATURE_UNSUPPORTED");
                break;
            case ScanCallback.SCAN_FAILED_INTERNAL_ERROR:
                Log.e(logTag, "SCAN_FAILED_INTERNAL_ERROR");
                break;
        }
        super.onScanFailed(errorCode);
    }
}
