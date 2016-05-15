package com.example.android.bluetoothchat;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.common.activities.SampleActivityBase;

/**
 * Created by subash on 11/05/2016.
 */
public class ThreshholdActivity extends SampleActivityBase {

    public static final String TAG = "ThreshholdActivity";


    private static final int REQUEST_ENABLE_BT = 3;


    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter;

    /**
     * Member object for the chat services
     */
    private BluetoothChatService mChatService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_threshhold);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        final Button saveButton = (Button) findViewById(R.id.save_threshhold);

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            mChatService = new BluetoothChatService(ThreshholdActivity.this, mHandler);
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TextView min_text = (TextView) findViewById(R.id.set_min);
                TextView max_text = (TextView) findViewById(R.id.set_max);

                String min_string = min_text.getText().toString();
                String min_msg = "L:" + min_string + "#"; // For "lower threshhold"
                byte[] min = min_msg.getBytes();

                String max_string = max_text.getText().toString();
                String max_msg = "U: " + max_string + "#"; // For "upper threshhold"
                byte[] max = max_msg.getBytes();

                mChatService.write(min);
                mChatService.write(max);

                Toast toast = Toast.makeText(getApplicationContext(), "sent to Arduino", Toast.LENGTH_SHORT);
                toast.show();

                startActivity(new Intent(ThreshholdActivity.this, MainActivity.class));

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    /**
     * The Handler that sends information with the BluetoothChatService
     */
    public final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Constants.MESSAGE_WRITE) {

                // Do something with the sent message


                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
                // mConversationArrayAdapter.add("Me:  " + writeMessage);
            }
        }
    };
}

