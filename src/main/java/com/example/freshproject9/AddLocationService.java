package com.example.freshproject9;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AddLocationService extends IntentService implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<Status> {


    GoogleApiClient mGoogleApiClient;
    PendingIntent mGeofencePendingIntent ;

    public AddLocationService() {

        super("AddLocationService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    // The GeofenceRequest object.
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            Log.i(getClass().getSimpleName(),securityException.getMessage());
        }
    }
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER | GeofencingRequest.INITIAL_TRIGGER_DWELL);
        builder.addGeofences(getGeofecne());
        return builder.build();
    }
    private List<Geofence> getGeofecne(){
        List<Geofence> mGeofenceList = new ArrayList<>();

        //add one object
        mGeofenceList.add(new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId("key")

                // Set the circular region of this geofence.
                .setCircularRegion(
                        30.18633917, //lat
                        31.46183117, //long
                        50) // radios

                // Set the expiration duration of the geofence. This geofence gets automatically
                // removed after this period of time.
                //1000 millis  * 60 sec * 5 min
                .setExpirationDuration(1000 * 60 * 5)

                // Set the transition types of interest. Alerts are only generated for these
                // transition. We track entry and exit transitions in this sample.
                .setTransitionTypes(
                        Geofence.GEOFENCE_TRANSITION_DWELL)
                //Time before fire notification
                .setLoiteringDelay(3000)
                // Create the geofence.
                .build());
        return mGeofenceList;

    }
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.i(getClass().getSimpleName(),String.valueOf(i));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(getClass().getSimpleName(),connectionResult.getErrorMessage());
    }

    @Override
    public void onResult(@NonNull Status status) {
        if (status.isSuccess()) {
            Log.i(getClass().getSimpleName(),"Success");
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            Log.i(getClass().getSimpleName(),GeofenceTransitionsIntentService.getErrorString(status.getStatusCode()));
        }
    }
}
