package com.tan.iotwfirebase;

import android.content.Intent;
import android.graphics.Rect;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.tan.iotwfirebase.Storage.IPreferenceConstants;
import com.tan.iotwfirebase.Storage.TinyDB;

import java.util.ArrayList;
import java.util.List;

public class welcom_page extends AppCompatActivity implements AbsListView.OnScrollListener, IPreferenceConstants {

    //private static final int MAX_ROWS = 50;
    private int lastTopValue = 0;

    private ArrayList<String> sensorList  = new ArrayList<>();
    private ListView listView;
    private ImageView backgroundImage;
    private ArrayAdapter adapter;
    private  TinyDB prefs;
    private FloatingActionButton addSensorButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcom_page);

        listView = (ListView) findViewById(R.id.list);
        addSensorButton = (FloatingActionButton) findViewById(R.id.addSensor);
        prefs = new TinyDB(this);

        initList();
        addSensor();

        adapter = new ArrayAdapter(this, R.layout.list_row, sensorList);
        listView.setAdapter(adapter);

        // inflate custom header and attach it to the list
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.custom_header, listView, false);
        listView.addHeaderView(header, null, false);

        // we take the background image and button reference from the header
        backgroundImage = (ImageView) header.findViewById(R.id.listHeaderImage);
        listView.setOnScrollListener(this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent;
                intent = new Intent(welcom_page.this, MainActivity.class);
                intent.putExtra(CHILD_KEY, ("sensor" + position));
                startActivity(intent);
            }
        });
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        Rect rect = new Rect();
        backgroundImage.getLocalVisibleRect(rect);
        if (lastTopValue != rect.top) {
            lastTopValue = rect.top;
            backgroundImage.setY((float) (rect.top / 2.0));
        }

    }

    private void initList(){

        //ideally list init with null, need user to add sensor beforehand to use app
        //need to remove sensor 1

        //need to add get arraylist from sharedpref
        try {
            sensorList = prefs.getListString(PREF_SENSORLIST);

        }catch(Exception e){

            Log.e("Iot", "initList: ",e );
        }

        if(sensorList.isEmpty()) {
            sensorList.add("Sensor 1");
        }
    }

    private void addSensor(){

        addSensorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sensorList.add("Sensor" + (sensorList.size() + 1) );
                Log.d("Iot", "onClickFloating: " + sensorList.toString());
                prefs.putListString(PREF_SENSORLIST, sensorList);
                Toast.makeText(welcom_page.this, "Sensor Added", Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
            }
        });
    }
}
