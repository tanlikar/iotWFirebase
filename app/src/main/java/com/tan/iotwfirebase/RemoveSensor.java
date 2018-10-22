package com.tan.iotwfirebase;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.tan.iotwfirebase.Storage.IPreferenceConstants;
import com.tan.iotwfirebase.Storage.TinyDB;

import java.util.ArrayList;
import java.util.Objects;

public class RemoveSensor extends AppCompatActivity  implements IPreferenceConstants{

    private ArrayList<String> groupList  = new ArrayList<>();
    private TinyDB prefs;
    private ArrayList<ArrayList<String >> sensorList = new ArrayList<>();
    private String sensorType;
    private String groupNum;
    ArrayAdapter<String> adp;
    ArrayAdapter<String> adapter;
    private int sensorPosition;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_sensor);

        Button button = (Button) findViewById(R.id.remove_sensor_button);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_activity_remove_sensor);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        prefs = new TinyDB(this);
        initList();

        //group adapter
        Spinner spinner_group_remove = (Spinner) findViewById(R.id.spinner_activity_remove_group);
        adp = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, groupList);
        spinner_group_remove.setAdapter(adp);

        //type adapter
        Spinner spinner_type = (Spinner) findViewById(R.id.spinner_type_remove);
        ArrayList<String> temp1= new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, temp1);
        spinner_type.setAdapter(adapter);

        spinner_group_remove.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                groupNum = parent.getItemAtPosition(position).toString();
                ArrayList<String> temp = prefs.getListString(groupNum);
                adapter.clear();
                adapter.addAll(temp);
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        spinner_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                sensorType = parent.getItemAtPosition(position).toString();
                sensorPosition = position;
                Log.d("spinner_remove_sensor", "onItemClick: " + parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    ArrayList<String> temp = prefs.getListString(groupNum);
                    Toast.makeText(RemoveSensor.this, temp.get(sensorPosition) + " is Removed from " + groupNum, Toast.LENGTH_SHORT).show();
                    temp.remove(sensorPosition);
                    prefs.putListString(groupNum, temp);
                    adapter.clear();
                    adapter.addAll(temp);
                    adapter.notifyDataSetChanged();
                }catch (Exception ignored){

                }

            }

        });


    }

    private void initList(){

        //ideally list init with null, need user to add sensor beforehand to use app
        //need to remove sensor 1

        //need to add get arraylist from sharedpref
        try {
            groupList = prefs.getListString(PREF_GROUPLIST);
            Log.d("remove sensor", String.valueOf(groupList));

        }catch(Exception e){

            Log.e("On", "initList: ",e );
        }

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


