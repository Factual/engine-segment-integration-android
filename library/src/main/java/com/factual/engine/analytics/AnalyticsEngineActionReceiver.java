package com.factual.engine.analytics;

import com.factual.FactualCircumstance;
import com.factual.FactualError;
import com.factual.engine.api.CircumstanceResponse;
import com.factual.engine.api.FactualActionReceiver;
import com.factual.engine.api.FactualPlace;
import com.segment.analytics.Analytics;

import java.util.List;
import java.util.UUID;

public class AnalyticsEngineActionReceiver extends FactualActionReceiver {
    static final String ACTION_ID = "factual-segment-default-action-id";

    @Override
    public void onCircumstancesMet(List<CircumstanceResponse> circumstanceResponses) {
        Analytics analytics = Analytics.with(getContext().getApplicationContext());

        for (CircumstanceResponse circumstanceResponse : circumstanceResponses) {
            UUID uuid = UUID.randomUUID();
            for (FactualPlace place : circumstanceResponse.getAtPlaces()) {
                AnalyticsEngineUtil.logPlaceEntered(place, uuid.toString(), analytics);
            }
            for (FactualPlace place : circumstanceResponse.getNearPlaces()) {
                AnalyticsEngineUtil.logPlaceNear(place, uuid.toString(), analytics);
            }
        }
    }

    @Override
    public void onCircumstanceError(FactualCircumstance factualCircumstance, FactualError factualError) {
    }
}
