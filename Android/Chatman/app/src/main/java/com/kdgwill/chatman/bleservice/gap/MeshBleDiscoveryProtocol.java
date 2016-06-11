package com.kdgwill.chatman.bleservice.gap;

import android.support.annotation.NonNull;

import com.kdgwill.chatman.bleservice.BLES_Constants;
import com.kdgwill.chatman.bleservice.DeviceSetEntry;
import com.kdgwill.chatman.bleservice.MeshBleService;
import com.kdgwill.chatman.bleservice.MeshBleServiceRemote;

import java.lang.ref.WeakReference;

/**
 * Created by kylewilliams on 5/18/16.
 */
public class MeshBleDiscoveryProtocol {
    protected String logTag = "Chatman/MeshBleDiscoveryProtocol :=";

    private final WeakReference<MeshBleServiceRemote> mHandler;

    private final MeshBleGapCentral mScanner;
    private final MeshBleGapPeripheral mAdvertiser;

    public MeshBleDiscoveryProtocol(@NonNull MeshBleService service,MeshBleServiceRemote handler) {
        mHandler = new WeakReference<>(handler);

        mScanner = new MeshBleGapCentral(service);
        mAdvertiser = new MeshBleGapPeripheral(service);
    }

    public MeshBleGapCentral getScanner(){return mScanner;}
    public MeshBleGapPeripheral getAdvertiser(){return mAdvertiser;}

    public DeviceSetEntry dequeDeviceInWaitList() {
        return getScanner().dequeueDevice();
    }

    private MeshBleServiceRemote getHandler(){
        return mHandler.get();
    }

    public void startAdvertise(final Runnable run) {
        getAdvertiser().stopAdvertise();

        getAdvertiser().startAdvertise();
        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mAdvertiser.stopAdvertise();
                if(run!=null){
                    run.run();
                }
            }
        }, BLES_Constants.ADVERTISE_PERIOD());
    }

    public void startScan(final Runnable run) {
        getScanner().stopScan();

        getScanner().startScan();
        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mScanner.stopScan();
                if(run!=null){
                    run.run();
                }
            }
        }, BLES_Constants.SCAN_PERIOD());
    }



}
