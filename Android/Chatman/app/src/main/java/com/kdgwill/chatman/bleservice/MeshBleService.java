package com.kdgwill.chatman.bleservice;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.kdgwill.chatman.bleservice.BLES_Constants.BLES_STATE;
import com.kdgwill.chatman.bleservice.gap.MeshBleDiscoveryProtocol;
import com.kdgwill.chatman.bleservice.gatt.MeshBleConnectionProtocolSuite;
import com.kdgwill.chatman.protocol.SimpleMeshProtocol;

import java.util.LinkedList;

public class MeshBleService extends Service implements SimpleMeshProtocol {
    protected String logTag = "Chatman/MeshBleService :=";

    private final MeshBleServiceRemote mHandler;
    private final Messenger mMessenger;

    private MacModule macModule;
    private BluetoothManager btmgr;
    private BluetoothAdapter btadptr;
    private MeshBleDiscoveryProtocol mDiscoveryProtocol;
    public MeshBleConnectionProtocolSuite mCPS;
    private LinkedList<Runnable> closingRemarks;
    private BLES_STATE mState = BLES_STATE.UNKNOWN;



    public MeshBleService() {
        mHandler = new MeshBleServiceRemote(this);
        mMessenger = new Messenger(mHandler);
        closingRemarks = new LinkedList<Runnable>();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(logTag, "onCreate");
        macModule = new MacModule(this);
        mDiscoveryProtocol = new MeshBleDiscoveryProtocol(this, mHandler);
        mCPS = new MeshBleConnectionProtocolSuite(this, mHandler);

        //This is a state Machine That guides the service
        nextState();

    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(logTag, "onBind");
        return mMessenger.getBinder();
    }

    @Override
    public void onDestroy() {
        while (!closingRemarks.isEmpty()) {
            closingRemarks.poll().run();
        }
        super.onDestroy();
    }

    public void addClosingAction(Runnable run) {
        closingRemarks.add(run);
    }

    public BluetoothManager getBluetoothManager() {
        if (btadptr == null) {
            btmgr = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            //TODO: What to do if can't access btmgr before BluetoothControllerFragment handles it
        }
        return btmgr;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        if (btadptr == null) {
            btadptr = getBluetoothManager().getAdapter();
            //TODO: What to do if can't access btadptr before BluetoothControllerFragment handles it
        }
        return btadptr;
    }

    public MacModule getMacModule() {
        return macModule;
    }

    public void sendBLESMessage(Message msg) {
        mHandler.sendBLESMessage(msg);
    }

    public DeviceSetEntry dequeDeviceInWaitList() {
        return mDiscoveryProtocol.dequeDeviceInWaitList();
    }
//////////////////////////STATE MACHINE
    public void idleState() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                nextState();
            }
        }, BLES_Constants.IDLE_PERIOD());
    }

    //TODO: Create an injectionState to interupt current. This can work since mHandler works asychonously
    private void nextState() {
        if(mState==BLES_STATE.UNKNOWN){
            mState = BLES_STATE.ADVERTISING;
        }else {
            int len = BLES_Constants.BLES_STATE.values().length;
            int state = (mState.ordinal() + 1) % len;
            state = (state == 0) ? state + 1 : state;//never unknown again
            mState = BLES_Constants.BLES_STATE.values()[state];
        }
        setsState(mState);
//        Log.d(logTag,"Change State: " + mState.name());
        switch (mState) {
            case IDLE:
                idleState();
                break;
            case ADVERTISING:
                mDiscoveryProtocol.startAdvertise(nextState);
                break;
            case SCANNING:
                mDiscoveryProtocol.startScan(nextState);
                break;
//            case UPDATE_ENTRIES:
//                mCPS.getClient().processDiscoveries(nextState);
//                break;
//            case PROCESS_ENTRIES:
//                Set<MeshDatagram> addToMessagesQueue = new HashSet<>();
//                ParcelUuid ds = MacModule.createValidBleUUID("ffff");
//                addToMessagesQueue.add(new MeshDatagram(ds,ds,new Object()));
//                mCPS.getClient().processEntries(nextState,addToMessagesQueue);
//                break;
            default:
                //TODO: What to do if in invalid state?
                nextState();
        }
//        Log.d(logTag,"State Ended: " + mState.name());
    }

    private void setsState(BLES_STATE newState) {
        if (mState != newState) {
            mState = newState;
            Message msg = getStateMessage();
            if (msg != null) {
                sendBLESMessage(msg);
            }
        }
    }

    public BLES_STATE getState(){
        return mState;
    }

    private Message getStateMessage() {
        Message msg = Message.obtain(null, BLES_Constants.BLES_MSG_STATE_CHANGED);
        if (msg != null) {
            msg.arg1 = mState.ordinal();
        }
        return msg;
    }

    private Runnable nextState = new Runnable() {
        @Override
        public void run() {
            nextState();
        }
    };
}
