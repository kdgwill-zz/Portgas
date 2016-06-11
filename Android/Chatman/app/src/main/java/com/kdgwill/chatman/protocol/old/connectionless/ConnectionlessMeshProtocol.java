package com.kdgwill.chatman.protocol.old.connectionless;

import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.ParcelUuid;
import android.util.Log;

import com.kdgwill.chatman.protocol.ConnectionlessProtocol;
import com.kdgwill.chatman.protocol.old.impl.BLEMeshProtocol;
import com.kdgwill.chatman.protocol.old.impl.MeshActivity_OLD;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * ONLY FOR DEMONSTRATION SAKE!!
 *
 * A very simple connectionless protocol for ble mesh networking
 * <p>
 * This protocol simply floods the network with intended messages, upon receiving a message,
 * it first checks to see if the message is already cached; If it is it drops the message,
 * in the event that it is a new message it caches the message for a set duration rebroadcasts
 * the message
 * <p>
 * This Protocol does not support fragmentation or directed messages as the overhead of
 * the message organization would be to high do to hardware limitations of Bluetooth Low Energy
 * <p>
 * Created by Kyle D. Williams on 3/14/16.
 * ~Hail-Innovation
 */
public class ConnectionlessMeshProtocol extends BLEMeshProtocol implements ConnectionlessProtocol {

    protected String logTag = "Chatman/ConnectionlessMeshProtocol := ";


    public ConnectionlessMeshProtocol(MeshActivity_OLD activity) {
        super(activity);
    }

    public ConnectionlessMeshProtocol(MeshActivity_OLD activity, BluetoothManager bluetoothManager) {
        super(activity, bluetoothManager);

        List<ScanFilter> filters = new ArrayList<>();
        ScanFilter filter = new ScanFilter.Builder()
                .setServiceUuid(getActivity().getMeshAppUUID())
                .build();
        filters.add(filter);

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .build();
        getBLEScanner().stopScan(discoveryCallBack);
        getBLEScanner().startScan(filters, settings, discoveryCallBack);
    }

    public interface CMPReadRequest{void read(String str);}
    public CMPReadRequest reader;


    public void write(String str) {
        getBLEAdvertiser().stopAdvertising(advertisingCallback);
        /**
         TODO:
         Settings are source of battery drain for BLE Use step up and step down of power
         level to control discoverability. If can connect to at least 1 person then
         Mesh should allow balanced
         */
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
//                .setAdvertiseMode( AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY )
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(true)
                .setTimeout(300)
                .build();

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                .addServiceUuid(getActivity().getMeshAppUUID())
                .addServiceData(MAC_UUID,str.getBytes(Charset.forName( "UTF-8" ) ))
                .build();

        getBLEAdvertiser().startAdvertising(settings, data, advertisingCallback);
    }

    AdvertiseCallback advertisingCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
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
            super.onStartFailure(errorCode);
        }
    };

    private ScanCallback discoveryCallBack = new ScanCallback() {

        HashMap<ParcelUuid,HashSet<String>> set = new HashMap<>();

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (result == null || result.getDevice() == null) {
                return;
            }
            StringBuilder builder = new StringBuilder();
            Map<ParcelUuid, byte[]> data = result.getScanRecord().getServiceData();
            ParcelUuid key = (ParcelUuid) data.keySet().toArray()[0];
            builder.append(new String(data.get(key), Charset.forName("UTF-8"))).append("\n");

            String str = builder.toString();

            if(!set.containsKey(key)){
                set.put(key,new HashSet<String>());
            }

            if(set.get(key).contains(str)){
                return;
            }

            set.get(key).add(str);
            if(reader!=null){
                reader.read(str);
                ConnectionlessMeshProtocol.this.write(str);
            }
        }

        @Override
        public void onBatchScanResults(List<android.bluetooth.le.ScanResult> results) {
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
    };

    @Override
    public void cleanUp() {
        getBLEScanner().stopScan(discoveryCallBack);
        getBLEAdvertiser().stopAdvertising(advertisingCallback);
    }
}























