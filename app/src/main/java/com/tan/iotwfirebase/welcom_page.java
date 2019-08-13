package com.tan.iotwfirebase;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.github.kayvannj.permission_utils.Func2;
import com.github.kayvannj.permission_utils.PermissionUtil;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.tan.iotwfirebase.Storage.AppPreferences;
import com.tan.iotwfirebase.Storage.IPreferenceConstants;
import com.tan.iotwfirebase.Storage.TinyDB;
import com.tan.iotwfirebase.helper.AppUtils;
import com.tan.iotwfirebase.helper.ILocationConstants;
import com.tan.iotwfirebase.service.LocationService;

import java.util.ArrayList;
import java.util.Arrays;

public class welcom_page extends AppCompatActivity implements ILocationConstants, NavigationView.OnNavigationItemSelectedListener, AbsListView.OnScrollListener, IPreferenceConstants {

    private int lastTopValue = 0;

    private ArrayList<String> groupList  = new ArrayList<>();
    private ListView listView;
    private ImageView backgroundImage;
    private ArrayAdapter adapter;
    private  TinyDB prefs;
    private  Toolbar toolbar;
    private AppPreferences appPreferences;
    private float CurLatitude;
    private float CurLongitude;
    private float homeDistance;

    /**
     * Constant used in the location settings dialog.
     */
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    //protected static final String TAG = MainActivity.class.getSimpleName();

    /**
     * Receiver listening to Location updates and updating UI in activity
     */
    private LocationReceiver locationReceiver;

    /**
     * Permission util with callback mechanism to avoid boilerplate code
     * <p/>
     * https://github.com/kayvannj/PermissionUtil
     */
    private PermissionUtil.PermissionRequestObject mBothPermissionRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequestHighAccuracy;
    private LocationRequest mLocationRequestBalanceAccuracy;
    Task<LocationSettingsResponse> result;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_sliding_menu);

        appPreferences = new AppPreferences(this);

        //enable gps
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationReceiver = new LocationReceiver();

        //set toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        slidingMenu();

        //set listview
        listView = (ListView) findViewById(R.id.list);
        prefs = new TinyDB(this);

        initList();

        //list adapter
        adapter = new ArrayAdapter(this, R.layout.list_row, groupList);
        listView.setAdapter(adapter);

        // inflate custom header and attach it to the list
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.custom_header, listView, false);
        listView.addHeaderView(header, null, false);

        // we take the background image and button reference from the header
        backgroundImage = (ImageView) header.findViewById(R.id.listHeaderImage);
        listView.setOnScrollListener(this);

        //select groupnum
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent;
                intent = new Intent(welcom_page.this, sensor_select.class);
                intent.putExtra(CHILD_KEY, parent.getItemAtPosition(position).toString());
                Log.d("parent", "onItemClick: " + parent.getItemAtPosition(position).toString());
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
            groupList = prefs.getListString(PREF_GROUPLIST);
            Log.d("On", String.valueOf(groupList));

        }catch(Exception e){

            Log.e("On", "initList: ",e );
        }

    }

    private void slidingMenu(){

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.my_sliding_menu, menu);
        return true;
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_removeAll) {

            groupList.clear();
            prefs.putListString(PREF_GROUPLIST, groupList);
            Toast.makeText(welcom_page.this, "Groups Removed", Toast.LENGTH_SHORT).show();
            adapter.notifyDataSetChanged();

        } else if (id == R.id.nav_setHomeLocation) {

            try {

                    Log.d("Iot", "onClick homelatitude: " + CurLatitude);
                    Log.d("Iot", "onClick homelongitude: " + CurLongitude);

                    appPreferences.putFloat(PREF_LATITUDE, CurLatitude);
                    appPreferences.putFloat(PREF_LONGITUDE, CurLongitude);
                    Toast.makeText(welcom_page.this, R.string.Location_set_message, Toast.LENGTH_SHORT).show();

                }catch (Exception e) {

                Toast.makeText(welcom_page.this, R.string.Location_not_set_message, Toast.LENGTH_SHORT).show();
                Log.e("Iot", "onClick: ", e);

            }

        } else if (id == R.id.nav_addGroup) {

            int a=groupList.size()+1;
            for(int x=0; x<a; x++){

                String temp = "Group "+ (x+1);
                if(!groupList.contains(temp)){
                    groupList.add(temp);
                    Log.d("Iot", "onClickFloating: " + groupList.toString());
                    prefs.putListString(PREF_GROUPLIST, groupList);
                    Toast.makeText(welcom_page.this, "Group Added", Toast.LENGTH_SHORT).show();
                    adapter.notifyDataSetChanged();
                }
            }

        } else if (id == R.id.nav_removeGroup) {

            Intent intent;
            intent = new Intent(welcom_page.this, RemoveGroup.class);
            startActivity(intent);

        } else if (id == R.id.nav_help) {

        }else if (id == R.id.nav_addSensor){

            Intent intent;
            intent = new Intent(welcom_page.this, add_sensor.class);
            startActivity(intent);

        }else if (id == R.id.nav_removeSensor){
            Intent intent;
            intent = new Intent(welcom_page.this, RemoveSensor.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    private void startLocationService() {

        Intent serviceIntent = new Intent(this, LocationService.class);
        startService(serviceIntent);

    }

    @Override
    protected void onStart() {
        super.onStart();

        LocalBroadcastManager.getInstance(this).registerReceiver(locationReceiver, new IntentFilter(LOCATION_ACTION));
        mLocationRequestHighAccuracy = new LocationRequest();
        mLocationRequestBalanceAccuracy = new LocationRequest();

        mLocationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequestBalanceAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequestHighAccuracy);
        builder.addLocationRequest(mLocationRequestBalanceAccuracy);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        result = LocationServices.getSettingsClient(this).checkLocationSettings(locationSettingsRequest);

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    /**
                     * Check if Settings->Location is enabled/disabled
                     * Not app specific permission (location)
                     * Here I am talking of the scenario where Settings->Location is disabled and user runs the app.
                     */
                    // All location settings are satisfied. The client can initialize location

                    /**
                     * Runtime permissions are required on Android M and above to access User's location
                     */

                } catch (ApiException exception) {

                    /**
                     * Go in exception because Settings->Location is disabled.
                     * First it will Enable Location Services (GPS) then check for run time permission to app.
                     */
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),

                                /**
                                 * Display enable Enable Location Services (GPS) dialog like Google Map and then
                                 * check for run time permission to app.
                                 */
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
                                        welcom_page.this,
                                        REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            }
        });

        //askPermission & start service
        if (AppUtils.hasM() && !(ContextCompat.checkSelfPermission(welcom_page.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(welcom_page.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {

            Log.d("Iot", "ask permission");
            askPermissions();

        } else {

            startLocationService();

        }

    }

    /**
     * Ask user for permissions to access GPS location on Android M
     */
    public void askPermissions() {

        mBothPermissionRequest =
                PermissionUtil.with(this).request(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION).onResult(
                        new Func2() {
                            @Override
                            protected void call(int requestCode, String[] permissions, int[] grantResults) {

                                Log.d("Iot", "call: " + Arrays.toString(grantResults));

                                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                                    Log.d("Iot", "permission : " + Arrays.toString(permissions));
                                    startLocationService();

                                } else {

                                    Toast.makeText(welcom_page.this, R.string.permission_denied, Toast.LENGTH_LONG).show();
                                }
                            }

                        }).ask(PERMISSION_ACCESS_LOCATION_CODE);

    }

    @Override
    protected void onStop() {
        super.onStop();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationReceiver);
    }

   @Override
    protected  void onResume() {
        super.onResume();

        initList();
        Log.d("Iot", "onResume: " + groupList);

        adapter = new ArrayAdapter(this, R.layout.list_row, groupList);
        listView.setAdapter(adapter);

    }

    private class LocationReceiver extends BroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {


            if (null != intent && intent.getAction().equals(LOCATION_ACTION)) {

                //get data from locationService
                String locationData = intent.getStringExtra(LOCATION_MESSAGE);
                homeDistance = intent.getFloatExtra(LOCATION_homeDistance, 9999);
                CurLatitude = intent.getBundleExtra(LOCATION_CurLocation).getFloat("latitude", 9999);
                CurLongitude = intent.getBundleExtra(LOCATION_CurLocation).getFloat("longitude", 9999);


                Log.d("Iot", "homeDistance onReceive :" + homeDistance);

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (null != mBothPermissionRequest) {
            mBothPermissionRequest.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
