package com.tan.iotwfirebase;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.github.kayvannj.permission_utils.Func2;
import com.github.kayvannj.permission_utils.PermissionUtil;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.tan.iotwfirebase.Storage.AppPreferences;
import com.tan.iotwfirebase.Storage.IPreferenceConstants;
import com.tan.iotwfirebase.helper.AppUtils;
import com.tan.iotwfirebase.helper.ILocationConstants;
import com.tan.iotwfirebase.service.LocationService;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements ILocationConstants, IPreferenceConstants {

    //variable name
    TextView disTemp;
    TextView disHumi;
    TextView ledState;
    Switch mOn;
    Switch mAuto;
    LineChart tempGraph;
    Button setHome;

    private DatabaseReference mDatabaseReference;
    private Query tempQuery;
    private TempData tempData = new TempData();
    private ArrayList<TempData> mData = new ArrayList<>();
    private Long mHumi;
    private Long mLedState;
    private Long refTimestamp;
    private float homeLatitude;
    private float homeLongitude;
    private LocationService mLocationService = new LocationService();
    private AppPreferences appPreferences;
    private float homeDistance;


    protected static final String TAG = MainActivity.class.getSimpleName();

    /**
     * Receiver listening to Location updates and updating UI in activity
     */
    private LocationReceiver locationReceiver;

    /**
     * Permission util with callback mechanism to avoid boilerplate code
     * <p/>
     * https://github.com/kayvannj/PermissionUtil
     */
    private PermissionUtil.PermissionRequestObject mBothPermissionRequest;


    private FusedLocationProviderClient mFusedLocationClient;


    @SuppressLint("SetTextI18n")
    @Override
    protected void  onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        disTemp = (TextView) findViewById(R.id.temp_display);
        disHumi = (TextView) findViewById(R.id.humi_display);
        ledState = (TextView) findViewById(R.id.led_state);
        mOn = (Switch) findViewById(R.id.on_switch);
        mAuto = (Switch) findViewById(R.id.auto_switch);
        tempGraph = (LineChart) findViewById(R.id.tempGraph);
        setHome = (Button) findViewById(R.id.setHome);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        locationReceiver = new LocationReceiver();

        appPreferences = new AppPreferences(this);


       checkLedState();
       updateHumi();
       updateTemp();
       enableLed();
       setHome();

    }

    private void updateTemp(){

        tempQuery = mDatabaseReference.child("temperature").orderByChild("timestamp").limitToLast(1000);
        tempQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                if(dataSnapshot.exists()) {
                    mData.add(dataSnapshot.getValue(TempData.class));
                    disTemp.setText(mData.get(mData.size()-1).getTemp().toString() + "°C");

                    //graphing
                    ArrayList<Entry> mEntries = new ArrayList<Entry>();

                    refTimestamp = mData.get(0).getTimestamp()/1000;
                    IAxisValueFormatter xAxisFormater = new HourAxisValueFormatter(refTimestamp);
                    XAxis xAxis = tempGraph.getXAxis();
                    xAxis.setValueFormatter(xAxisFormater);

                    for(int x = 0; x<mData.size(); x++){

                        mEntries.add(new Entry(((mData.get(x).getTimestamp()/1000)-refTimestamp), mData.get(x).getTemp()));
                    }

                    LineDataSet dataSet = new LineDataSet(mEntries, "Temperature");
                    LineData lineData = new LineData(dataSet);
                    lineData.setDrawValues(false);
                    MyMarkerView myMarkerView= new MyMarkerView(getApplicationContext(), R.layout.my_marker_view_layout, refTimestamp);
                    tempGraph.setMarker(myMarkerView);
                    tempGraph.setData(lineData);
                    tempGraph.invalidate();

                    //debug
                    Log.d("Iot", mData.get(0).getTemp().toString());
                    Log.d("Iot", "size : " + String.valueOf(mData.size()));


                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {


            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateHumi(){

        mDatabaseReference.child("humi").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    mHumi = (Long) dataSnapshot.getValue();
                    Log.d("Iot", "humi = " + mHumi.toString());
                    disHumi.setText(mHumi.toString() + " %");
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }

    private void checkLedState(){

        mDatabaseReference.child("led_state").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()) {
                    mLedState = (Long) dataSnapshot.getValue();

                    if (mLedState == 1){

                        ledState.setText("LED is ON");
                        Log.d("Iot", "led on");
                    }else{

                        ledState.setText("LED is OFF");
                        Log.d("Iot", "led off");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void enableLed(){

        mOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked == true){

                    try {
                        mDatabaseReference.child("led_switch").setValue(1);
                        Log.d("Iot", "on : " + 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{

                    try {
                        mDatabaseReference.child("led_switch").setValue(0);
                        Log.d("Iot", "on : " + 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        mAuto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked == true){

                    try {
                        mDatabaseReference.child("auto_switch").setValue(1);
                        Log.d("Iot", "auto : " + 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }else{

                    try {
                        mDatabaseReference.child("auto_switch").setValue(0);
                        Log.d("Iot", "auto : " + 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


            }
        });

    }

    private void setHome(){

        setHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    Log.d("Iot", "onClick homelatitude: " + homeLatitude);
                    Log.d("Iot", "onClick homelongitude: " + homeLongitude);

                    appPreferences.putFloat(PREF_LATITUDE, homeLatitude);
                    appPreferences.putFloat(PREF_LONGITUDE, homeLongitude);

                }catch (Exception e){

                    Toast.makeText(MainActivity.this, "location is not set", Toast.LENGTH_SHORT).show();
                    Log.e("Iot", "onClick: ", e );
                }
            }
        });
    }

    private void startLocationService() {

        Intent serviceIntent = new Intent(this, LocationService.class);
        startService(serviceIntent);

    }

    @Override
    protected void onStart() {
        super.onStart();

        LocalBroadcastManager.getInstance(this).registerReceiver(locationReceiver, new IntentFilter(LOCATION_ACTION));


        /**
         * Runtime permissions are required on Android M and above to access User's location
         */
        if (AppUtils.hasM() && !(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {

            askPermissions();

        } else {

            startLocationService();

        }

    }

    /**
     * Ask user for permissions to access GPS location on Android M
     */
    public void askPermissions() {
        mBothPermissionRequest =
                PermissionUtil.with(this).request(android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION).onResult(
                        new Func2() {
                            @Override
                            protected void call(int requestCode, String[] permissions, int[] grantResults) {

                                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                                    startLocationService();

                                } else {

                                    Toast.makeText(MainActivity.this, R.string.permission_denied, Toast.LENGTH_LONG).show();
                                }
                            }

                        }).ask(PERMISSION_ACCESS_LOCATION_CODE);

    }

    @Override
    protected void onStop() {
        super.onStop();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationReceiver);
    }

    private class LocationReceiver extends BroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {


            if (null != intent && intent.getAction().equals(LOCATION_ACTION)) {

                String locationData = intent.getStringExtra(LOCATION_MESSAGE);
                homeDistance = intent.getFloatExtra(LOCATION_homeDistance, 9999);
                homeLatitude = intent.getBundleExtra(LOCATION_homeLocation).getFloat("latitude", 0);
                homeLongitude = intent.getBundleExtra(LOCATION_homeLocation).getFloat("longitude", 0);

                Log.d("Iot", "homeDistance onReceive" + homeDistance);

            }
        }
    }

}

