package com.factual.engine.analytics;

import com.factual.FactualCircumstance;
import com.factual.engine.api.CircumstanceResponse;
import com.factual.engine.api.FactualActionReceiver;
import com.factual.engine.api.FactualPlace;
import com.segment.analytics.Analytics;

import java.util.List;
import java.util.UUID;

public class AnalyticsEngineTrackActionReceiver extends FactualActionReceiver {
    public static final String ACTION_ID = "factual-engine-segment-analytics-action-id";

    @Override
    public void onCircumstancesMet(List<CircumstanceResponse> circumstanceResponses) {
        Analytics analytics = Analytics.with(getContext().getApplicationContext());

        for (CircumstanceResponse circumstanceResponse : circumstanceResponses) {
            UUID uuid = UUID.randomUUID();
            FactualCircumstance circ = circumstanceResponse.getCircumstance();
            for (FactualPlace place : circumstanceResponse.getAtPlaces()) {
                AnalyticsEngineUtil.logPlaceEntered(circ, place, uuid.toString(), analytics);
            }
            for (FactualPlace place : circumstanceResponse.getNearPlaces()) {
                AnalyticsEngineUtil.logPlaceApproached(circ, place, uuid.toString(), analytics);
            }
        }
    }
}
