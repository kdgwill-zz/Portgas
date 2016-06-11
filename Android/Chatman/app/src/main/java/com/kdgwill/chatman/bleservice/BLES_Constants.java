package com.kdgwill.chatman.bleservice;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.Random;
import java.util.UUID;

/**
 * Created by kylewilliams on 5/11/16.
 */
public final class BLES_Constants {
    ////////Services
    //Service UUID to write messages
    public static UUID SERVICE_UUID_MESSAGE
            = UUID.fromString("1706BBC0-88AB-4B8D-877E-2237916EE929");
    ////////Characteristics
    //Write characteristic write message
    public static UUID CHARACTERISTIC_UUID_MESSAGE
            = UUID.fromString("BD28E457-4026-4270-A99F-F9BC20182E15");
    ////////////////////////
    //This helps prevent the same state schedule
    public static final Random randomGenerator = new Random();
    private static final int minIdle = 500;//0.5secs
    private static final int maxIdle = 1000;//1.5secs
    private static final int minScan = 1000;//1 secs
    private static final int maxScan = 2000;//2 secs
    private static final int minAd = 2000;//2 secs
    private static final int maxAd = 4000;//4 secs

    public static final int IDLE_PERIOD(){
        return randomGenerator.nextInt(maxIdle - minIdle + 1) + minIdle;
    }

    public static final int SCAN_PERIOD(){
        return randomGenerator.nextInt(maxScan - minScan + 1) + minScan;
    }

    public static final int ADVERTISE_PERIOD(){
        return randomGenerator.nextInt(maxAd - minAd + 1) + minAd;
    }

    //Determine how long a remote device can remain undiscoverable by host before it is forgotten
    public static final int DEVICE_KEEPALIVE_PERIOD = 30000; // 30 secs

    public static final int BLES_MSG_REGISTER = 1;
    public static final int BLES_MSG_UNREGISTER = 2;
    public static final int BLES_MSG_STATE_CHANGED = 3;
    public static final int BLES_MSG_DEVICE_FOUND = 4;

    public static Integer shortUnsignedAtOffset(BluetoothGattCharacteristic characteristic, int offset) {
        Integer lowerByte = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
        Integer upperByte = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 1);

        return (upperByte << 8) + lowerByte;
    }

    //Returns time in millis
    public static long getCurrentTime(){
        return System.currentTimeMillis();
    }

    public enum BLES_STATE {
        UNKNOWN,
        IDLE,
        ADVERTISING,
        SCANNING,
        UPDATE_ENTRIES,
        PROCESS_ENTRIES
    }


}
