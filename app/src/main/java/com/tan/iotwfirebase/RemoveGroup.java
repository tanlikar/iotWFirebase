package com.tan.iotwfirebase;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tan.iotwfirebase.Storage.IPreferenceConstants;
import com.tan.iotwfirebase.Storage.TinyDB;

import java.util.ArrayList;
import java.util.Objects;

public class RemoveGroup extends AppCompatActivity implements IPreferenceConstants {


    private TinyDB prefs;
    private ArrayList<String> groupList  = new ArrayList<>();
    ArrayAdapter<String> adp;
    private String groupNum;
    private int temp;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_group);

        prefs = new TinyDB(this);

        initList();

        Button button = (Button)findViewById(R.id.remove_group_button);

        //back arrow at toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_remove_group);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //group adapter
        Spinner spinner_group_remove = (Spinner) findViewById(R.id.spinner_activity_remove_group);
        adp = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, groupList);
        spinner_group_remove.setAdapter(adp);

        spinner_group_remove.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                groupNum = parent.getItemAtPosition(position).toString();
                temp = position;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    Toast.makeText(RemoveGroup.this, groupNum + " is Removed", Toast.LENGTH_SHORT).show();
                    groupList.remove(temp);
                    prefs.putListString(PREF_GROUPLIST, groupList);

                    adp.clear();
                    initList();
                    adp.addAll(groupList);
                    Log.d("grouplistRemove", "onClick: " + groupList);
                    adp.notifyDataSetChanged();

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
            Log.d("removeSensor", String.valueOf(groupList));

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
