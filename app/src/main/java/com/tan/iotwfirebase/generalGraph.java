package com.tan.iotwfirebase;

import android.annotation.SuppressLint;
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
import com.tan.iotwfirebase.Storage.IPreferenceConstants;
import com.tan.iotwfirebase.helper.ChildConstants;

import java.util.ArrayList;
import java.util.Objects;

public class generalGraph extends AppCompatActivity implements IPreferenceConstants, ChildConstants {

    //variable name
    TextView disGeneral;
    LineChart generalGraph;
    TextView text;

    private DatabaseReference mDatabaseReference;
    private Query generalQuery;
    private ArrayList<generalData> mData = new ArrayList<>();
    private generalData data = new generalData();
    private Long refTimestamp;
    private ArrayList<String> sensorlist = new ArrayList<>();


    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_graph);


        disGeneral = (TextView) findViewById(R.id.general_display);
        generalGraph = (LineChart) findViewById(R.id.generalGraph);
        text = (TextView) findViewById(R.id.general_text);

        //back arrow at toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_activity_humi);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        sensorlist = getIntent().getStringArrayListExtra(CHILD_KEY);
        text.setText(sensorlist.get(2) + " :");

        update();
    }


    private void update(){

            generalQuery = mDatabaseReference.child(sensorlist.get(0)).child(sensorlist.get(1)).orderByChild("timestamp").limitToLast(1000);
            generalQuery.addChildEventListener(new ChildEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                if(dataSnapshot.exists()) {

                    mData.add(dataSnapshot.getValue(generalData.class));

                        switch (sensorlist.get(2)) {
                            case TEMPCILD:
                                disGeneral.setText(String.format("%s°C", mData.get(mData.size() - 1).getData().toString()));
                                break;

                            case HUMICHILD:
                                disGeneral.setText((mData.get(mData.size() - 1).getData().toString()) + " %");
                                break;

                            case DUSTCHILD:
                                disGeneral.setText((mData.get(mData.size() - 1).getData().toString()) + " ppm");
                                break;

                            case CO2CHILD:
                                disGeneral.setText((mData.get(mData.size() - 1).getData().toString()) + " ppm");
                                break;

                            case COCHILD:
                                disGeneral.setText((mData.get(mData.size() - 1).getData().toString()) + " ppm");
                                break;

                            case SMOKECHILD:
                                disGeneral.setText((mData.get(mData.size() - 1).getData().toString()) + " ppm");
                                break;

                            case LPGCHILD:
                                disGeneral.setText((mData.get(mData.size() - 1).getData().toString()) + " ppm");
                                break;

                            case METHANECHILD:
                                disGeneral.setText((mData.get(mData.size() - 1).getData().toString()) + " ppm");
                                break;


                        }


                    //graphing
                    ArrayList<Entry> mEntries = new ArrayList<>();
                    refTimestamp = mData.get(0).getTimestamp()/1000;
                    IAxisValueFormatter xAxisFormater = new HourAxisValueFormatter(refTimestamp);
                    XAxis xAxis = generalGraph.getXAxis();
                    xAxis.setValueFormatter(xAxisFormater);

                    for(int x = 0; x<mData.size(); x++){
                        mEntries.add(new Entry(((mData.get(x).getTimestamp()/1000)-refTimestamp), mData.get(x).getData()));

                    }

                    LineDataSet dataSet = new LineDataSet(mEntries, sensorlist.get(2));
                    LineData lineData = new LineData(dataSet);
                    lineData.setDrawValues(false);
                    MyMarkerView myMarkerView= new MyMarkerView(getApplicationContext(), R.layout.my_marker_view_layout, refTimestamp, sensorlist.get(2));
                    generalGraph.setMarker(myMarkerView);
                    generalGraph.setData(lineData);
                    generalGraph.invalidate();

                    //debug
                    Log.d("check", "onChildAdded: true");
                    Log.d("Iot", mData.get(0).getData().toString());
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
