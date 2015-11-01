package com.project.hackbu;

import java.lang.ref.WeakReference;

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

import android.os.Bundle;
import android.view.View;
import android.app.Activity;
import android.os.AsyncTask;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class BandHeartRateAppActivity extends Activity {

    private BandClient client = null;
    private Button btnStart;
    private TextView hrtStatus;
    private TextView disStatus;

    private BandHeartRateEventListener mHeartRateEventListener = new BandHeartRateEventListener() {
        @Override
        public void onBandHeartRateChanged(final BandHeartRateEvent event) {
            if (event != null && event.getQuality() == HeartRateQuality.LOCKED) {
                appendToHrtText(String.format("Heart Rate: %d bpm\n",
                        event.getHeartRate()));
            } else {
                appendToHrtText(String.format("Heart Rate: -- bpm\n"));
            }
        }
    };

    private BandDistanceEventListener mDistanceEventListener = new BandDistanceEventListener() {
        @Override
        public void onBandDistanceChanged(BandDistanceEvent bandDistanceEvent) {
            if (bandDistanceEvent != null){
                appendToDisText(String.format("Current Motion: %s\n",
                        bandDistanceEvent.getMotionType()));
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hrtStatus = (TextView) findViewById(R.id.hrtStatus);
        disStatus = (TextView) findViewById(R.id.disStatus);
        btnStart = (Button) findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hrtStatus.setText("");
                disStatus.setText("");
                new HeartRateSubscriptionTask().execute();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        hrtStatus.setText("");
        disStatus.setText("");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (client != null) {
            try {
                client.getSensorManager().unregisterHeartRateEventListener(mHeartRateEventListener);
                client.getSensorManager().unregisterDistanceEventListener(mDistanceEventListener);
            } catch (BandIOException e) {
                appendToHrtText(e.getMessage());
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (client != null) {
            try {
                client.disconnect().await();
            } catch (InterruptedException e) {
                // Do nothing as this is happening during destroy
            } catch (BandException e) {
                // Do nothing as this is happening during destroy
            }
        }
        super.onDestroy();
    }

    private class HeartRateSubscriptionTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (getConnectedBandClient()) {
                    client.getSensorManager().registerHeartRateEventListener(mHeartRateEventListener);
                    client.getSensorManager().registerDistanceEventListener(mDistanceEventListener);

                } else {
                    appendToHrtText("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
                }
            } catch (BandException e) {
                String exceptionMessage="";
                switch (e.getErrorType()) {
                    case UNSUPPORTED_SDK_VERSION_ERROR:
                        exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.\n";
                        break;
                    case SERVICE_ERROR:
                        exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.\n";
                        break;
                    default:
                        exceptionMessage = "Unknown error occured: " + e.getMessage() + "\n";
                        break;
                }
                appendToHrtText(exceptionMessage);

            } catch (Exception e) {
                appendToHrtText(e.getMessage());
            }
            return null;
        }
    }

    private void appendToHrtText(final String string) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hrtStatus.setText(string);
            }
        });
    }

    private void appendToDisText(final String string){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                disStatus.setText(string);
            }
        });
    }

    private boolean getConnectedBandClient() throws InterruptedException, BandException {
        if (client == null) {
            BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
            if (devices.length == 0) {
                appendToHrtText("Band isn't paired with your phone.\n");
                return false;
            }
            client = BandClientManager.getInstance().create(getBaseContext(), devices[0]);
        } else if (ConnectionState.CONNECTED == client.getConnectionState()) {
            return true;
        }

        appendToHrtText("Band is connecting...\n");
        return ConnectionState.CONNECTED == client.connect().await();
    }
}

