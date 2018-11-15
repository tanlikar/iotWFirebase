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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tan.iotwfirebase.Storage.IPreferenceConstants;
import com.tan.iotwfirebase.Storage.TinyDB;

import java.util.ArrayList;
import java.util.Objects;

public class add_sensor extends AppCompatActivity implements IPreferenceConstants {

    private String sensorType, sensorGroup;
    private ArrayList<String> groupList = new ArrayList<>();
    private ArrayList<String> sensorList = new ArrayList<>();
    private TinyDB prefs;
    private int counter;

    private DatabaseReference mDatabaseReference;


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sensor);

        Button button = (Button) findViewById(R.id.add_sensor);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_activity_add_sensor);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();


        Spinner spinner_type = (Spinner) findViewById(R.id.spinner_type);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sensor_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner_type.setAdapter(adapter);

        spinner_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                sensorType = parent.getItemAtPosition(position).toString();
                Log.d("spinner1", "onItemClick: " + parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Spinner spinner_group = (Spinner) findViewById(R.id.spinner_group);
        prefs = new TinyDB(this);
        initList();
        ArrayAdapter<String> adp = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, groupList);
        spinner_group.setAdapter(adp);

        spinner_group.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                sensorGroup = parent.getItemAtPosition(position).toString();
                Log.d("spinner2", "onItemClick: " + parent.getItemAtPosition(position).toString());

                //use sensor group as key to obtain sensorlist
                try{
                    sensorList = prefs.getListString(parent.getItemAtPosition(position).toString());
                    Log.d("sensorlst", "sensor list obtain = " +sensorList.toString());

                }catch (Exception e){
                    Log.e("sensorGroup error", "onItemSelected: ",e );
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                counter = 0;

                for(int x = 0 ; x < sensorList.size() ; x++){

                    String y = sensorType + " " + (x+1);

                    if(sensorList.contains(y)){
                        counter++;
                    }
                }

                sensorList.add(sensorType + " " + (counter+1));
                Log.d("add", "sensor added" + sensorType + " " + (counter +1));

                // use sensor group as key
                prefs.putListString(sensorGroup, sensorList);
                Toast.makeText(add_sensor.this,"Sensor added", Toast.LENGTH_SHORT).show();

                if(sensorType.equals("Control")) {
                    mDatabaseReference.child(sensorGroup).child(sensorType + " " + (counter + 1)).child("Temperature").setValue(25);
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
            Log.d("On", String.valueOf(groupList));

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
