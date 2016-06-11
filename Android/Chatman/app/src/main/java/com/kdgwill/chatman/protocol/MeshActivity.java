package com.kdgwill.chatman.protocol;

import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.kdgwill.chatman.BluetoothControllerFragment;
import com.kdgwill.chatman.R;
import com.kdgwill.chatman.bleservice.BLES_Constants;
import com.kdgwill.chatman.bleservice.MeshBleService;
import com.kdgwill.chatman.bleservice.gap.MeshBleGapCentral;

import java.lang.ref.WeakReference;

/**
 * Created by kylewilliams on 5/10/16.
 */
public class MeshActivity extends AppCompatActivity {
    private TextView mText;

    protected String logTag = "Chatman/MeshActivity := ";
    protected BluetoothControllerFragment bcf;

    private final Messenger mMessenger;
    private Intent mServiceIntent;
    private Messenger mService = null;
//    private BLES_DISCOVERY_STATE mState = BLES_DISCOVERY_STATE.UNKNOWN;


    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
            try {
                Message msg = Message.obtain(null, BLES_Constants.BLES_MSG_REGISTER);
                if (msg != null) {
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } else {
                    mService = null;
                }
                Log.d(logTag,"Service Connection Successful");
            } catch (Exception e) {
                Log.w(logTag, "Error connecting to BleService", e);
                mService = null;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    public MeshActivity() {
        super();
        mMessenger = new Messenger(new MeshBeeServiceReceiverHandler(this));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //First see if it is already created
        bcf = (BluetoothControllerFragment)getSupportFragmentManager()
                .findFragmentByTag("BluetoothControllerFragment");
//                .findFragmentById(R.id.BluetoothControllerFragment);
        if(bcf == null) {
            bcf = new BluetoothControllerFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(bcf,"BluetoothControllerFragment")
//                    .add(R.id.BluetoothControllerFragment,bcf)
                    .commit();
        }

        mServiceIntent = new Intent(this, MeshBleService.class);

        setContentView(R.layout.activity_main);
        mText = (TextView) findViewById( R.id.cam_textBox );
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(mServiceIntent, mConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        if (mService != null) {
            try {
                Message msg = Message.obtain(null, BLES_Constants.BLES_MSG_UNREGISTER);
                if (msg != null) {
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                }
            } catch (Exception e) {
                Log.w(logTag, "Error unregistering with BleService", e);
                mService = null;
            } finally {
                unbindService(mConnection);
            }
        }
        super.onStop();
    }

    public BluetoothAdapter getBluetoothAdapter(){return bcf.getBluetoothAdapter();}

    private static class MeshBeeServiceReceiverHandler extends Handler {
        private final WeakReference<MeshActivity> mActivity;

        public MeshBeeServiceReceiverHandler(MeshActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MeshActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
//                    case BLES_Constants.BLES_MSG_STATE_CHANGED:
//                        activity.stateChanged(BLES_DISCOVERY_STATE.values()[msg.arg1]);
//                        break;
                    case BLES_Constants.BLES_MSG_DEVICE_FOUND:
                        Bundle data = msg.getData();
                        if (data != null && data.containsKey(MeshBleGapCentral.DEBUG_KEY_MAC_ADDRESSES)) {
                            String str = data.getString(MeshBleGapCentral.DEBUG_KEY_MAC_ADDRESSES);
                            activity.mText.setText(str);
                        }
                        break;
                }
            }
            super.handleMessage(msg);
        }
    }
//    private void stateChanged(BLES_DISCOVERY_STATE newState) {
//        mState = newState;
//        switch (mState) {
//            case IDLE:
//                break;
//            case ADVERTISING:
//                break;
//            case SCANNING:
//                break;
//            default:
//                //TODO: What to do if in invalid state?
//        }
//    }

}
