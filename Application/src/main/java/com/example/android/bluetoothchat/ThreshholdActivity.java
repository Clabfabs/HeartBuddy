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
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    private BluetoothChatService mChatService = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.threshhold_setup);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        Button saveButton = (Button) findViewById(R.id.button_save);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TextView Minimum_heart_rateTextview = (TextView) findViewById(R.id.set_min);
                TextView Maximum_heart_rateTextview = (TextView) findViewById(R.id.set_max);

                String Minimum_heart_rate = Minimum_heart_rateTextview.getText().toString();
                String Maximum_heart_rate = Maximum_heart_rateTextview.getText().toString();

                startActivity(new Intent(ThreshholdActivity.this, MainActivity.class));

            }
        });

    }

}

