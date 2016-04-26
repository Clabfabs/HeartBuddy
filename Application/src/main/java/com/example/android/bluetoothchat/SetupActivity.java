package com.example.android.bluetoothchat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.common.activities.SampleActivityBase;
import com.example.android.common.logger.Log;
import com.example.android.common.logger.LogWrapper;

import java.util.ArrayList;

public class SetupActivity extends SampleActivityBase {

    public static final String TAG = "SetupActivity";

    private DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        db = DBHelper.getDBHelper(this);

        final Button nextButton = (Button) findViewById(R.id.setup_next);
        final Button resetButton = (Button) findViewById(R.id.setup_reset);


        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                // Collect data to be added
                TextView nameTextView = (TextView) findViewById(R.id.setup_name);
                TextView phoneTextView = (TextView) findViewById(R.id.setup_phone);
                String name = nameTextView.getText().toString();
                String phone = phoneTextView.getText().toString();

                // Insert data in DB
                db.insertContact(name, phone);
                Log.i(TAG, "Inserted " + name + ", " + phone);

                // See if that worked
                ArrayList<String> allEntries = db.getAllContacts();
                for (String s : allEntries) {
                    Log.i(TAG, "Content of DB: " + s);
                }

                // Start main activity
                startActivity(new Intent(SetupActivity.this, MainActivity.class));
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                db.reset();

                CharSequence text = "Reset!";
                Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    @Override
    public void onResume() {
        ArrayList<String> all = db.getAllContacts();
        TextView currentName = (TextView) findViewById(R.id.current_name);

        // TODO: When also phone number is available, set phone number

        TextView currentPhone = (TextView) findViewById(R.id.current_phone);

        if (all.size() != 0) {
            currentName.setText(all.get(0));
        }

        super.onResume();
    }
}

// This is an example