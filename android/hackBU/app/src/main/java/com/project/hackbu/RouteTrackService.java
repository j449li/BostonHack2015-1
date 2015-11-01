package com.project.hackbu;

import android.app.Activity;
import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandIOException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sensors.BandDistanceEvent;
import com.microsoft.band.sensors.BandDistanceEventListener;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.HeartRateConsentListener;
import com.microsoft.band.sensors.HeartRateQuality;
import com.microsoft.band.sensors.MotionType;
import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.BandIOException;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.sensors.BandDistanceEvent;
import com.microsoft.band.sensors.BandDistanceEventListener;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.HeartRateQuality;
import com.project.hackbu.util.ApiClient;
import com.project.hackbu.util.HTTPService;
import com.project.hackbu.util.UserData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

/**
 * Created by George on 2015-11-01.
 */
public class RouteTrackService extends Service implements
        GoogleApiClient.ConnectionCallbacks, LocationListener, GoogleApiClient.OnConnectionFailedListener {
    private static String TAG = RouteTrackService.class.toString();

    public static String ACTION_NEW_COORD = "com.project.hackbu.action_new_coord";
    public static String ACTION_ALL_COORDS = "com.project.hackbu.action_all_coords";
    public static String ACTION_LAZY_USER = "com.project.hackbu.action_lazy_user";

    public static String EXTRA_LATITUDE = "com.project.hackbu.latitude";
    public static String EXTRA_LONGITUDE = "com.project.hackbu.longitude";
    public static String EXTRA_LATITUDE_LIST = "com.project.hackbu.latitude_list";
    public static String EXTRA_LONGITUDE_LIST = "com.project.hackbu.longitude_list";

    // in milliseconds
    private static int REQUEST_FAST_INTERVAL = 3000;
    private static int REQUEST_INTERVAL = 5000;

    // meters
    private static int REQUEST_SMALLEST_DISPLACEMENT = 3;

    private GoogleApiClient mGoogleApiClient;
    private BroadcastReceiver receiver;

    // TODO to make a closed loop add the starting coord to the end
    private LinkedList<LatLng> runningAvg = new LinkedList<>();
    private List<LatLng> routeCoord = new ArrayList<>();

    private BandClient client = null;
    public static int recentHeartRate = 0;
    public static MotionType recentMotionType = MotionType.IDLE;
    private static int HEART_RATE_THRESHOLD = 70;

    private List<Integer> recentHBs = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();

        buildGoogleApiClient();
        mGoogleApiClient.connect();

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (action.equals(MapsActivity.ACTION_GET_COORDS)) {
                    Intent response = new Intent();
                    response.setAction(RouteTrackService.ACTION_ALL_COORDS);

                    int size = routeCoord.size();
                    double latitudes[] = new double[size];
                    double longitudes[] = new double[size];

                    for (int i = 0; i < size; i++) {
                        LatLng latLng = routeCoord.get(i);
                        latitudes[i] = latLng.latitude;
                        longitudes[i] = latLng.longitude;
                    }
                    response.putExtra(RouteTrackService.EXTRA_LATITUDE_LIST, latitudes);
                    response.putExtra(RouteTrackService.EXTRA_LONGITUDE_LIST, longitudes);
                    sendBroadcast(response);
                } else if (action.equals(MapsActivity.ACTION_STOP)) {
                    unregisterReceiver(receiver);
                    try {
                        makeUpdateToServer();
                    } catch (JSONException e) {
                        Log.e(TAG, e.toString());
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, e.toString());
                    }

                    stopSelf();
                }
            }
        };

        registerReceiver(receiver, new IntentFilter(MapsActivity.ACTION_GET_COORDS));
        registerReceiver(receiver, new IntentFilter(MapsActivity.ACTION_STOP));
    }

    @Override
    public void onDestroy() {
        routeCoord.clear();
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        mGoogleApiClient.disconnect();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void makeUpdateToServer() throws  UnsupportedEncodingException, JSONException{
        final JSONObject requestJson = new JSONObject();
        requestJson.put("player_id", UserData.getInstance().getData(UserData.ID));

        JSONArray arr = new JSONArray();
        for (LatLng ll : routeCoord) {
            JSONObject json1 = new JSONObject();
            json1.put("latitude", ll.latitude);
            json1.put("longitude", ll.longitude);
            arr.put(json1);
        }

        requestJson.put("points", arr);

        Log.e(TAG, requestJson.toString());

        ApiClient.post(getApplicationContext(), "/map/update", new StringEntity(requestJson.toString()), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (statusCode == 200) {
                    Log.d(TAG, "UPDATE SUCCESSFUL");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                Log.e(TAG, ""+statusCode);
//                Log.d(TAG, response.toString());
                //Log.d(TAG, requestJson.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, responseString);
                //Log.d(TAG, requestJson.toString());
            }

        });
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
        mLocationRequest.setInterval(REQUEST_INTERVAL);
        mLocationRequest.setFastestInterval(REQUEST_FAST_INTERVAL);
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

    private boolean isUserLazy() {
        int hbr = recentHeartRate;
        if (hbr < HEART_RATE_THRESHOLD) {
            recentHBs.add(hbr);
            if (recentHBs.size() > 4) {
                Intent intent = new Intent();
                intent.setAction(ACTION_LAZY_USER);
                sendBroadcast(intent);

                stopSelf();
                return true;
            }
            return false;
        } else {
            recentHBs.clear();
            return false;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (isUserLazy()) {
            return;
        }

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng newLatLng = new LatLng(latitude, longitude);

        if (runningAvg.size() == 7) {
            runningAvg.removeFirst();
        }

        runningAvg.add(new LatLng(latitude, longitude));

        LatLng smoothedLatLng = calcRunningAvg();
        routeCoord.add(smoothedLatLng);

        // broadcast here
        Intent intent = new Intent();
        intent.setAction(ACTION_NEW_COORD);
        intent.putExtra(EXTRA_LATITUDE, latitude);
        intent.putExtra(EXTRA_LONGITUDE, longitude);
        sendBroadcast(intent);
    }

    private LatLng calcRunningAvg() {
        double totLat = 0.0;
        double totLong = 0.0;

        for (LatLng ll : runningAvg) {
            totLat += ll.latitude;
            totLong += ll.longitude;
        }

        return new LatLng(totLat/runningAvg.size(), totLong/runningAvg.size());
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, connectionResult.toString());
    }

}
