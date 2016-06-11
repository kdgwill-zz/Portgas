package com.kdgwill.chatman.bleservice;

import android.content.Context;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kdgwill.chatman.R;

import java.util.UUID;

/**
 * Created by kylewilliams on 5/11/16.
 */
public class MacModule {

    public enum MAC_MODE{_default, SIXTEEN_BIT, THIRTY_TWO_BIT}
    private final MAC_MODE macMODE = MAC_MODE.THIRTY_TWO_BIT;
    private final ParcelUuid ChatMan_UUID;//The device type Identifier
    private final ParcelUuid BROADCAST_UUID;  //The unique device identifier for broadcasting
    private final ParcelUuid MAC_UUID;  //The unique device identifier

    public MacModule(@NonNull Context cntx){
        //Set UUIDs
        ChatMan_UUID = new ParcelUuid(UUID.fromString(
                //Lowercase Required By Spec 6.5.4
                cntx.getString(R.string.UUID_CHATMAN).toLowerCase()));
        BROADCAST_UUID = createBroadcastUUID();
        MAC_UUID = createMACUUID();
    }

    public MAC_MODE getMacMode(){return macMODE;}
    public ParcelUuid getMeshAppUUID(){return ChatMan_UUID;}
    public ParcelUuid getMACUUID(){return MAC_UUID;}
    public ParcelUuid getMeshBroadcastUUID(){return BROADCAST_UUID;}

    private ParcelUuid createBroadcastUUID(){
        // creating UUID
        UUID uid = null;
        switch (macMODE){
            case SIXTEEN_BIT:
                uid = UUID.fromString("0000ffff-0000-1000-8000-00805f9b34fb");
                break;
            case THIRTY_TWO_BIT:
                uid = UUID.fromString("ffffffff-0000-1000-8000-00805f9b34fb");
                break;
            case _default:
                uid = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
        }
//        return uuid
        return new ParcelUuid(uid);
    }

    private ParcelUuid createMACUUID(){
        //Create Special UUID for session
        String temp = UUID.randomUUID().toString().toLowerCase();
        //Convert Upper so can follow 32-bit transfer
        String s = TextUtils.split(temp,"-")[0];
        String newUUID = null; //"00000000-0000-1000-8000-00805f9b34fb";
        switch(macMODE){
            case SIXTEEN_BIT:
                //Append SIG generic postfix so that it only sends 16bits instead of 128bits
                String t = s.substring(s.length()/2);
                newUUID = "0000" + t + "-0000-1000-8000-00805f9b34fb";
                break;
            case THIRTY_TWO_BIT:
                //Append SIG generic postfix so that it only sends 32bits instead of 128bits
                newUUID = s + "-0000-1000-8000-00805f9b34fb";
                break;
            case _default:
                newUUID = temp;
        }
        //return UUID
        return new ParcelUuid(UUID.fromString(newUUID));
    }

    public static ParcelUuid createValidBleUUID(final String str){
        String newUuid;
        String upperPadding = "0000";
        String lowerBytes = "-0000-1000-8000-00805f9b34fb";
        switch(str.length()){
            case 4:
                newUuid = upperPadding + str + lowerBytes;
                break;
            case 8:
                newUuid = str + lowerBytes;
                break;
            default:
                return null;
        }
        return new ParcelUuid(UUID.fromString(newUuid.toLowerCase()));
    }
}
