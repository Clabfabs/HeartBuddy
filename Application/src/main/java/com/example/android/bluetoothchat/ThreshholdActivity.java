package com.example.android.bluetoothchat;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.android.common.activities.SampleActivityBase;

/**
 * Created by subash on 11/05/2016.
 */
public class ThreshholdActivity extends SampleActivityBase {

    public static final String TAG = "ThreshholdActivity";

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


        Button saveButton = (Button) findViewById(R.id.save_threshhold);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TextView set_min = (TextView) findViewById(R.id.set_min);
                TextView set_max = (TextView) findViewById(R.id.set_max);

                String min = set_min.getText().toString();
                String max = set_max.getText().toString();

                startActivity(new Intent(ThreshholdActivity.this, MainActivity.class));

            }
        });

    }

}

