package com.factual.engine.analytics;

import android.util.Log;

import com.factual.FactualCircumstance;
import com.factual.FactualException;
import com.factual.engine.FactualEngine;
import com.factual.engine.api.FactualPlace;
import com.factual.engine.api.mobile_state.FactualPlaceAttachmentUpdate;
import com.factual.engine.api.mobile_state.UserJourneyEvent;
import com.segment.analytics.Analytics;
import com.segment.analytics.Options;
import com.segment.analytics.Properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class AnalyticsEngineUtil {

    private static final String PLACE_ENTERED = "Place Entered";
    private static final String PLACE_APPROACHED = "Place Approached";

    public static void addTrackActionReceiver() {
        Log.i("engine", "Registering action handler: "
                + AnalyticsEngineTrackActionReceiver.ACTION_ID);
        FactualEngine.registerAction(AnalyticsEngineTrackActionReceiver.ACTION_ID,
                AnalyticsEngineTrackActionReceiver.class);
    }

    public static void trackUserJourneyEvent(UserJourneyEvent userJourneyEvent,
                                             Analytics analytics) {
        UUID uuid = UUID.randomUUID();
        if(userJourneyEvent.hasPlaceAttachmentUpdate()) {
            FactualPlaceAttachmentUpdate u = userJourneyEvent.getPlaceAttachmentUpdate();
            for(FactualPlace atPlace: u.getNewlyAttachedPlaces()) {
                logPlaceEntered(atPlace, uuid.toString(), analytics);
            }
        }
    }


    public static void logPlaceEntered(FactualPlace atPlace, String incidentId,
                                       Analytics analytics) {
        logPlaceVisit(atPlace, incidentId, PLACE_ENTERED, analytics);
    }

    public static void logPlaceApproached(FactualPlace nearPlace, String incidentId,
                                          Analytics analytics) {
        logPlaceVisit(nearPlace, incidentId, PLACE_APPROACHED, analytics);
    }

    public static void logPlaceVisit(FactualPlace place, String incidentId, String eventName,
                                     Analytics analytics) {
        logPlaceVisit(place, incidentId, eventName, null, analytics);
    }

    public static void logPlaceEntered(FactualCircumstance circumstance, FactualPlace atPlace,
                                       String incidentId, Analytics analytics) {
        logPlaceVisit(circumstance, atPlace, incidentId, PLACE_ENTERED, analytics);
    }

    public static void logPlaceApproached(FactualCircumstance circumstance, FactualPlace nearPlace,
                                          String incidentId, Analytics analytics) {
        logPlaceVisit(circumstance, nearPlace, incidentId, PLACE_APPROACHED, analytics);
    }

    public static void logPlaceVisit(FactualCircumstance circumstance, FactualPlace place,
                                     String incidentId, String eventName, Analytics analytics) {
        Map<String, String> eventProps = new HashMap<>();
        if(circumstance != null) {
            eventProps.put("circumstance_id", circumstance.getCircumstanceId());
            eventProps.put("circumstance_expression", circumstance.getExpression());
        }
        logPlaceVisit(place, incidentId, eventName, eventProps, analytics);
    }

    public static void logPlaceVisit(FactualPlace place, String incidentId, String eventName,
                                     Map<String, String> eventProperties, Analytics analytics) {
        if(analytics != null) {
            String engineVersion;
            try {
                engineVersion = FactualEngine.getSdkVersion();
            }
            catch(FactualException e) {
                Log.e("engine", e.getMessage());
                engineVersion = "N/A";
            }
            Properties placeProps = makePropertiesFrom(place, incidentId);
            if(eventProperties != null && !eventProperties.isEmpty()) {
                placeProps.putAll(eventProperties);
            }
            analytics.track(eventName, placeProps, makeOptionsFrom(engineVersion));
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
        p.putValue("confidence",
                place.getThresholdMet().toString().toLowerCase(Locale.getDefault()));
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