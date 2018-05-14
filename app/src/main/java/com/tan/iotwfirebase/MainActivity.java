package com.tan.iotwfirebase;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.tan.iotwfirebase.Storage.AppPreferences;
import com.tan.iotwfirebase.Storage.IPreferenceConstants;

import java.util.ArrayList;
import java.util.Objects;


public class MainActivity extends AppCompatActivity implements  IPreferenceConstants {

    //variable name
    TextView disTemp;
    TextView disHumi;
    Switch mOn;
    Switch mAuto;
    Switch mAutoGps;
    LineChart tempGraph;

    private DatabaseReference mDatabaseReference;
    private Query tempQuery;
    private TempData tempData = new TempData();
    private HumiData humiData = new HumiData();
    private ArrayList<TempData> mData = new ArrayList<>();
    private ArrayList<HumiData> mHumiData = new ArrayList<>();
    private Long mHumi;
    private Long mLedState;
    private Long mAutoState;
    private Long refTimestamp;
    private Long autoOnGps;
    private String sensorNum;


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("SetTextI18n")
    @Override
    protected void  onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        disTemp = (TextView) findViewById(R.id.temp_display);
        disHumi = (TextView) findViewById(R.id.humi_display);
        mOn = (Switch) findViewById(R.id.on_switch);
        mAuto = (Switch) findViewById(R.id.auto_switch);
        tempGraph = (LineChart) findViewById(R.id.tempGraph);
        mAutoGps = (Switch) findViewById(R.id.AutoGpsButton);

        //back arrow at toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_activity_main);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        AppPreferences appPreferences = new AppPreferences(this);

        sensorNum = getIntent().getStringExtra(CHILD_KEY);

        appPreferences.putString(CHILD_KEY, sensorNum);

       checkAutoState();
       checkLedState();
       updateHumi();
       updateTemp();
       enableLed();
       enableAutoOnGps();

    }

    private void updateTemp(){

        tempQuery = mDatabaseReference.child(sensorNum).child("temperature").orderByChild("timestamp").limitToLast(1000);
        tempQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                if(dataSnapshot.exists()) {
                    mData.add(dataSnapshot.getValue(TempData.class));
                    disTemp.setText(String.format("%s°C", mData.get(mData.size() - 1).getTemp().toString()));

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
                    Log.d("check", "onChildAdded: true");
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

      /*  mDatabaseReference.child(sensorNum).child("humi").addValueEventListener(new ValueEventListener() {
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

        });*/

      Query HumiQuery = mDatabaseReference.child(sensorNum).child("humi").orderByChild("timestamp").limitToLast(1000);
      HumiQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                if (dataSnapshot.exists()) {

//                    mHumi = (Long) dataSnapshot.getValue();
//                    Log.d("Iot", "humi = " + mHumi.toString());
//                    disHumi.setText(String.format("%s %%", mHumi.toString()));

                    mHumiData.add(dataSnapshot.getValue(HumiData.class));
                    disHumi.setText(String.format("%s %%", mHumiData.get(mHumiData.size() - 1).getHumi().toString()));
                    Log.d("Iot", "onChildAdded:" + mHumiData.size());
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

    private void checkLedState(){

        mDatabaseReference.child("led_switch").child(sensorNum).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()) {
                    mLedState = (Long) dataSnapshot.getValue();

                    if (mLedState == 1){

                        mOn.setChecked(true);
                        Log.d("Iot", "led on");
                    }else{

                        mOn.setChecked(false);
                        Log.d("Iot", "led off");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void checkAutoState(){

        mDatabaseReference.child("auto_switch").child(sensorNum).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()) {
                    mAutoState = (Long) dataSnapshot.getValue();

                    if (mAutoState == 1){

                        mAuto.setChecked(true);
                        Log.d("Iot", "Auto on : on");
                    }else{

                        mAuto.setChecked(false);
                        Log.d("Iot", "Auto on : off");
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
                        mDatabaseReference.child("led_switch").child(sensorNum).setValue(1);
                        Log.d("Iot", "on : " + 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{

                    try {
                        mDatabaseReference.child("led_switch").child(sensorNum).setValue(0);
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
                        mDatabaseReference.child("auto_switch").child(sensorNum).setValue(1);
                        Log.d("Iot", "auto : " + 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }else{

                    try {
                        mDatabaseReference.child("auto_switch").child(sensorNum).setValue(0);
                        Log.d("Iot", "auto : " + 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


            }
        });

    }

    private void enableAutoOnGps(){

        mAutoGps.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){

                    try {
                        mDatabaseReference.child("autoOnGps").child(sensorNum).setValue(1);
                        Log.d("Iot", "autoOnGps on : " + 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{

                    try {
                        mDatabaseReference.child("autoOnGps").child(sensorNum).setValue(0);
                        Log.d("Iot", "on : " + 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        mDatabaseReference.child("autoOnGps").child(sensorNum).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()) {
                    autoOnGps = (long) dataSnapshot.getValue();

                    if(autoOnGps == 1){

                        mAutoGps.setChecked(true);
                        Log.d("Iot", "received AutoOnGps : on");
                    }else{

                        mAutoGps.setChecked(false);
                        Log.d("Iot", "received AutoOnGps : off");
                    }


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}

