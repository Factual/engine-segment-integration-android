package com.factual.engine.analytics;

import android.util.Log;

import com.factual.FactualCircumstance;
import com.factual.FactualException;
import com.factual.engine.FactualEngine;
import com.factual.engine.api.FactualCircumstanceException;
import com.factual.engine.api.FactualPlace;
import com.segment.analytics.Analytics;
import com.segment.analytics.Options;
import com.segment.analytics.Properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AnalyticsEngineUtil {

    public static final String USER_JOURNEY_CIRC_ID = "factual-segment-user-journey-circ-id";
    public static final String USER_JOURNEY_CIRC_EXPR = "(at any-factual-place)";

    public static void trackUserJourney() {
        addUserJourneyActionReceiver();
        FactualCircumstance circumstance =
            new FactualCircumstance(USER_JOURNEY_CIRC_ID, USER_JOURNEY_CIRC_EXPR,
                    AnalyticsEngineUserJourneyActionReceiver.ACTION_ID);
        Log.i("engine", "Registering circumstance: <" + circumstance.getCircumstanceId()
                + "," + circumstance.getExpression() + "," + circumstance.getActionId() + ">");
        try {
            FactualEngine.registerCircumstance(circumstance);
        }
        catch (FactualCircumstanceException e) {
            Log.e("engine", e.getMessage());
        }
    }

    public static void addUserJourneyActionReceiver() {
        Log.i("engine", "Registering action handler: "
                + AnalyticsEngineUserJourneyActionReceiver.ACTION_ID);
        FactualEngine.registerAction(AnalyticsEngineUserJourneyActionReceiver.ACTION_ID,
                AnalyticsEngineUserJourneyActionReceiver.class);
    }

    public static void logPlaceEntered(FactualPlace atPlace, String incidentId, Analytics analytics) {
        logPlaceVisit(atPlace, incidentId, "Place Entered", analytics);
    }

    public static void logPlaceNear(FactualPlace nearPlace, String incidentId, Analytics analytics) {
        logPlaceVisit(nearPlace, incidentId, "Place Near", analytics);
    }

    public static void logPlaceVisit(FactualPlace place, String incidentId, String eventName,
                                     Analytics analytics) {
        if(analytics != null) {
            String engineVersion;
            try {
                engineVersion = FactualEngine.getSdkVersion();
            }
            catch(FactualException e) {
                Log.e("engine", e.getMessage());
                engineVersion = "N/A";
            }
            analytics.track(
                eventName, makePropertiesFrom(place, incidentId), makeOptionsFrom(engineVersion));
        }
        else {
            Log.e("engine", "Analytics not initialized!");
        }
    }

    private static Properties makePropertiesFrom(FactualPlace place, String incidentId) {
        Properties p = new Properties();
        p.putValue("incidentId", incidentId);
        p.putValue("latitude", place.getLatitude());
        p.putValue("longitude", place.getLongitude());
        p.putValue("accuracy", place.getDistance());
        p.putValue("place_id", place.getFactualId());
        p.putValue("place_name", place.getName());
        p.putValue("confidence", place.getThresholdMet().toString().toLowerCase(Locale.getDefault()));
        if(place.getChainId() != null && place.getChainId().trim().length() > 0) {
            p.putValue("place_chain_id", place.getChainId());
        }
        if(place.getCategoryIds() != null && place.getCategoryIds().size() > 0) {
            List<String> cats = new ArrayList<>(place.getCategoryIds().size());
            for (Integer categoryId: place.getCategoryIds()) {
                cats.add(categoryId.toString());
            }
            p.putValue("place_categories", cats);
        }
        return p;
    }

    private static Options makeOptionsFrom(String engineVersion) {
        Map<String, Object> m = new HashMap<>();
        m.put("name", "Engine");
        m.put("version", engineVersion);

        Options o = new Options();
        o.setIntegrationOptions("Factual", m);
        return o;
    }
}