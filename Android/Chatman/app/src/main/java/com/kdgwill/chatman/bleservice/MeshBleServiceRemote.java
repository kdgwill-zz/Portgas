package com.kdgwill.chatman.bleservice;

import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by kylewilliams on 5/11/16.
 */
public class MeshBleServiceRemote  extends Handler {
    protected String logTag = "Chatman/MeshBleServiceRemote :=";

    private final WeakReference<MeshBleService> mService;
    private final List<Messenger> mClients;

    public MeshBleServiceRemote(MeshBleService service) {
        mService = new WeakReference<>(service);
        mClients = new LinkedList<>();
    }

    @Override
    public void handleMessage(Message msg) {
        MeshBleService service = mService.get();

        if (service != null) {
            switch (msg.what) {
                case BLES_Constants.BLES_MSG_REGISTER:
                    mClients.add(msg.replyTo);
                    Log.d(logTag, "Client Registered");
                    break;
                case BLES_Constants.BLES_MSG_UNREGISTER:
                    mClients.remove(msg.replyTo);
                    Log.d(logTag, "Client Unregistered");
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    public List<Messenger> getResponders(){
        return mClients;
    }

    public void sendBLESMessage(Message msg) {
        for (int i = getResponders().size() - 1; i >= 0; i--) {
            Messenger messenger = getResponders().get(i);
            if (!sendBLESMessage(messenger, msg)) {
                getResponders().remove(messenger);
            }
        }
    }

    public boolean sendBLESMessage(Messenger messenger, Message msg) {
        boolean success = true;
        try {
            messenger.send(msg);
        } catch (RemoteException e) {
            Log.w(logTag, "Lost connection to client", e);
            success = false;
        }
        return success;
    }
}