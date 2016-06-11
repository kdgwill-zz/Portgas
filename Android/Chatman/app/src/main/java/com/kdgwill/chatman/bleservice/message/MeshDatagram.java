package com.kdgwill.chatman.bleservice.message;

import android.os.ParcelUuid;
import android.support.annotation.NonNull;

/**
 * Created by kylewilliams on 5/19/16.
 */
public class MeshDatagram {
    public final ParcelUuid destinationAddress;
    public final ParcelUuid sourceAddress;

    public final Object obj;//THIS IS THE ACTUAL DATA MESSAGE FOR NOW WE IGNORE

//    BLEDatagram
//    Destination Address int(we can use int because inherently we know the rest so use int and transform to hex (so use Integer.toHexString(int))
//    Source Address int (actually String.format("%08x", 1234) is more useful since can pad and use Integer.parseInt(i, 16); to get back int)
//    Type - byte
//    Data - Object


    public MeshDatagram(@NonNull ParcelUuid destinationAddress,
                        @NonNull ParcelUuid sourceAddress, @NonNull Object obj) {
        this.destinationAddress = destinationAddress;
        this.sourceAddress = sourceAddress;
        this.obj = obj;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MeshDatagram that = (MeshDatagram) o;

        if (!destinationAddress.equals(that.destinationAddress)) return false;
        if (!sourceAddress.equals(that.sourceAddress)) return false;
        return obj.equals(that.obj);

    }

    @Override
    public int hashCode() {
        int result = destinationAddress.hashCode();
        result = 31 * result + sourceAddress.hashCode();
        result = 31 * result + obj.hashCode();
        return result;
    }
}
