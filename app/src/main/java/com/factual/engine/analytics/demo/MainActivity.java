package com.factual.engine.analytics.demo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.factual.FactualException;
import com.factual.engine.FactualEngine;
import com.factual.engine.analytics.AnalyticsEngineClientReceiver;
import com.factual.engine.api.FactualCircumstanceException;
import com.factual.engine.api.FactualPlacesListener;
import com.segment.analytics.Analytics;

public class MainActivity extends AppCompatActivity {

    private final String SEGMENT_WRITE_KEY = "WRITE KEY GOES HERE";
    private final String FACTUAL_ENGINE_API_KEY = "ENGINE KEY GOES HERE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            initializeEngine();

            if (isRequiredPermissionAvailable()) {
                startEngineAndSetUpButtons();
            }
            else {
                requestLocationPermissions();
            }
        }
        catch (FactualException e) {
            Log.e("engine", e.getMessage());
        }
    }

    private void initializeEngine() throws FactualException {
        FactualEngine.initialize(this, FACTUAL_ENGINE_API_KEY);
    }

    private void startEngineAndSetUpButtons() {
        createAnalytics();
        startEngineWithAnalytics();
        addButtonForCircumstanceDetection();
        addButtonForFindingPlaces();
    }

    private Analytics createAnalytics() {
        Analytics analytics =
                new Analytics.Builder(this, SEGMENT_WRITE_KEY)
                        .trackApplicationLifecycleEvents()
                        .recordScreenViews()
                        .logLevel(Analytics.LogLevel.VERBOSE)
                        .build();

        // Set the initialized instance as a globally accessible instance.
        Analytics.setSingletonInstance(analytics);
        return Analytics.with(this);
    }

    private void startEngineWithAnalytics(){
        FactualEngine.setListener(AnalyticsEngineClientReceiver.class);
        try {
            Log.i("engine-demo", "Trying to start Engine");
            FactualEngine.start();
        } catch (FactualException e) {
            Log.e("engine", e.getMessage());
        }
    }

    public boolean isRequiredPermissionAvailable(){
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED;
    }

    private void addButtonForCircumstanceDetection() {
        final Button button = findViewById(R.id.button_id);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    Log.i("engine", "Detecting circumstances now");
                    FactualEngine.runCircumstances();
                }
                catch (FactualCircumstanceException e) {
                    Log.e("engine", e.getMessage());
                }
            }
        });
    }

    private void addButtonForFindingPlaces() {
        final Button button = findViewById(R.id.whats_around_button_id);
        final Analytics analytics = Analytics.with(this);
        button.setOnClickListener(new View.OnClickListener() {
            FactualPlacesListener l = new AnalyticsEnginePlaceCandidatesListener(analytics);
            public void onClick(View v) {
                try {
                    Log.i("engine", "What's around?");
                    FactualEngine.getPlaceCandidates(l);
                }
                catch (FactualException e) {
                    Log.e("engine", e.getMessage());
                }
            }
        });
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

    // example permissions boilerplate
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (isRequiredPermissionAvailable()) {
            startEngineAndSetUpButtons();
        } else {
            Log.e("engine", "Necessary permissions were never provided.");
        }
    }

}