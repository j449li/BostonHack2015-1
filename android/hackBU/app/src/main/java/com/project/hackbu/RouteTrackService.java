package com.project.hackbu;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by George on 2015-11-01.
 */
public class RouteTrackService extends Service implements
        GoogleApiClient.ConnectionCallbacks, LocationListener, GoogleApiClient.OnConnectionFailedListener {
    private static String TAG = MapsActivity.class.toString();

    // in milliseconds
    private static int REQUEST_FAST_INTERVAL = 7000;
    private static int REQUEST_INTERVAL = 4000;

    // meters
    private static int REQUEST_SMALLEST_DISPLACEMENT = 10;

    private GoogleApiClient mGoogleApiClient;

    // TODO to make a closed loop add the starting coord to the end
    private List<LatLng> routeCoord = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();

        buildGoogleApiClient();
        mGoogleApiClient.connect();
    }

    @Override
    public void onDestroy() {
        routeCoord.clear();
        mGoogleApiClient.disconnect();
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(REQUEST_FAST_INTERVAL);
        mLocationRequest.setFastestInterval(REQUEST_INTERVAL);
        mLocationRequest.setSmallestDisplacement(REQUEST_SMALLEST_DISPLACEMENT);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        return mLocationRequest;
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, createLocationRequest(), this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        routeCoord.add(new LatLng(latitude, longitude));
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, connectionResult.toString());
    }

}
