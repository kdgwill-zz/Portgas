package com.kdgwill.chatman.bleservice.gatt;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.support.annotation.NonNull;
import android.util.Log;

import com.kdgwill.chatman.bleservice.BLES_Constants;
import com.kdgwill.chatman.bleservice.MeshBleService;

import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Created by kylewilliams on 5/11/16.
 */
public class MeshBleGattServer extends BluetoothGattServerCallback {
    protected String logTag = "Chatman/MeshBleGattServer :=";

    private final WeakReference<MeshBleService> mService;
    private BluetoothGattServer mGattServer;
    public ArrayList<BluetoothDevice> btDevs;

    public MeshBleGattServer(@NonNull MeshBleService service) {
        mService = new WeakReference<>(service);

         mGattServer = getBleService().getBluetoothManager()
                .openGattServer(getBleService(),this);

        btDevs = new ArrayList<>();

        initServer();
        getBleService().addClosingAction(new Runnable() {
            @Override
            public void run() {
                MeshBleGattServer.this.shutdownServer();
            }
        });
    }
    /*
         * Create the GATT server instance, attaching all services and
         * characteristics that should be exposed
         */
    private void initServer() {
        mGattServer = getBleService().getBluetoothManager()
                        .openGattServer(getBleService(), this);

        //Set Up services and characteristics
        BluetoothGattService service =
                new BluetoothGattService(BLES_Constants.SERVICE_UUID_MESSAGE,
                        BluetoothGattService.SERVICE_TYPE_PRIMARY);

        //add a read characteristic.
        BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(
                BLES_Constants.CHARACTERISTIC_UUID_MESSAGE,
                        BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                        BluetoothGattCharacteristic.PERMISSION_WRITE);
        service.addCharacteristic(characteristic);
        mGattServer.addService(service);
    }

    private void shutdownServer(){
        mGattServer.clearServices();
        mGattServer.close();
        mGattServer = null;
    }

    private MeshBleService getBleService(){
        if(mService==null){
            //Todo: What to do if service link was removed
        }
        return mService.get();
    }
    public BluetoothGattServer getBluetoothGattServer(){return mGattServer;}

    @Override
    public void onConnectionStateChange(BluetoothDevice device, int status,
                                        int newState) {
        super.onConnectionStateChange(device, status, newState);

        if (BluetoothGatt.GATT_SUCCESS == status) {
            btDevs.add(device);
        } else {
            btDevs.remove(device);
        }
    }

    /**
     * A remote client has requested to read a local characteristic.
     *
     * <p>An application must call {@link BluetoothGattServer#sendResponse}
     * to complete the request.
     *
     * @param device The remote device that has requested the read operation
     * @param requestId The Id of the request
     * @param offset Offset into the value of the characteristic
     * @param characteristic Characteristic to be read
     */
    @Override
    public void onCharacteristicReadRequest(BluetoothDevice device, int requestId,
                                            int offset, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
        mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                characteristic.getValue());
    }

    /**
     * A remote client has requested to write to a local characteristic.
     *
     * <p>An application must call {@link BluetoothGattServer#sendResponse}
     * to complete the request.
     *
     * @param device The remote device that has requested the write operation
     * @param requestId The Id of the request
     * @param characteristic Characteristic to be written to.
     * @param preparedWrite true, if this write operation should be queued for
     *                      later execution.
     * @param responseNeeded true, if the remote device requires a response
     * @param offset The offset given for the value
     * @param value The value the client wants to assign to the characteristic
     */
    @Override
    public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                                             BluetoothGattCharacteristic characteristic,
                                             boolean preparedWrite, boolean responseNeeded,
                                             int offset, byte[] value) {
        super.onCharacteristicWriteRequest(device, requestId, characteristic,
                preparedWrite, responseNeeded, offset, value);
//
//        mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
//                characteristic.getValue());

        Log.d(logTag,new String(value, StandardCharsets.UTF_8));
    }

    /**
     * A remote client has requested to read a local descriptor.
     *
     * <p>An application must call {@link BluetoothGattServer#sendResponse}
     * to complete the request.
     *
     * @param device The remote device that has requested the read operation
     * @param requestId The Id of the request
     * @param offset Offset into the value of the characteristic
     * @param descriptor Descriptor to be read
     */
    @Override
    public void onDescriptorReadRequest(BluetoothDevice device, int requestId,
                                        int offset, BluetoothGattDescriptor descriptor) {
        super.onDescriptorReadRequest(device, requestId, offset, descriptor);
        mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                descriptor.getValue());
    }

    /**
     * A remote client has requested to write to a local descriptor.
     *
     * <p>An application must call {@link BluetoothGattServer#sendResponse}
     * to complete the request.
     *
     * @param device The remote device that has requested the write operation
     * @param requestId The Id of the request
     * @param descriptor Descriptor to be written to.
     * @param preparedWrite true, if this write operation should be queued for
     *                      later execution.
     * @param responseNeeded true, if the remote device requires a response
     * @param offset The offset given for the value
     * @param value The value the client wants to assign to the descriptor
     */
    @Override
    public void onDescriptorWriteRequest(BluetoothDevice device, int requestId,
                                         BluetoothGattDescriptor descriptor,
                                         boolean preparedWrite, boolean responseNeeded,
                                         int offset,  byte[] value) {
        super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite,
                responseNeeded, offset, value);
//        mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
//                descriptor.getValue());
        Log.d(logTag,new String(value, StandardCharsets.UTF_8));
    }

    /**
     * Execute all pending write operations for this device.
     *
     * <p>An application must call {@link BluetoothGattServer#sendResponse}
     * to complete the request.
     *
     * @param device The remote device that has requested the write operations
     * @param requestId The Id of the request
     * @param execute Whether the pending writes should be executed (true) or
     *                cancelled (false)
     */
    @Override
    public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
        super.onExecuteWrite(device, requestId, execute);
        mGattServer.sendResponse(device,requestId,BluetoothGatt.GATT_SUCCESS,0,null);
    }

    @Override
    public void onNotificationSent(BluetoothDevice device, int status) {
        super.onNotificationSent(device,status);
    }

    @Override
    public void onMtuChanged(BluetoothDevice device, int mtu) {
        super.onMtuChanged(device, mtu);
    }

}
