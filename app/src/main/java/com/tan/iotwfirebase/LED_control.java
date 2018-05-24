package com.tan.iotwfirebase;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tan.iotwfirebase.Storage.AppPreferences;
import com.tan.iotwfirebase.Storage.IPreferenceConstants;

import java.util.ArrayList;
import java.util.Objects;

public class LED_control extends AppCompatActivity implements IPreferenceConstants {

    Switch mOn;
    Switch mAuto;
    Switch mAutoGps;

    private Long mLedState;
    private Long mAutoState;
    private DatabaseReference mDatabaseReference;
    private Long autoOnGps;
    private String groupNum;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        mOn = (Switch) findViewById(R.id.on_switch);
        mAuto = (Switch) findViewById(R.id.auto_switch);
        mAutoGps = (Switch) findViewById(R.id.AutoGpsButton);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_activity_control);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        AppPreferences appPreferences = new AppPreferences(this);

        ArrayList<String> sensorchild = getIntent().getStringArrayListExtra(CHILD_KEY);
        groupNum = sensorchild.get(0);

        //for autogps
        appPreferences.putString(CHILD_KEY, groupNum);

        enableLed();
        enableAutoOnGps();
        checkAutoState();
        checkLedState();

    }

    private void checkLedState(){

        mDatabaseReference.child("led_switch").child(groupNum).addValueEventListener(new ValueEventListener() {
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

        mDatabaseReference.child("auto_switch").child(groupNum).addValueEventListener(new ValueEventListener() {
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

                if(isChecked){

                    try {
                        mDatabaseReference.child("led_switch").child(groupNum).setValue(1);
                        Log.d("Iot", "on : " + 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{

                    try {
                        mDatabaseReference.child("led_switch").child(groupNum).setValue(0);
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

                if(isChecked){

                    try {
                        mDatabaseReference.child("auto_switch").child(groupNum).setValue(1);
                        Log.d("Iot", "auto : " + 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }else{

                    try {
                        mDatabaseReference.child("auto_switch").child(groupNum).setValue(0);
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
                        mDatabaseReference.child("autoOnGps").child(groupNum).setValue(1);
                        Log.d("Iot", "autoOnGps on : " + 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{

                    try {
                        mDatabaseReference.child("autoOnGps").child(groupNum).setValue(0);
                        Log.d("Iot", "on : " + 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        mDatabaseReference.child("autoOnGps").child(groupNum).addValueEventListener(new ValueEventListener() {
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
