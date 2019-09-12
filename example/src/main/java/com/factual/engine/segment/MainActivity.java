package com.factual.engine.segment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.factual.engine.FactualEngine;
import com.segment.analytics.Analytics;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "BrazeEngineExampleApp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Set up Segment */
        Context context = getApplicationContext();
        Analytics analytics = new Analytics.Builder(context, Configuration.SEGMENT_WRITE_KEY)
            .flushQueueSize(1)
            .build();
        Analytics.setSingletonInstance(analytics);

        /* Set up Engine */
        if (!isRequiredPermissionAvailable()) {
            requestLocationPermissions();
        } else {
            initializeEngine();
        }
    }

    public void initializeEngine() {
        Log.i("engine", "starting initialization");
        FactualEngine.initialize(getApplicationContext(),
            Configuration.ENGINE_API_KEY,
            ExampleFactualClientReceiver.class);
    }

    /* Permission Boiler plate */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
        @NonNull int[] grantResults) {
        if (isRequiredPermissionAvailable()) {
            initializeEngine();
        } else {
            Log.e(TAG, "Necessary permissions were never provided.");
        }
    }

    public boolean isRequiredPermissionAvailable() {
        return ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.INTERNET
            },
            0);
    }
}