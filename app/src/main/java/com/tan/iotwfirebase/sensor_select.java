package com.tan.iotwfirebase;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.tan.iotwfirebase.Storage.IPreferenceConstants;
import com.tan.iotwfirebase.Storage.TinyDB;

import java.util.ArrayList;
import java.util.Objects;

public class sensor_select extends AppCompatActivity implements IPreferenceConstants {

    private String groupNum;
    ArrayList<String> sensorList = new ArrayList<>();

    private ListView listView;
    private TinyDB prefs;
    private ArrayAdapter adapter;
    final private String TEMPCILD = "Temperature";
    final private  String HUMICHILD = "Humidity";
    final private String CONTROLCHILD ="Control";

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_select);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_activity_sensor_select);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        groupNum = getIntent().getStringExtra(CHILD_KEY);

        prefs = new TinyDB(this);

        try{

            sensorList = prefs.getListString(groupNum);
            Log.d("sensorlist", "onCreate: " + sensorList);

        }catch(Exception e){

            Log.e("get", "sensorlist get error", e );
        }

        listView = (ListView) findViewById(R.id.list_sensor);
        adapter = new ArrayAdapter(this, R.layout.list_row, sensorList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent;
                ArrayList<String> childList = new ArrayList<>();
                childList.add(groupNum);

               String[] split = parent.getItemAtPosition(position).toString().split(" ");

               if(split[0].equals(TEMPCILD)){

                   intent = new Intent(sensor_select.this, MainActivity.class);
                   childList.add(parent.getItemAtPosition(position).toString());
                   intent.putStringArrayListExtra(CHILD_KEY, childList);
                   startActivity(intent);

               }else  if (split[0].equals(HUMICHILD)){

                   intent = new Intent(sensor_select.this, HumiGraph.class);
                   childList.add(parent.getItemAtPosition(position).toString());
                   intent.putStringArrayListExtra(CHILD_KEY, childList);
                   startActivity(intent);

               } else if (split[0].equals(CONTROLCHILD)){

                   intent = new Intent(sensor_select.this, LED_control.class);
                   childList.add(parent.getItemAtPosition(position).toString());
                   intent.putStringArrayListExtra(CHILD_KEY, childList);
                   startActivity(intent);
               }

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
