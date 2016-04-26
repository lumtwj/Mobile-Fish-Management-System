package com.mocentury.fis.util;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.mocentury.fis.MainActivity;
import com.mocentury.fis.object.Data;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lumtwj on 23/4/16.
 */
public class LocationUtil implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    public static final long FASTEST_INTERVAL = 1000 * 30;

    MainActivity activity;
    String species;
    double length;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    public LocationUtil(MainActivity activity, String species, double length) {
        this.activity = activity;
        this.species = species;
        this.length = length;

        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
    }

    public void connect() {
        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();
    }

    public void disconnect() {
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(activity.getPackageName(), String.format("Pos (lat, long): %f, %f", location.getLatitude(), location.getLongitude()));

        Data d = new Data(species, length, location.getLatitude(), location.getLongitude());
        new AddFishApi(activity).execute(d);

        disconnect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public static String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
