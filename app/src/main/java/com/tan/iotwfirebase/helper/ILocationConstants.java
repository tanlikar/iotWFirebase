package com.tan.iotwfirebase.helper;

/**
 * Created by tanli on 1/8/2018.
 */

public interface ILocationConstants {


    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * If accuracy is lesser than 100m , discard it
     */
    int ACCURACY_THRESHOLD = 100;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;


    /**
     * Broadcast Receiver Action to update location
     */
    String LOCATION_ACTION = "com.tan.iotwfirebase.LOCATION_ACTION";

    /**
     * Message key for data with in the broadcast
     */
    String LOCATION_MESSAGE = "com.tan.iotwfirebase.LOCATION_DATA";

    String LOCATION_homeDistance = "com.tan.iotwfirebase.distance";

    String LOCATION_CurLocation = "com.tan.iotwfirebase.CurLocation";

    String LOCATION_AutoGpsState = "com.tan.iotwfirebase.AutoGpsState";

    /***
     * Request code while asking for permissions
     */
    int PERMISSION_ACCESS_LOCATION_CODE = 123;


}

