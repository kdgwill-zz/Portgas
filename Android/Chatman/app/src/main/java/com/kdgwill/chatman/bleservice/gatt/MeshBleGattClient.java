package com.kdgwill.chatman.bleservice.gatt;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.util.Log;

import com.kdgwill.chatman.bleservice.BLES_Constants;
import com.kdgwill.chatman.bleservice.DeviceSetEntry;
import com.kdgwill.chatman.bleservice.MeshBleService;
import com.kdgwill.chatman.bleservice.message.MeshDatagram;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by kylewilliams on 5/11/16.
 */
public class MeshBleGattClient {

    protected String logTag = "Chatman/MeshBleGattClient :=";

    private final WeakReference<MeshBleService> mService;
    //Hashmap of ParcelUUID with a device set and it's message queue
    private HashMap<ParcelUuid, DeviceSetEntry> mCachedDevies;


    //Either a descriptor or characteristic
    private final Queue<Object> mCurrentMessageQueue;
    private DeviceSetEntry mCurrentDSE = null;
    private BluetoothGatt mCurrentGatt = null;


    private Handler mHandler;
    private boolean mIsWriting = false;


    public MeshBleGattClient(@NonNull MeshBleService service) {
        mService = new WeakReference<>(service);
        mCachedDevies = new HashMap<>();
        mHandler = new Handler(service.getApplicationContext().getMainLooper());
        mCurrentMessageQueue = new ConcurrentLinkedQueue<Object>();
    }

    public MeshBleService getBleService() {
        if (mService == null) {
            //Todo: What to do if service link was removed
        }
        return mService.get();
    }

    //TODO: At a later time add list instead of calling service deque like process entries
    public void processDiscoveries(Runnable done) {
        //TODO: PROPER DISCOVERY PHASE

        //Cycle all eligible devices and add to list
        DeviceSetEntry entry;

        while ((entry = getBleService().dequeDeviceInWaitList()) != null) {
            // if already in list update timestamp
            if (mCachedDevies.containsKey(entry.getID())) {
                //Update timestamp for entry
                long currentTime = System.currentTimeMillis();
                DeviceSetEntry origEntry = mCachedDevies.get(entry.getID());
                origEntry.resetTimestamp(currentTime);
            }
            //TODO: IMPORTANT: Determine if device is useful somehow and don't add if not
            // 1. Connect to server
            // 2. Send Request for Unique Device List(List of connected devices)
            // 3. When Server returns determine if want to add server to own list
            //      If no return Null/0 if yes return Unique Device List
            // 4. If Server returns yes update(i.e add new device and if need be remove old one)
            //      list and set flag to update other connected devices servers on next connect
            // 5. Terminate link to server
            // * Note since only 3-4 connections allowed should be able to sent each 32 bit UUID
            // in a single 20 byte(160 bit) packet by shifting the raw UUIDs into place
            //** For now just indiscriminately add device
            mCachedDevies.put(entry.getID(), entry);
        }
        done.run();
    }

    public void processEntries(Runnable done, Set<MeshDatagram> addToMessagesQueue) {
        for (MeshDatagram md : addToMessagesQueue) {
//            if (mCachedDevies.containsKey(md.destinationAddress)) {
//                //Check if anyone you know is the destination
//                mCachedDevies.get(md.destinationAddress).addMessageQueue(md);
//                continue;
//            } else {
//                //Check if anyone you know knows the destination
//                for (DeviceSetEntry p : mCachedDevies.values()) {
//                    if (p.hasKnownDeviceID(md.destinationAddress)) {
//                        //Check if anyone you know is the destination
//                        p.addMessageQueue(md);
//                        continue;
//                    }
//                }
//            }
            //broadcast to everyone if get to this point
            for (DeviceSetEntry p : mCachedDevies.values()) {
                p.addMessageQueue(md);
            }
        }
        processEntries(done);
    }

    private void processEntries(Runnable done) {
//      Loop Through Main List
        mCurrentDSE = null;
        mCurrentMessageQueue.clear();
        closeGatt();

        for (DeviceSetEntry e : mCachedDevies.values()) {
            //TODO: For now this will be blocking if another has a long write it may be forced to wait
            if (e.hasMessagesToSend()) {
                mCurrentDSE = e;
                mCurrentMessageQueue.addAll(e.getMessageQueue());
                e.getMessageQueue().clear();
                break;
            }
        }
        Log.d(logTag,11+"");

        if (mCurrentDSE == null) {
            done.run();
            return;
        }
        Log.d(logTag,12+"");

        long timeSinceLastDiscovered = BLES_Constants.getCurrentTime() - mCurrentDSE.getTimestamp();
        boolean exceededTime = BLES_Constants.DEVICE_KEEPALIVE_PERIOD < timeSinceLastDiscovered;

        //Rule of Thumb call cancelDiscovery before connect
        getBleService().getBluetoothAdapter().cancelDiscovery();
        //Autoconnect true so that if the connection to the sensor is lost,
        // the proxy will attempt to restore the connection for us without any prompting.
        mCurrentGatt = mCurrentDSE.getDevice()
                .connectGatt(MeshBleGattClient.this.getBleService(),
                        true, mGattCallback);
//        refreshDeviceCache(mCurrentGatt);

        //If Cannot connect and discovery timer has exceeded remove it
        if (mCurrentGatt == null && exceededTime) {
            mCachedDevies.remove(mCurrentDSE);
            processEntries(done);
            return;
        }

        Log.d(logTag,13+"");

        //Update last seen timestamp
        //THIS WILL NOT SERVER THE PURPOSE CORRECTLY AT THIS POINT
        mCurrentDSE.resetTimestamp(BLES_Constants.getCurrentTime());
    }

    private boolean refreshDeviceCache(BluetoothGatt gatt) {
        try {
            //Use Reflection to get internal call to refresh To possibly help with a problem
            BluetoothGatt localBluetoothGatt = gatt;
            Method localMethod = localBluetoothGatt.getClass().getMethod("refresh");
            if (localMethod != null) {
                boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt)).booleanValue();
                return bool;
            }
        } catch (Exception localException) {
            Log.e(logTag, "An exception occured while refreshing device");
        }
        return false;
    }

    private void closeGatt() {
        //Rule of Thumb call cancelDiscovery before connect
        getBleService().getBluetoothAdapter().cancelDiscovery();
        //DISCONNECT ALLOWS YOU TO RECONNECT AT A LATER TIME BUT USES LIMTIED BLE INTERFACES
        //gatt.disconnect();
        //CALL CLOSE WILL END SERVICE AND RELEASE RESOURCES
        if(mCurrentGatt!=null){
            mCurrentGatt.close();
            mCurrentGatt = null;
        }
    }


    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.d(logTag,"1111111111");

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                getBleService().getBluetoothAdapter().cancelDiscovery();
                gatt.discoverServices();
                Log.d(logTag,10+"");

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                //TODO: State disconnected what to do here
                closeGatt();
                Log.d(logTag,100+"");

            }
            Log.d(logTag,"323424234234");

        }


        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.v(logTag, "onServicesDiscovered: " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //Now that we are connected and Discovered Services
                // add queue to write queue and start writing
                startWrite(gatt);
                Log.d(logTag,5+"");

            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            mIsWriting = false;
            nextWrite();
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            mIsWriting = false;
            nextWrite();
        }

    };

    private void startWrite(BluetoothGatt gatt) {
        BLES_Constants.BLES_STATE mState = getBleService().getState();
        switch (mState) {
            case UPDATE_ENTRIES:
                break;
            case PROCESS_ENTRIES:
                BluetoothGattService messageService = mCurrentGatt
                        .getService(BLES_Constants.SERVICE_UUID_MESSAGE);
                Log.d(logTag,1+"");
                if(messageService!=null){
                    BluetoothGattCharacteristic messageCharacteristic =
                            messageService.getCharacteristic(BLES_Constants.CHARACTERISTIC_UUID_MESSAGE);
                    if(messageCharacteristic!=null){
                        Log.d(logTag,2+"");

                        //TODO: Support split packets
                        mCurrentMessageQueue.clear();//just clear for now
                        messageCharacteristic.setValue("Hello");
                        write(messageCharacteristic);
                    }
                }
                break;
            default:
        }
    }

    private synchronized void write(Object o) {
        if (mCurrentMessageQueue.isEmpty() && !mIsWriting) {
            doWrite(o);
        } else {
            mCurrentMessageQueue.add(o);
        }
    }

    private synchronized void nextWrite() {
        if (!mCurrentMessageQueue.isEmpty() && !mIsWriting) {
            doWrite(mCurrentMessageQueue.poll());
        }
    }

    private synchronized void doWrite(Object o) {
        if (o instanceof BluetoothGattCharacteristic) {
            mIsWriting = true;
            mCurrentGatt.writeCharacteristic((BluetoothGattCharacteristic) o);
        } else if (o instanceof BluetoothGattDescriptor) {
            mIsWriting = true;
            mCurrentGatt.writeDescriptor((BluetoothGattDescriptor) o);
        } else {
            nextWrite();
        }
    }

}
