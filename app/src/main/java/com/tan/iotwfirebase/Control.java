package com.tan.iotwfirebase;

import android.annotation.SuppressLint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tan.iotwfirebase.Storage.IPreferenceConstants;
import com.tan.iotwfirebase.Storage.TinyDB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class Control extends AppCompatActivity implements IPreferenceConstants {

    //TODO remove ambient

    Switch mAutoGps;
  //  Switch mAutoPmv;
    Switch mSleep;
    Switch mSwing;
    //Switch mAmbient;
    Button mUpButton, mDownButton, mPowerButton;
    TextView mTextView;
    Spinner spinner_fan;

    private final Long minSetting = 16L;
    private final Long maxSetting = 30L;
    //private Long  mAutoPmvState;
    private int mFanState;
    private DatabaseReference mDatabaseReference;
    private TinyDB mTinyDB;
    private Long autoOnGps;
    private String groupNum;
    private  ArrayList<String> childlist = new ArrayList<>();
    private Long initTemp;
    private String[] fanArray;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_new);

        mPowerButton = (Button) findViewById(R.id.power_button);
        //mAutoGps = (Switch) findViewById(R.id.AutoGpsButton);
        mUpButton = (Button) findViewById(R.id.up_button);
        mDownButton = (Button) findViewById(R.id.down_button);
        mTextView = (TextView) findViewById(R.id.control_dis);
        //mAutoPmv = (Switch) findViewById(R.id.autoPMV);
        mSwing = (Switch) findViewById(R.id.switch_swing);
        mSleep = (Switch) findViewById(R.id.switch_sleep);
        //mAmbient = (Switch) findViewById(R.id.switch_ambient);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_activity_control);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

       // AppPreferences appPreferences = new AppPreferences(this);
        mTinyDB = new TinyDB(this);

        childlist = getIntent().getStringArrayListExtra(CHILD_KEY);
        groupNum = childlist.get(0);

        //for autogps
        mTinyDB.putListString(GPS_CHILD_KEY, childlist);

        fanArray = getResources().getStringArray(R.array.fan_speed);
        spinner_fan = (Spinner) findViewById(R.id.spinner_fan);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.fan_speed, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner_fan.setAdapter(adapter);

        setSleep();
        setInitQuiet();
        iniFanSpeed();
        setInitTemp();
        enableOn();
        //enableAutoOnGps();
        //checkAutoState();
        control();

        spinner_fan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Log.d("fan", "fanArray" + Arrays.toString(fanArray));
                Log.d("fan", "onItemSelected: " + parent.getItemAtPosition(position).toString());
                mDatabaseReference.child(childlist.get(0)).child(childlist.get(1)).child("fan").setValue(position);
                mTinyDB.putInt(FAN_SPEED, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

//        private void checkAutoState(){
//
//        mDatabaseReference.child(childlist.get(0)).child(childlist.get(1)).child("auto_pmv").addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                    if (dataSnapshot.exists()) {
//                        mAutoPmvState = (Long) dataSnapshot.getValue();
//
//                        if (mAutoPmvState == 1) {
//
//                            mAutoPmv.setChecked(true);
//                            Log.d("Iot", "Auto on pmv : on");
//                        } else {
//
//                            mAutoPmv.setChecked(false);
//                            Log.d("Iot", "Auto on pmv: off");
//                        }
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                }
//            });

//    }

    private void iniFanSpeed(){

        mDatabaseReference.child(childlist.get(0)).child(childlist.get(1)).child("fan").addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    if (dataSnapshot.exists()) {
                        mFanState = Math.toIntExact((Long) dataSnapshot.getValue());
                        mTinyDB.putInt(FAN_SPEED, mFanState);
                        spinner_fan.setSelection(mFanState);
                        Log.d("initFan", String.valueOf(mFanState));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        int fan_speed = mTinyDB.getInt(FAN_SPEED);
        spinner_fan.setSelection(fan_speed);
        Log.d("initFan", String.valueOf(fan_speed));

    }

    private void enableOn(){

        mPowerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mDatabaseReference.child(childlist.get(0)).child(childlist.get(1)).child("on").setValue(1);
                    mDatabaseReference.child(childlist.get(0)).child(childlist.get(1)).child("Temperature").setValue(initTemp);
                    Log.d("Iot", "on : " + 1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

       /*mAutoPmv.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){

                    try {
                        mDatabaseReference.child(childlist.get(0)).child(childlist.get(1)).child("auto_pmv").setValue(1);
                        Log.d("Iot", "auto pmv : " + 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }else{

                    try {
                        mDatabaseReference.child(childlist.get(0)).child(childlist.get(1)).child("auto_pmv").setValue(0);
                        Log.d("Iot", "auto pmv : " + 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });*/

    }

/*    private void enableAutoOnGps(){

        mAutoGps.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){

                    try {
                        mDatabaseReference.child(childlist.get(0)).child(childlist.get(1)).child("autoOnGps").setValue(1);
                        Log.d("Iot", "autoOnGps on : " + 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{

                    try {
                        mDatabaseReference.child(childlist.get(0)).child(childlist.get(1)).child("autoOnGps").setValue(0);
                        Log.d("Iot", "on : " + 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        mDatabaseReference.child(childlist.get(0)).child(childlist.get(1)).child("autoOnGps").addValueEventListener(new ValueEventListener() {
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
    }*/

    private void control(){
        //TODO

        mUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initTemp = initTemp + 1;
                if(initTemp >= 30) {
                    initTemp = maxSetting;
                }
                mDatabaseReference.child(childlist.get(0)).child(childlist.get(1)).child("Temperature").setValue(initTemp);
            }
        });

        mDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initTemp = initTemp - 1;
                if(initTemp <= 16){
                    initTemp = minSetting;
                }
                mDatabaseReference.child(childlist.get(0)).child(childlist.get(1)).child("Temperature").setValue(initTemp);
            }
        });
    }

    private void setInitTemp(){

        mDatabaseReference.child(childlist.get(0)).child(childlist.get(1)).child("Temperature").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()) {
                    initTemp =  (Long) dataSnapshot.getValue();
                    mTextView.setText(initTemp.toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setInitQuiet(){

        mDatabaseReference.child(childlist.get(0)).child(childlist.get(1)).child("Sleep").addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()) {
                    Long initSleep =  (Long) dataSnapshot.getValue();
                    if(initSleep == 1) {
                        mSleep.setChecked(true);
                    }else if(initSleep == 0){
                        mSleep.setChecked(false);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseReference.child(childlist.get(0)).child(childlist.get(1)).child("Swing").addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()) {
                    Long initSwing =  (Long) dataSnapshot.getValue();
                    if(initSwing == 1) {
                        mSwing.setChecked(true);

                    }else if(initSwing == 0){
                        mSwing.setChecked(false);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

//        mDatabaseReference.child(childlist.get(0)).child(childlist.get(1)).child("Ambient").addValueEventListener(new ValueEventListener() {
//            @SuppressLint("SetTextI18n")
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                if(dataSnapshot.exists()) {
//                    Long initAmbient =  (Long) dataSnapshot.getValue();
//                    if(initAmbient == 1) {
//                        mAmbient.setChecked(true);
//
//                    }else if(initAmbient == 0){
//                        mAmbient.setChecked(false);
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
    }

    private void setSleep(){

        mSleep.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){

                    try {
                        mDatabaseReference.child(childlist.get(0)).child(childlist.get(1)).child("Sleep").setValue(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }else{

                    try {
                        mDatabaseReference.child(childlist.get(0)).child(childlist.get(1)).child("Sleep").setValue(0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mSwing.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){

                    try {
                        mDatabaseReference.child(childlist.get(0)).child(childlist.get(1)).child("Swing").setValue(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }else{

                    try {
                        mDatabaseReference.child(childlist.get(0)).child(childlist.get(1)).child("Swing").setValue(0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

//        mAmbient.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if(isChecked){
//
//                    try {
//                        mDatabaseReference.child(childlist.get(0)).child(childlist.get(1)).child("Ambient").setValue(1);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                }else{
//
//                    try {
//                        mDatabaseReference.child(childlist.get(0)).child(childlist.get(1)).child("Ambient").setValue(0);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
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
