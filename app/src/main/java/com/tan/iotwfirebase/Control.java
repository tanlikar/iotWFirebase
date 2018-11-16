package com.tan.iotwfirebase;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
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
import java.util.Objects;

public class Control extends AppCompatActivity implements IPreferenceConstants {

    Switch mOn;
    Switch mAutoTemp;
    Switch mAutoGps;
    Switch mAutoPmv;
    Button mUpButton, mDownButton;
    TextView mTextView;

    private Long mLedState;
    private Long mAutoState, mAutoPmvState;
    private DatabaseReference mDatabaseReference;
    private TinyDB mTinyDB;
    private Long autoOnGps;
    private String groupNum;
    private  ArrayList<String> childlist = new ArrayList<>();
    private Long initTemp;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_new);

        mOn = (Switch) findViewById(R.id.on_switch);
        mAutoTemp = (Switch) findViewById(R.id.auto_switch);
        mAutoGps = (Switch) findViewById(R.id.AutoGpsButton);
        mUpButton = (Button) findViewById(R.id.up_button);
        mDownButton = (Button) findViewById(R.id.down_button);
        mTextView = (TextView) findViewById(R.id.control_dis);
        mAutoPmv = (Switch) findViewById(R.id.autoPMV);

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
        //appPreferences.putString(CHILD_KEY, groupNum);

        setInitTemp();
        enableOn();
        enableAutoOnGps();
        checkAutoState();
        checkOnState();
        control();

    }

    private void checkOnState(){

        mDatabaseReference.child(childlist.get(0)).child(childlist.get(1)).child("on").addValueEventListener(new ValueEventListener() {
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

        mDatabaseReference.child(childlist.get(0)).child(childlist.get(1)).child("auto_switch").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()) {
                    mAutoState = (Long) dataSnapshot.getValue();

                    if (mAutoState == 1){

                        mAutoTemp.setChecked(true);
                        Log.d("Iot", "Auto on : on");
                    }else{

                        mAutoTemp.setChecked(false);
                        Log.d("Iot", "Auto on : off");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseReference.child(childlist.get(0)).child(childlist.get(1)).child("auto_pmv").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    mAutoPmvState = (Long) dataSnapshot.getValue();

                    if (mAutoPmvState == 1) {

                        mAutoPmv.setChecked(true);
                        Log.d("Iot", "Auto on pmv : on");
                    } else {

                        mAutoPmv.setChecked(false);
                        Log.d("Iot", "Auto on pmv: off");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void enableOn(){

        mOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){

                    try {
                        mDatabaseReference.child(childlist.get(0)).child(childlist.get(1)).child("on").setValue(1);
                        mDatabaseReference.child(childlist.get(0)).child(childlist.get(1)).child("Temperature").setValue(initTemp);
                        Log.d("Iot", "on : " + 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{

                    try {
                        mDatabaseReference.child(childlist.get(0)).child(childlist.get(1)).child("on").setValue(0);
                        Log.d("Iot", "on : " + 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        mAutoTemp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){

                    try {
                        mDatabaseReference.child(childlist.get(0)).child(childlist.get(1)).child("auto_switch").setValue(1);
                        Log.d("Iot", "auto : " + 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }else{

                    try {
                        mDatabaseReference.child(childlist.get(0)).child(childlist.get(1)).child("auto_switch").setValue(0);
                        Log.d("Iot", "auto : " + 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


            }
        });

        mAutoPmv.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
        });

    }

    private void enableAutoOnGps(){

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
    }

    private void control(){
        //TODO

        mUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initTemp = initTemp + 1;
                mDatabaseReference.child(childlist.get(0)).child(childlist.get(1)).child("Temperature").setValue(initTemp);
            }
        });

        mDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initTemp = initTemp - 1;
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
