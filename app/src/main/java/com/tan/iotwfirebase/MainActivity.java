package com.tan.iotwfirebase;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.SimpleTimeZone;


public class MainActivity extends AppCompatActivity {


    //variable name
   // Button mButton;
    TextView disTemp;
    TextView disHumi;
    TextView ledState;
    Switch mOn;
    Switch mAuto;
    LineChart tempGraph;

    private DatabaseReference mDatabaseReference;
    private Query tempQuery;
    private TempData tempData = new TempData();
    private ArrayList<TempData> mData = new ArrayList<>();
    private Long mHumi;
    private Long mLedState;
    private boolean loading = true;
    private Long refTimestamp;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        disTemp = (TextView) findViewById(R.id.temp_display);
        disHumi = (TextView) findViewById(R.id.humi_display);
        ledState = (TextView) findViewById(R.id.led_state);
        //mButton = (Button) findViewById(R.id.button4);
        mOn = (Switch) findViewById(R.id.on_switch);
        mAuto = (Switch) findViewById(R.id.auto_switch);
        tempGraph = (LineChart) findViewById(R.id.tempGraph);

        //
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

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
                    tempGraph.setData(lineData);
                    tempGraph.invalidate();

                    //debug
                    Log.d("Iot", mData.get(0).getTemp().toString());
                    Log.d("Iot", "size : " + String.valueOf(mData.size()));


                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                if(dataSnapshot.exists()) {
                    mData.add(dataSnapshot.getValue(TempData.class));
                    disTemp.setText(mData.get(mData.size()-1).getTemp().toString() + "°C");

                    Log.d("Iot", mData.get(0).getTemp().toString());
                    Log.d("Iot", "size : " + String.valueOf(mData.size()));


                }
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

        //for checking arraylist , to be remove
       /* mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for(Integer x = 0; x<mData.size() ; x=x+1){
                    Log.d("Iot_timestamp", mData.get(x).getTimestamp().toString());
                }
                Log.d("Iot", "length :" + data().length);
                Date date = new Date(mData.get(0).getTimestamp());
                Log.d("Iot", "date :" + date);
            }
        });*/

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

}

