package com.factual.engine.analytics.demo;

import com.factual.FactualError;
import com.factual.engine.analytics.AnalyticsEngineUtil;
import com.factual.engine.api.FactualPlace;
import com.factual.engine.api.FactualPlacesListener;
import com.factual.engine.api.PlaceCandidateResponse;
import com.segment.analytics.Analytics;

import java.util.UUID;

public class AnalyticsEnginePlaceCandidatesListener implements FactualPlacesListener {

    private Analytics analytics;

    public AnalyticsEnginePlaceCandidatesListener(Analytics analytics) {
        this.analytics = analytics;
    }

    @Override
    public void onPlacesResponse(PlaceCandidateResponse placeCandidateResponse) {
        String incidentId = (UUID.randomUUID()).toString();
        for (FactualPlace place : placeCandidateResponse.getCandidates()) {
            AnalyticsEngineUtil.logPlaceVisit(
                place, incidentId, "Place Candidate", analytics);
        }
    }

    @Override
    public void onPlacesError(FactualError factualError) {

    }
}
