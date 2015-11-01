package com.project.hackbu;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static String TAG = MapsActivity.class.toString();

    public static String ACTION_GET_COORDS = "com.project.hackbu.action_get_coords";
    public static String ACTION_STOP = "com.project.hackbu.action_stop";

    public static int INITIAL_ZOOM_LVL = 15;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private BroadcastReceiver receiver;

    private Button btnStartStop;
    private Button btnRanking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_maps);

        setUpMapIfNeeded();

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (action.equals(RouteTrackService.ACTION_NEW_COORD)) {
                    double latitude = intent.getDoubleExtra(RouteTrackService.EXTRA_LATITUDE, -1);
                    double longitude = intent.getDoubleExtra(RouteTrackService.EXTRA_LONGITUDE, -1);

                    if (latitude != -1 && longitude != -1) {
                        addMarker(latitude, longitude);
                    }

                } else if (action.equals(RouteTrackService.ACTION_ALL_COORDS)) {

                    double latitude = intent.getDoubleExtra(RouteTrackService.EXTRA_LATITUDE, -1);
                    double longitude = intent.getDoubleExtra(RouteTrackService.EXTRA_LONGITUDE, -1);

                    if (latitude != -1 && longitude != -1) {
                        addMarker(latitude, longitude);
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
                    }

                    double latitudes[] = intent.getDoubleArrayExtra(RouteTrackService.EXTRA_LATITUDE_LIST);
                    double longitudes[] = intent.getDoubleArrayExtra(RouteTrackService.EXTRA_LONGITUDE_LIST);

                    if (latitudes.length == longitudes.length) {
                        for (int i =0 ; i < latitudes.length; i++) {
                            addMarker(latitudes[i], longitudes[i]);
                        }
                    }

                }
            }
        };
        registerReceiver(receiver, new IntentFilter(RouteTrackService.ACTION_NEW_COORD));
        registerReceiver(receiver, new IntentFilter(RouteTrackService.ACTION_ALL_COORDS));

        btnStartStop = (Button) findViewById(R.id.btnStartStop);
        btnRanking = (Button) findViewById(R.id.btnRanking);

        btnStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btnStartStop.getText().equals("Start")) {
                    if (!isMyServiceRunning(RouteTrackService.class)) {
                        Toast.makeText(getApplicationContext(), "Tracking Started", Toast.LENGTH_SHORT).show();

                        startService(new Intent(MapsActivity.this, RouteTrackService.class));
                    }
                    btnStartStop.setText("End");
                    btnStartStop.setBackgroundColor(Color.RED);
                    startService(new Intent(MapsActivity.this, RouteTrackService.class));
                }
                else {
                    btnStartStop.setText("Start");
                    btnStartStop.setBackgroundColor(Color.GREEN);
                    Toast.makeText(getApplicationContext(), "Tracking Stopped", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent();
                    intent.setAction(ACTION_STOP);
                    sendBroadcast(intent);

                    mMap.clear();
                }
            }
        });
        btnRanking.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, RankingActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        if (isMyServiceRunning(RouteTrackService.class)) {
            Intent intent = new Intent();
            intent.setAction(MapsActivity.ACTION_GET_COORDS);
            sendBroadcast(intent);
        } else {
            startService(new Intent(this, RouteTrackService.class));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            finish();
        }

        mMap.animateCamera(CameraUpdateFactory.zoomTo(INITIAL_ZOOM_LVL));
    }

    private void addMarker(double latitude, double longitude) {
        LatLng latLng = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(latLng));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    // I believe the more correct approach is to have the service set a global variable of its state
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
