package com.example.android.bluetoothchat;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.common.logger.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.location.LocationRequest;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;

import static com.example.android.bluetoothchat.R.string.common_google_play_services_network_error_text;


/**
 * This fragment controls Bluetooth to communicate with other devices.
 */
public class HeartDashboardFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "BluetoothChatFragment";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;


    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;


    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services÷÷
     */
    private BluetoothChatService mChatService = null;

    private DBHelper db;

    protected LocationManager locationManager;
    protected LocationListener locationListener;

    String emergencyPhoneNumber;
    String emergencyName;

    LineGraphSeries<DataPoint> dataSeries = new LineGraphSeries<DataPoint>();
    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;

    protected String mLatitudeLabel;
    protected String mLongitudeLabel;
    TextView mLatitudeText;
    TextView mLongitudeText;

    private Activity myActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        myActivity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setContentView(R.layout.fragment_heart_dashboard);


        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            FragmentActivity activity = getActivity();
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }

        mLatitudeLabel = getResources().getString(R.string.latitude_label);
        mLongitudeLabel = getResources().getString(R.string.longitude_label);
        mLatitudeText = (TextView) getActivity().findViewById((R.id.latitude_text));
        mLongitudeText = (TextView) getActivity().findViewById((R.id.longitude_text));

        buildGoogleApiClient();


        db = DBHelper.getDBHelper(getContext());

        ArrayList<String> all = db.getAllContacts();

        if (all.size() != 0) {
            emergencyName = all.get(0);
            emergencyPhoneNumber = all.get(1);
        }
    }

    private void setContentView(int activity_main) {
    }


    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(myActivity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

    }


    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        // If BT is not on, request that it be enabled.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            mChatService = new BluetoothChatService(getActivity(), mHandler);
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(myActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(myActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            //    mLatitudeText.setText(String.format("%s: %f", mLatitudeLabel,
            //          mLastLocation.getLatitude()));
            //mLongitudeText.setText(String.format("%s: %f", mLongitudeLabel,
            //  mLastLocation.getLongitude()));
            Log.e("Latitude :", "" + mLastLocation.getLatitude());
            Log.e("Longitude :", "" + mLastLocation.getLongitude());
            AssignCoordinates(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            // mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
            //mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
        } else {
            Toast.makeText(myActivity, "No Last Connection is Found", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onConnectionSuspended ( int i){
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
        }
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

        initializeGraph();
    }


    private void initializeGraph() {
        GraphView graph = (GraphView) getView().findViewById(R.id.graph);
        graph.getViewport().setScrollable(true);
        graph.getViewport().setScalable(true);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(150);

        graph.addSeries(dataSeries);
        dataSeries.setDrawDataPoints(true);
        dataSeries.setDataPointsRadius(10); // set the radius of data point
        dataSeries.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Toast.makeText(getActivity(), "Series: On Data Point clicked: " + dataPoint, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateGraph(int x, int y) {
        dataSeries.appendData(new DataPoint(x, y), true, 100);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_heart_dashboard, container, false);
    }

    /**
     * Makes this device discoverable.
     */
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Updates the status on the action bar.
     *
     * @param resId a string resource ID
     */
    private void setStatus(int resId) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resId);
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    public final Handler mHandler = new Handler() {
        Calendar cal = Calendar.getInstance();
        int counter = 0;

        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            // mConversationArrayAdapter.clear();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    // mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    int BPM;

                    switch (readMessage.substring(0, 1)) {
                        case "B":
                            BPM = Integer.parseInt(readMessage.substring(1));
                            int x = counter++;
                            Log.i(TAG, Integer.toString(x));
                            Log.i(TAG, Integer.toString(BPM));

                            // Update the graph
                            updateGraph(x, BPM);

                            // Show the BPM
                            TextView hb = (TextView) getActivity().findViewById(R.id.heart_beat);
                            Log.i(TAG, hb.getText().toString());
                            hb.setText(Integer.toString(BPM));

                            // Animate img
                            ImageView img = (ImageView) getActivity().findViewById(R.id.heart_image);
                            Animation pulse = AnimationUtils.loadAnimation(getContext(), R.anim.heart_pulse);
                            img.startAnimation(pulse);

                            /*img.setImageResource(R.drawable.heart_big);

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    ImageView img = (ImageView) getActivity().findViewById(R.id.heart_image);
                                    img.setImageResource(R.drawable.heart_small);
                                }
                            }, 1000);*/

                            break;
                        case "L":
                        case "H":

                            if (ActivityCompat.checkSelfPermission(myActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(myActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                break;
                            }
                            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                            BPM = Integer.parseInt(readMessage.substring(1));
                            String message = "EMERGENCY!!!!!!! BPM of " + BPM + " detected!";
                            if (mLastLocation != null) {
                                //    mLatitudeText.setText(String.format("%s: %f", mLatitudeLabel,
                                //          mLastLocation.getLatitude()));
                                //mLongitudeText.setText(String.format("%s: %f", mLongitudeLabel,
                                //  mLastLocation.getLongitude()));
                                Log.e("Latitude :", "" + mLastLocation.getLatitude());
                                Log.e("Longitude :", "" + mLastLocation.getLongitude());
                                message += " Patient latitude: " + mLastLocation.getLatitude() + ", Patient longitude: " + mLastLocation.getLongitude() + ".";
                                // mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
                                //mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
                            } else {
                                Toast.makeText(myActivity, "No Last Connection is Found", Toast.LENGTH_LONG).show();
                            }
                            Log.i(TAG, message);
                            sendSMSMessage(message);
                            break;
                        case "X":
                        case "Y":
                            break;
                        default:
                            Log.i(TAG, "no valid input");
                    }
                    // mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    protected void sendSMSMessage(String message) {
        Log.i("Send SMS", "");

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(emergencyPhoneNumber, null, message, null, null);
            Toast.makeText(getContext(), "SMS sent.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "SMS faild, please try again.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    mChatService = new BluetoothChatService(getActivity(), mHandler);
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
        }
    }

    /**
     * Establish connection with other divice
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.bluetooth_chat, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.secure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            }
            case R.id.insecure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            }
            case R.id.discoverable: {
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
            }
            case R.id.go_to_setup: {
                startActivity(new Intent(getActivity(), SetupActivity.class));
            }
            case R.id.set_threshholds: {
                startActivity(new Intent(getActivity(), ThreshholdActivity.class));
            }
        }
        return false;
    }

    public void AssignCoordinates(double Latitude, double Longitude) {
        try {
            mLatitudeText.setText(String.valueOf(Latitude));
            mLongitudeText.setText(String.valueOf(Longitude));
        } catch (Exception e) {
            Log.e("AssignCoor Method", e.getMessage());
        } //if the app is crashed
    }
}
