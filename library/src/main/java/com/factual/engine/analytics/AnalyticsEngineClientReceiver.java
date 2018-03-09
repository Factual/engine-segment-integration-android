package com.factual.engine.analytics;

import com.factual.engine.FactualClientReceiver;

public class AnalyticsEngineClientReceiver extends FactualClientReceiver {
    @Override
    public void onStarted() {
        AnalyticsEngineUtil.trackUserJourney();
    }

    @Override
    public void onStopped() {

    }
}
