package com.tan.iotwfirebase;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tan.iotwfirebase.Storage.IPreferenceConstants;
import com.tan.iotwfirebase.Storage.TinyDB;

import java.util.ArrayList;
import java.util.Objects;

public class RemoveSensor extends AppCompatActivity implements IPreferenceConstants {


    private TinyDB prefs;
    private ArrayList<String> groupList  = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_sensor);

        prefs = new TinyDB(this);

        //get existing sensor list
        try {
            groupList = prefs.getListString(PREF_GROUPLIST);

        }catch(Exception e){

            Log.e("Iot", "initList: ",e );
        }

        //back arrow at toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_remove_sensor);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final EditText editTextField = (EditText) findViewById(R.id.sensorTBremove_text);

        editTextField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String sensorName = editTextField.getText().toString();

                    Log.d("Iot", "onEditorAction: " + sensorName);

                    for(int x = 0; x<groupList.size(); x++){

                        if(sensorName.equals(groupList.get(x))){

                            groupList.remove(x);
                            prefs.putListString(PREF_GROUPLIST, groupList);

                            Toast.makeText(RemoveSensor.this, sensorName + " Removed", Toast.LENGTH_SHORT).show();

                        }
                    }

                    return true;
                }

                return false;
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
