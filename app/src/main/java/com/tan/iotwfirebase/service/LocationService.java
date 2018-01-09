package com.tan.iotwfirebase.service;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tan.iotwfirebase.R;
import com.tan.iotwfirebase.Storage.AppPreferences;
import com.tan.iotwfirebase.Storage.IPreferenceConstants;
import com.tan.iotwfirebase.helper.ILocationConstants;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by tanli on 1/8/2018.
 */

public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ILocationConstants, IPreferenceConstants {


    private static final String TAG = LocationService.class.getSimpleName();

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;

    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;


    private String mLatitudeLabel;
    private String mLongitudeLabel;
    private String mLastUpdateTimeLabel;
    private String mDistance;
    private Long autoOnGps;
    private DatabaseReference mDatabaseReference;

    /**
     * Time when the location was updated represented as a String.
     */
    protected String mLastUpdateTime;

    private Location oldLocation;

    private Location newLocation;

    private Location homeLocation;


    private AppPreferences appPreferences;

    /**
     * Total distance covered
     */
    private float distance;
    private float homeLatitude;
    private  float homeLongitude;
    private float homeDistance;
    private Bundle homeCoor = new Bundle();

    @Override
    public void onCreate() {
        super.onCreate();

        appPreferences = new AppPreferences(this);

        oldLocation = new Location("Point A");
        newLocation = new Location("Point B");

        homeLocation = new Location("Home");

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        mLatitudeLabel = getString(R.string.latitude_label);
        mLongitudeLabel = getString(R.string.longitude_label);
        mLastUpdateTimeLabel = getString(R.string.last_update_time_label);
        mDistance = getString(R.string.distance);

        mLastUpdateTime = "";

        distance = appPreferences.getFloat(PREF_DISTANCE, 0);
        homeLatitude = appPreferences.getFloat(PREF_LATITUDE, 0);
        homeLongitude = appPreferences.getFloat(PREF_LONGITUDE, 0);
        homeLocation.setLatitude(homeLatitude);
        homeLocation.setLongitude(homeLongitude);

        Log.d("Iot", "onCreate Distance: " + distance);
        Log.d("Iot", "onCreate homeLocation: " + homeLocation);
        Log.d("Iot", "onCreate homeLocation: " + homeLocation);


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        buildGoogleApiClient();

        mGoogleApiClient.connect();

        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }

        return START_STICKY;

    }


    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {

        try {

            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);

        } catch (SecurityException ex) {


        }
    }


    /**
     * Updates the latitude, the longitude, and the last location time in the UI.
     */
    private void updateUI() {

        if (null != mCurrentLocation) {

            StringBuilder sbLocationData = new StringBuilder();
            sbLocationData.append(mLatitudeLabel)
                    .append(" ")
                    .append(mCurrentLocation.getLatitude())
                    .append("\n")
                    .append(mLongitudeLabel)
                    .append(" ")
                    .append(mCurrentLocation.getLongitude())
                    .append("\n")
                    .append(mLastUpdateTimeLabel)
                    .append(" ")
                    .append(mLastUpdateTime)
                    .append("\n")
                    .append(mDistance)
                    .append(" ")
                    .append(getUpdatedDistance())
                    .append(" meters");

            /*
             * update preference with latest value of distance
             */
            appPreferences.putFloat(PREF_DISTANCE, distance);

            Log.d("Iot", "Location Data:\n" + sbLocationData.toString());

            sendLocationBroadcast(sbLocationData.toString());

        } else {

            Toast.makeText(this, R.string.unable_to_find_location, Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Send broadcast using LocalBroadcastManager to update UI in activity
     *
     * @param sbLocationData
     */
    private void sendLocationBroadcast(String sbLocationData) {

        //send sbLocationData to mainActivity
        Intent locationIntent = new Intent();
        locationIntent.setAction(LOCATION_ACTION);
        locationIntent.putExtra(LOCATION_MESSAGE, sbLocationData);
        locationIntent.putExtra(LOCATION_homeDistance, homeDistance);
        locationIntent.putExtra(LOCATION_homeLocation, homeCoor);

        LocalBroadcastManager.getInstance(this).sendBroadcast(locationIntent);

    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {

        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }


    @Override
    public void onDestroy() {

        appPreferences.putFloat(PREF_DISTANCE, distance);

        stopLocationUpdates();

        mGoogleApiClient.disconnect();

        Log.d(TAG, "onDestroy Distance " + distance);


        super.onDestroy();
    }


    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) throws SecurityException {
        Log.i(TAG, "Connected to GoogleApiClient");


        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            updateUI();
        }

        startLocationUpdates();

    }

    /**
     * Callback that fires when the location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
        getOnGpsState();

    }

    @Override
    public void onConnectionSuspended(int cause) {

        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {

        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    private float getUpdatedDistance() {

        /**
         * There is 68% chance that user is with in 100m from this location.
         * So neglect location updates with poor accuracy
         */


        if (mCurrentLocation.getAccuracy() > ACCURACY_THRESHOLD) {

            return distance;
        }


        if (oldLocation.getLatitude() == 0 && oldLocation.getLongitude() == 0) {

            oldLocation.setLatitude(mCurrentLocation.getLatitude());
            oldLocation.setLongitude(mCurrentLocation.getLongitude());

            newLocation.setLatitude(mCurrentLocation.getLatitude());
            newLocation.setLongitude(mCurrentLocation.getLongitude());

            return distance;
        } else {

            oldLocation.setLatitude(newLocation.getLatitude());
            oldLocation.setLongitude(newLocation.getLongitude());

            newLocation.setLatitude(mCurrentLocation.getLatitude());
            newLocation.setLongitude(mCurrentLocation.getLongitude());

        }


        /**
         * Calculate distance between last two geo locations
         */
        distance = newLocation.distanceTo(oldLocation);

        //get homeLocation from phone storage
        homeLatitude = appPreferences.getFloat(PREF_LATITUDE, 0);
        homeLongitude = appPreferences.getFloat(PREF_LONGITUDE, 0);
        homeLocation.setLatitude(homeLatitude);
        homeLocation.setLongitude(homeLongitude);

        homeDistance = newLocation.distanceTo(homeLocation);


        Log.d("Iot", "getHomeDistance calculated: " + homeDistance);
        Log.d("Iot", "Oncreate homeLatitude :" + homeLatitude);
        Log.d("Iot", "Oncreate homeLongitude :" + homeLongitude);

        try{
            if(homeDistance <= 5000 && autoOnGps == 1){

                try {
                    mDatabaseReference.child("led_switch").setValue(1);
                    Log.d("Iot", "Distance in Range on LED");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(homeDistance > 5000 && autoOnGps == 1){

                try {
                    mDatabaseReference.child("led_switch").setValue(0);
                    Log.d("Iot", "Distance Out of Range on LED");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        //save current location as home location
        homeCoor.clear();
        homeCoor.putFloat("latitude", (float)mCurrentLocation.getLatitude());
        homeCoor.putFloat("longitude", (float)mCurrentLocation.getLongitude());

        return distance;
    }


    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void getOnGpsState(){

        mDatabaseReference.child("autoOnGps").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()) {
                    autoOnGps = (long) dataSnapshot.getValue();
                    Log.d("Iot", "autoOnGps Backgroud : " + autoOnGps);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
