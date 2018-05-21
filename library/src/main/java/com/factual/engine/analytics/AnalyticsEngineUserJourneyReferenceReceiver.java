package com.factual.engine.analytics;

import android.util.Log;

import com.factual.engine.api.mobile_state.UserJourneyEvent;
import com.factual.engine.api.mobile_state.UserJourneyReceiver;
import com.segment.analytics.Analytics;

import org.json.JSONException;

public class AnalyticsEngineUserJourneyReferenceReceiver extends UserJourneyReceiver {
    @Override
    public void onUserJourneyEvent(UserJourneyEvent userJourneyEvent) {
        try {
            Log.i("engine", "Received User Journey event: " + userJourneyEvent.toJson().toString());
        } catch (JSONException e) {
            Log.e("engine", "Error with User Journey json");
        }
        Analytics analytics = Analytics.with(getContext().getApplicationContext());
        AnalyticsEngineUtil.trackUserJourneyEvent(userJourneyEvent, analytics);
    }
}
