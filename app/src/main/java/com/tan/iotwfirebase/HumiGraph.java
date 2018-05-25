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

//tempGraph
public class HumiGraph extends AppCompatActivity implements  IPreferenceConstants {

    //variable name
    TextView disHumi;
    LineChart humiGraph;

    private DatabaseReference mDatabaseReference;
    private Query humiQuery;
    private TempData tempData = new TempData();
    private ArrayList<HumiData> mData = new ArrayList<>();
    private Long refTimestamp;
    private ArrayList<String> sensorlist = new ArrayList<>();


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("SetTextI18n")
    @Override
    protected void  onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_humi_graph);


        disHumi = (TextView) findViewById(R.id.humi_display);
        humiGraph = (LineChart) findViewById(R.id.humiGraph);

        //back arrow at toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_activity_humi);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        //AppPreferences appPreferences = new AppPreferences(this);


        sensorlist = getIntent().getStringArrayListExtra(CHILD_KEY);

        //appPreferences.putString(CHILD_KEY, sensorNum);

        updateTemp();

    }

    private void updateTemp(){

        humiQuery = mDatabaseReference.child(sensorlist.get(0)).child(sensorlist.get(1)).orderByChild("timestamp").limitToLast(1000);
        humiQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                if(dataSnapshot.exists()) {
                    mData.add(dataSnapshot.getValue(HumiData.class));
                    disHumi.setText(String.format("%s°C", mData.get(mData.size() - 1).getHumi().toString()));

                    //graphing
                    ArrayList<Entry> mEntries = new ArrayList<>();

                    refTimestamp = mData.get(0).getTimestamp()/1000;
                    IAxisValueFormatter xAxisFormater = new HourAxisValueFormatter(refTimestamp);
                    XAxis xAxis = humiGraph.getXAxis();
                    xAxis.setValueFormatter(xAxisFormater);

                    for(int x = 0; x<mData.size(); x++){

                        mEntries.add(new Entry(((mData.get(x).getTimestamp()/1000)-refTimestamp), mData.get(x).getHumi()));
                    }

                    LineDataSet dataSet = new LineDataSet(mEntries, "Temperature");
                    LineData lineData = new LineData(dataSet);
                    lineData.setDrawValues(false);
                    MyMarkerView myMarkerView= new MyMarkerView(getApplicationContext(), R.layout.my_marker_view_layout, refTimestamp);
                    humiGraph.setMarker(myMarkerView);
                    humiGraph.setData(lineData);
                    humiGraph.invalidate();

                    //debug
                    Log.d("check", "onChildAdded: true");
                    Log.d("Iot", mData.get(0).getHumi().toString());
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

