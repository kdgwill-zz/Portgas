package com.kdgwill.chatman.bleservice.gatt;

import android.support.annotation.NonNull;

import com.kdgwill.chatman.bleservice.MeshBleService;
import com.kdgwill.chatman.bleservice.MeshBleServiceRemote;

import java.lang.ref.WeakReference;

/**
 * Created by kylewilliams on 5/18/16.
 */
public class MeshBleConnectionProtocolSuite {
    protected String logTag = "Chatman/MeshBleConnectionProtocolSuite :=";

    private final WeakReference<MeshBleService> mService;
    private final WeakReference<MeshBleServiceRemote> mHandler;

    public final MeshBleGattServer mServer;
    private final MeshBleGattClient mClient;

    public MeshBleConnectionProtocolSuite(@NonNull MeshBleService service,
                                          MeshBleServiceRemote handler) {
        mService = new WeakReference<>(service);
        mHandler = new WeakReference<>(handler);

        mServer = new MeshBleGattServer(service);
        mClient = new MeshBleGattClient(service);
    }

    public MeshBleGattClient getClient(){return mClient;}
    public MeshBleGattServer getServer(){return mServer;}
    private MeshBleService getService(){return mService.get();}
    private MeshBleServiceRemote getHandler(){return mHandler.get();}

}
