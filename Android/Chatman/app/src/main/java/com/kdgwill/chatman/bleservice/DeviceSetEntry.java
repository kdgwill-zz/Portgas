package com.kdgwill.chatman.bleservice;

import android.bluetooth.BluetoothDevice;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;

import com.kdgwill.chatman.bleservice.message.MeshDatagram;

import java.util.LinkedList;

/**
 * Created by kylewilliams on 5/11/16.
 */
public class DeviceSetEntry extends Object{
    private final ParcelUuid mPUuid;
    private BluetoothDevice mDevice;//The interface continually updates so this has to be relevant
    private long mTimestamp;
    private LinkedList<ParcelUuid> mKnownDevices;
    private LinkedList<MeshDatagram> mMessageQueue;

    public DeviceSetEntry(@NonNull ParcelUuid mPUuid,
                          @NonNull BluetoothDevice mDevice,
                          long timestamp) {
        this.mPUuid = mPUuid;
        this.mDevice = mDevice;
        this.mTimestamp = timestamp;
    }

    public ParcelUuid getID(){return mPUuid;}
    public BluetoothDevice getDevice(){return mDevice;}
    public long getTimestamp(){return mTimestamp;}
    public void resetTimestamp(long newTime){mTimestamp = newTime;}
    public void resetDevice(BluetoothDevice btDevice){mDevice = btDevice;}
    public LinkedList<ParcelUuid> getKnownDeviceList(){
        if(mKnownDevices == null){
            mKnownDevices = new LinkedList<>();
        }
        return mKnownDevices;
    }
    public LinkedList<MeshDatagram> getMessageQueue(){
        if(mMessageQueue == null){
            mMessageQueue = new LinkedList<>();
        }
        return mMessageQueue;
    }
    public boolean hasMessagesToSend(){return !getMessageQueue().isEmpty();}
    public boolean addMessageQueue(MeshDatagram md){return getMessageQueue().add(md);}

    public boolean addKnownDeviceID(ParcelUuid id){return getKnownDeviceList().add(id);}
    public boolean hasKnownDeviceID(ParcelUuid id){return getKnownDeviceList().contains(id);}


    /////////////////////SPECIAL EQUALS AND HASH CODE FOCUS ONLY ON ParcelUuid for uniqueness
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeviceSetEntry that = (DeviceSetEntry) o;

        return mPUuid.equals(that.mPUuid);

    }

    @Override
    public int hashCode() {
        return mPUuid.hashCode();
    }
}
