package com.factual.engine.analytics;

import com.factual.FactualConfigMetadata;
import com.factual.FactualError;
import com.factual.FactualInfo;
import com.factual.engine.FactualClientReceiver;

public class AnalyticsEngineClientReceiver extends FactualClientReceiver {
    @Override
    public void onStarted() {
        AnalyticsEngineUtil.trackAllPlaceVisits();
    }

    @Override
    public void onStopped() {

    }

    @Override
    public void onError(FactualError factualError) {

    }

    @Override
    public void onInfo(FactualInfo factualInfo) {

    }

    @Override
    public void onSyncWithGarageComplete() {

    }

    @Override
    public void onConfigLoad(FactualConfigMetadata factualConfigMetadata) {

    }
}
