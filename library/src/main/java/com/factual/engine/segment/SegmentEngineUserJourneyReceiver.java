package com.factual.engine.segment;

import android.content.Context;
import android.util.Log;
import com.factual.engine.api.FactualPlace;
import com.factual.engine.api.mobile_state.UserJourneyEvent;
import com.factual.engine.api.mobile_state.UserJourneyReceiver;
import com.factual.engine.api.mobile_state.UserJourneySpan;
import com.factual.engine.integrationutils.*;
import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;
import java.util.List;

public class SegmentEngineUserJourneyReceiver extends UserJourneyReceiver {

  // Key constants
  static final String SPAN_ID_KEY = "span_id";
  static final String EVENT_SOURCE_KEY = "event_source";

  static final String START_TIME_UNAVAILABLE_KEY = "start_time_unavailable";
  static final String END_TIME_UNAVAILABLE_KEY = "end_time_unavailable";
  static final String START_TIMESTAMP_KEY = "start_timestamp";
  static final String END_TIMESTAMP_KEY = "end_timestamp";
  static final String DURATION_KEY = "duration";

  static final String IS_HOME_KEY = "is_home";
  static final String IS_WORK_KEY = "is_work";
  static final String INGRESS_LATITUDE_KEY = "ingress_latitude";
  static final String INGRESS_LONGITUDE_KEY = "ingress_longitude";

  static final String COUNTRY_KEY = "country";
  static final String LOCALITIES_KEY = "localities";
  static final String POSTCODE_KEY = "postcode";
  static final String REGION_KEY = "region";

  static final String NAME_KEY = "name";
  static final String PLACE_ID_KEY = "factual_id";
  static final String LATITUDE_KEY = "latitude";
  static final String LONGITUDE_KEY = "longitude";
  static final String CATEGORIES_KEY = "category_labels";
  static final String CHAIN_KEY = "chain_name";
  static final String DISTANCE_KEY = "distance";
  static final String LOCALITY_KEY = "locality";

  @Override
  public void onUserJourneyEvent(UserJourneyEvent userJourneyEvent) { /* Not supported */ }

  @Override
  public void onUserJourneySpan(UserJourneySpan userJourneySpan) {
    // Only send span data when did travel is false and tracking User Journey Spans is enabled
    if (SegmentEngineIntegration.isTrackingSpans() && !userJourneySpan.didTravel()) {
      Context context = getContext().getApplicationContext();
      handleUserJourneySpan(context, userJourneySpan);
    }
  }

  // Sends span data to Segment
  void handleUserJourneySpan(Context context, UserJourneySpan span) {
    Analytics analytics = Analytics.with(context);
    Properties properties = new Properties();

    // Span information
    String spanId = span.getSpanId();
    boolean startTimestampUnavailable = span.isStartTimestampUnavailable();
    boolean endTimestampUnavailable = span.isEndTimestampUnavailable();
    double startTimestamp = span.getStartTimestamp();
    double endTimestamp = span.getEndTimestamp();

    // Get duration of span in seconds
    double duration = !startTimestampUnavailable && !endTimestampUnavailable ?
        (endTimestamp - startTimestamp) : 0;

    // Get current place data
    PlaceVisitData currentPlace = new PlaceVisitData(span.getCurrentPlace());

    // Populate properties
    properties.putValue(SPAN_ID_KEY, spanId)
        .putValue(EVENT_SOURCE_KEY, SegmentEngineIntegration.sourceName)
        .putValue(START_TIME_UNAVAILABLE_KEY, startTimestampUnavailable)
        .putValue(END_TIME_UNAVAILABLE_KEY, endTimestampUnavailable)
        .putValue(START_TIMESTAMP_KEY, startTimestamp)
        .putValue(END_TIMESTAMP_KEY, endTimestamp)
        .putValue(DURATION_KEY, duration)
        .putValue(COUNTRY_KEY, currentPlace.getCountry())
        .putValue(LOCALITIES_KEY, currentPlace.getLocalities())
        .putValue(POSTCODE_KEY, currentPlace.getPostcode())
        .putValue(REGION_KEY, currentPlace.getRegion())
        .putValue(INGRESS_LATITUDE_KEY, currentPlace.getIngressLatitude())
        .putValue(INGRESS_LONGITUDE_KEY, currentPlace.getIngressLongitude())
        .putValue(IS_HOME_KEY, currentPlace.isHome())
        .putValue(IS_WORK_KEY, currentPlace.isWork());

    // Send data to Segment
    Log.i(SegmentEngineIntegration.TAG, "Sending user journey span event to Segment");
    analytics.track(SegmentEngineIntegration.ENGINE_SPAN_EVENT_KEY, properties);

    // Send attached places data if there are any to send
    int numPlaceEvents = getNumPlaceEvents(context, currentPlace.getNumPlaces());
    if (numPlaceEvents > 0) {
      Log.i(SegmentEngineIntegration.TAG,
          String.format("Sending %d attached place event(s) to Segment", numPlaceEvents));
      sendPlacesData(currentPlace.getPlaces(), spanId, analytics, numPlaceEvents);
    }
  }

  // Sends attached places data to Segment
  private void sendPlacesData(List<FactualPlace> places, String spanId, Analytics analytics, int numPlaceEvents) {
    Properties properties = new Properties();
    properties.putValue(EVENT_SOURCE_KEY, SegmentEngineIntegration.sourceName)
        .putValue(SPAN_ID_KEY, spanId);

    // Loop through attached places
    for (int index = 0; index < numPlaceEvents; index++) {
      FactualPlace place = places.get(index);

      // Add properties
      properties.putValue(NAME_KEY, place.getName())
          .putValue(CATEGORIES_KEY, PlaceCategoryMap.getPlaceCategories(place))
          .putValue(CHAIN_KEY, PlaceChainMap.getChain(place))
          .putValue(PLACE_ID_KEY, place.getFactualId())
          .putValue(LATITUDE_KEY, place.getLatitude())
          .putValue(LONGITUDE_KEY, place.getLongitude())
          .putValue(DISTANCE_KEY, place.getDistance())
          .putValue(LOCALITY_KEY, place.getLocality())
          .putValue(REGION_KEY, place.getRegion())
          .putValue(COUNTRY_KEY, place.getCountry())
          .putValue(POSTCODE_KEY, place.getPostcode());

      // Push custom event to Segment
      analytics.track(SegmentEngineIntegration.ENGINE_SPAN_ATTACHED_PLACE_EVENT_KEY, properties);
    }
  }

  // Gets number of attached place events to send
  private static int getNumPlaceEvents(Context context, Integer numAvailableAttachedPlaces) {
    int maxAtPlaceEventsPerCircumstance = context
        .getSharedPreferences(SegmentEngineIntegration.class.getName(),
            Context.MODE_PRIVATE)
        .getInt(
            SegmentEngineIntegration.NUM_MAX_ATTACHED_PLACE_EVENTS_PER_SPAN_KEY,
            SegmentEngineIntegration.NUM_MAX_ATTACHED_PLACE_EVENTS_PER_SPAN_DEFAULT
        );

    return Math.min(maxAtPlaceEventsPerCircumstance, numAvailableAttachedPlaces);
  }
}
