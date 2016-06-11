package com.kdgwill.chatman.protocol.old.connectionless;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.kdgwill.chatman.R;
import com.kdgwill.chatman.protocol.old.impl.MeshActivity_OLD;

/**
 * Created by kylewilliams on 5/10/16.
 */
public class ConnectionlessMeshActivity extends MeshActivity_OLD {

    protected String logTag = "Chatman/ConnectionlessMeshActivity :=";
    private ConnectionlessMeshProtocol mp;
    private TextView mText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connetionless_activity_main);
        mText = (TextView) findViewById( R.id.cam_textBox );
    }

    @Override
    protected void onResume() {
        super.onResume();
        getProtocol().reader = new ConnectionlessMeshProtocol.CMPReadRequest() {
            @Override
            public void read(String str) {
                if(mText!=null){
                    mText.setText(str);
                }else{
                    Log.d(logTag,"mText NULL");
                    Log.d(logTag,str);
                }
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mp.cleanUp();
    }

    public ConnectionlessMeshProtocol getProtocol() {
        if (getBluetoothAdapter() == null) {
            //TODO: Throw error
            throw new NullPointerException("Impossible: Bluetooth Adapter is Null");
        }
        if (mp == null) {
            mp = new ConnectionlessMeshProtocol(this, bcf.getBluetoothManager());
        } else {
            //Weak links so reset just in case
            mp.setActivity(this);
            mp.setBluetoothManager(bcf.getBluetoothManager());
        }
        return mp;
    }

    public void advertise_btn(View view) {
        getProtocol().write("Hello".toString());
    }

}

