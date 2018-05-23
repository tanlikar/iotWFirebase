package com.tan.iotwfirebase;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import com.tan.iotwfirebase.Storage.AppPreferences;
import com.tan.iotwfirebase.Storage.IPreferenceConstants;

import java.util.ArrayList;
import java.util.Objects;

public class HumiGraph extends AppCompatActivity  implements IPreferenceConstants {

    TextView disHumi;

    private DatabaseReference mDatabaseReference;

    private HumiData humiData = new HumiData();
    private ArrayList<HumiData> mHumiData = new ArrayList<>();

    private Long refTimestamp;
    private String sensorNum;

    LineChart humiGraph;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_humi_graph);

        disHumi = (TextView) findViewById(R.id.humi_display);
        humiGraph = (LineChart) findViewById(R.id.humiGraph);

        //back arrow at toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_activity_main);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        AppPreferences appPreferences = new AppPreferences(this);

        sensorNum = getIntent().getStringExtra(CHILD_KEY);

        appPreferences.putString(CHILD_KEY, sensorNum);

        updateHumi();
    }

    private void updateHumi(){

        Query HumiQuery = mDatabaseReference.child(sensorNum).child("humi").orderByChild("timestamp").limitToLast(1000);
        HumiQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                if (dataSnapshot.exists()) {

                    mHumiData.add(dataSnapshot.getValue(HumiData.class));
                    disHumi.setText(String.format("%s %%", mHumiData.get(mHumiData.size() - 1).getHumi().toString()));
                    Log.d("Iot", "onChildAdded:" + mHumiData.size());

                    //graphing
                    ArrayList<Entry> mEntries = new ArrayList<>();

                    refTimestamp = mHumiData.get(0).getHumiTimestamp()/1000;
                    IAxisValueFormatter xAxisFormater = new HourAxisValueFormatter(refTimestamp);
                    XAxis xAxis = humiGraph.getXAxis();
                    xAxis.setValueFormatter(xAxisFormater);

                    for(int x = 0; x<mHumiData.size(); x++){

                        mEntries.add(new Entry(((mHumiData.get(x).getHumiTimestamp()/1000)-refTimestamp), mHumiData.get(x).getHumi()));
                    }

                    LineDataSet dataSet = new LineDataSet(mEntries, "Humidity");
                    LineData lineData = new LineData(dataSet);
                    lineData.setDrawValues(false);
                    MyMarkerView myMarkerView= new MyMarkerView(getApplicationContext(), R.layout.my_marker_view_layout, refTimestamp);
                    humiGraph.setMarker(myMarkerView);
                    humiGraph.setData(lineData);
                    humiGraph.invalidate();

                    //debug
                    Log.d("check", "onChildAdded: true");
                    Log.d("Iot", mHumiData.get(0).getHumi().toString());
                    Log.d("Iot", "size : " + String.valueOf(mHumiData.size()));
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


}
