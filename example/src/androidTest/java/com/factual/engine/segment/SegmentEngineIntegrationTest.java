package com.factual.engine.segment;

import android.content.Context;
import android.location.Location;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import com.factual.engine.api.CircumstanceResponse;
import com.factual.engine.api.FactualCircumstance;
import com.factual.engine.api.FactualPlace;
import com.factual.engine.api.PlaceConfidenceThreshold;
import com.factual.engine.api.mobile_state.FactualPlaceVisit;
import com.factual.engine.api.mobile_state.UserJourneySpan;
import com.segment.analytics.Analytics;
import com.segment.analytics.Traits;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Example tests for Segment Engine integration
 */
@RunWith(AndroidJUnit4.class)
public class SegmentEngineIntegrationTest {

  // Keys used in Braze API
  private static String API_KEY = "api_key";
  private static String EMAIL_ADDRESS = "email_address";
  private static String ID_KEY = "external_id";
  private static String USERS_KEY = "users";
  private static String CUSTOM_EVENTS_KEY = "custom_events";
  private static String EVENT_NAME_KEY = "name";
  private static String COUNT_KEY = "count";

  private static Context appContext;

  /**
   * Setup Segment Engine
   */
  @BeforeClass
  public static void setUp() {
    appContext = InstrumentationRegistry.getTargetContext();

    // Configure Segment
    Analytics analytics = new Analytics.Builder(appContext, StubConfiguration.SEGMENT_WRITE_KEY)
        .flushQueueSize(1)
        .build();
    Analytics.setSingletonInstance(analytics);
    Traits traits = new Traits().putEmail(StubConfiguration.TEST_USER_EMAIL);
    analytics.identify(StubConfiguration.TEST_USER_ID, traits, null);
  }

  /**
   * Tests that Segment Engine Integration is sending circumstances
   */
  @Test
  public void testSegmentEngineCircumstances() {
    // Get events we are expecting to be sent to Braze through Segment
    String circName = StubConfiguration.CIRCUMSTANCE_NAME;
    String circMetEventName = SegmentEngineIntegration.CIRCUMSTANCE_MET_EVENT_KEY + circName;
    String atPlaceEventName = SegmentEngineIntegration.AT_PLACE_EVENT_KEY + circName;
    HashSet<String> events = new HashSet<>();
    events.add(circMetEventName);
    events.add(atPlaceEventName);

    // Get counts for these events
    Map<String, Integer> beforeEventCounts = getCountsForEvents(events);

    // Push a circumstance event to Segment
    SegmentEngineIntegration.pushToSegment(appContext, createCircumstance(), 1, 0);

    // Wait for data to be sent to Segment and pushed to Braze
    delay(60);

    // Get counts for events after being tracked
    Map<String, Integer> afterEventCounts = getCountsForEvents(events);

    Integer circumstanceCountBefore = beforeEventCounts.get(circMetEventName);
    Integer circumstanceCountAfter = afterEventCounts.get(circMetEventName);
    Assert.assertEquals(circumstanceCountBefore + 1, circumstanceCountAfter, 0);

    Integer atPlaceCountBefore = beforeEventCounts.get(atPlaceEventName);
    Integer atPlaceCountAfter = afterEventCounts.get(atPlaceEventName);
    Assert.assertEquals(atPlaceCountBefore + 1, atPlaceCountAfter, 0);
  }

  /**
   * Tests that Segment Engine Integration is sending spans
   */
  @Test
  public void testSegmentEngineSpans() {
    // Start the integration
    SegmentEngineIntegration.trackUserJourneySpans(appContext, 1);

    // Get events we are expecting to be sent to Braze through Segment
    String spanEventName = SegmentEngineIntegration.ENGINE_SPAN_EVENT_KEY;
    String attachedPlaceEventName = SegmentEngineIntegration.ENGINE_SPAN_ATTACHED_PLACE_EVENT_KEY;
    HashSet<String> events = new HashSet<>();
    events.add(spanEventName);
    events.add(attachedPlaceEventName);

    // Get counts for these events
    Map<String, Integer> beforeEventCounts = getCountsForEvents(events);

    // Push span event to Segment
    SegmentEngineUserJourneyReceiver receiver = new SegmentEngineUserJourneyReceiver();
    receiver.handleUserJourneySpan(appContext, createSpan());

    // Wait for data to be sent to Segment and pushed to Braze
    delay(60);

    // Get counts for events after being tracked
    Map<String, Integer> afterEventCounts = getCountsForEvents(events);

    Integer spanCountBefore = beforeEventCounts.get(spanEventName);
    Integer spanCountAfter = afterEventCounts.get(spanEventName);
    Assert.assertEquals(spanCountBefore + 1, spanCountAfter, 0);

    Integer attachedPlaceCountBefore = beforeEventCounts.get(attachedPlaceEventName);
    Integer attachedPlaceCountAfter = afterEventCounts.get(attachedPlaceEventName);
    Assert.assertEquals(attachedPlaceCountBefore + 1, attachedPlaceCountAfter, 0);
  }

  /**
   * Gets counts for events sent to Braze
   *
   * @param eventNames The names of the events to get counts for
   * @return A map containing the event name and the number of times it appeared
   */
  private Map<String, Integer> getCountsForEvents(Set<String> eventNames) {
    Map<String, Integer> counts = new HashMap<>();
    for (String event : eventNames) {
      counts.put(event, 0);
    }

    OkHttpClient client = new OkHttpClient();
    Request request = brazeApiRequest();
    JSONObject jsonData = new JSONObject();

    try {
      // Get Braze API data
      Response responses = client.newCall(request).execute();
      jsonData = new JSONObject(responses.body().string());
    } catch (Exception e) {
      fail("Failed to get valid response from Braze: " + e.getMessage());
    }

    try {
      // Get test user
      JSONArray users = jsonData.getJSONArray(USERS_KEY);
      JSONObject user = null;
      for (int i = 0; i < users.length(); i++) {
        JSONObject testUser = users.getJSONObject(i);
        if (testUser.getString(ID_KEY).equals(StubConfiguration.TEST_USER_ID)) {
          user = testUser;
          break;
        }
      }
      // Get custom events
      JSONArray customEvents = user.getJSONArray(CUSTOM_EVENTS_KEY);
      // Loop through each custom event
      for (int i = 0; i < customEvents.length(); i++) {
        JSONObject event = customEvents.getJSONObject(i);
        String eventName = event.getString(EVENT_NAME_KEY);
        Integer count = event.getInt(COUNT_KEY);
        // Ensure this event is the one we sent
        if (counts.containsKey(event.getString(EVENT_NAME_KEY))) {
          // Add this event's count
          counts.put(eventName, count);
        }
      }
    } catch (Exception e) {
      Log.i("SegmentEngineTest", "No valid events found, returning zero: " + e.getMessage());
    } // If events don't exist, their counts are 0
    return counts;
  }

  /**
   * Waits for given number of seconds
   *
   * @param seconds Number of seconds to delay
   */
  private void delay(int seconds) {
    try {
      Thread.sleep(seconds * 1000);
    } catch (Exception e) {
      fail("Could not run delay because error: " + e.getMessage());
    }
  }

  /**
   * Creates a unique circumstance response to be tested.
   *
   * @return A unique Circumstance Response object to be tested
   */
  private CircumstanceResponse createCircumstance() {
    String circumstanceId = "test-circumstance";
    String circumstanceExpression = "(at any-factual-place)";
    ArrayList<String> tags = new ArrayList<>();
    tags.add("push-to-segment");
    String circumstanceName = StubConfiguration.CIRCUMSTANCE_NAME;
    FactualCircumstance circumstance = new FactualCircumstance(
        circumstanceId,
        circumstanceExpression,
        tags,
        circumstanceName);
    String placeName = "test-place";
    String factualPlaceId = "test-id-123";
    double distance = 0.0;
    double latitude = 0.0;
    double longitude = 0.0;
    FactualPlace testAtPlace = new FactualPlace(
        placeName,
        factualPlaceId,
        distance,
        latitude,
        longitude,
        PlaceConfidenceThreshold.HIGH);
    List<FactualPlace> testAtPlaces = new ArrayList<>();
    testAtPlaces.add(testAtPlace);
    List<FactualPlace> testNearPlaces = new ArrayList<>();
    Location location = new Location("test-location");

    return new CircumstanceResponse(
        circumstance,
        testAtPlaces,
        testNearPlaces,
        location);
  }

  /**
   * Creates a unique span to be tested. The unique values are the startTimestamp and endTimestamp
   * (creating a unique duration), id, distance, latitude, and longitude.
   *
   * @return A unique UserJourneySpan object to be tested.
   */
  private UserJourneySpan createSpan() {
    Location location = new Location("test-location");
    location.setLatitude(33.8003);
    location.setLongitude(-117.8827);

    JSONArray categoryArray = new JSONArray();
    categoryArray.put(372).put(406);
    FactualPlace place;
    List<FactualPlace> places = new ArrayList<>();

    // Create objects for span
    JSONObject placeObject = new JSONObject();
    try {
      placeObject.put(SegmentEngineUserJourneyReceiver.NAME_KEY,
          "Angel Stadium of Anaheim")
          .put(SegmentEngineUserJourneyReceiver.PLACE_ID_KEY, "test-id")
          .put(SegmentEngineUserJourneyReceiver.CATEGORIES_KEY, categoryArray)
          .put(SegmentEngineUserJourneyReceiver.DISTANCE_KEY, -1)
          .put(SegmentEngineUserJourneyReceiver.LATITUDE_KEY, location.getLatitude())
          .put(SegmentEngineUserJourneyReceiver.LONGITUDE_KEY, location.getLongitude())
          .put(SegmentEngineUserJourneyReceiver.COUNTRY_KEY, "us")
          .put(SegmentEngineUserJourneyReceiver.LOCALITY_KEY, "Anaheim")
          .put(SegmentEngineUserJourneyReceiver.REGION_KEY, "CA")
          .put(SegmentEngineUserJourneyReceiver.POSTCODE_KEY, "92806");
      place = new FactualPlace(placeObject);
      places.add(place);
    } catch (JSONException exception) {
      fail("Could not create placeObject because of exception: " + exception.getMessage());
    }

    FactualPlaceVisit placeVisit = new FactualPlaceVisit(location,
        places,
        null,
        false,
        false);

    return new UserJourneySpan.Builder().setCurrentPlace(placeVisit).build();
  }

  /**
   * @return A Request object for getting data from Braze
   */
  private Request brazeApiRequest() {
    String endpoint = StubConfiguration.BRAZE_REST_ENDPOINT;
    String url = String.format("https://%s/users/export/ids", endpoint);
    JSONObject jsonObject = new JSONObject();
    try {
      jsonObject.put(API_KEY, StubConfiguration.BRAZE_REST_API_KEY)
          .put(EMAIL_ADDRESS, StubConfiguration.TEST_USER_EMAIL);
    } catch (Exception e) {
      fail("Failed to make braze API request: " + e.getMessage());
    }
    String json = jsonObject.toString();

    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    RequestBody requestBody = RequestBody.create(JSON, json);

    return new Request.Builder().url(url).post(requestBody).build();
  }
}

