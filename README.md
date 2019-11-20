# Factual / Segment SDK for Android ![Build Status](https://app.bitrise.io/app/a6e1b23f407d8395/status.svg?token=RX7bPL4Gy2jjPw6J_ChZsw)

This repository contains the code for an integration between [Factual's Engine SDK](https://www.factual.com/products/engine/) and [Segment Analytics Track API](https://segment.com/docs/sources/mobile/android/#track). Using this library you can configure Factual's Location Engine SDK to send custom events to Segment to better understand users in the physical world and build personalized experiences to drive user engagement and revenue.

While this is not a Segment [packaged integration](https://segment.com/docs/guides/partners/packaged-integration.md), it's just as simple to use
and it follows Segment's guidelines for tracking location events.

### Integration with Segment UI

see: [engine-segment-integration](https://github.com/Factual/engine-segment-integration)

# installation

The project artifacts are available from Factual's Bintray Maven repository.

```
// repository for the Factual artifacts
repositories {
  maven {
    url "https://factual.bintray.com/maven"
  }
}

...

dependencies {
  compile 'com.factual.engine:segment-engine:2.0.0'
}
```

# Usage

### Requirements

* Configured and started `Engine` client. [see here](http://developer.factual.com/engine/android/)
* Configured `Segment` client. [see here](https://segment.com/docs/sources/mobile/android/)

### Tracking Factual Engine Circumstances

Start tracking Factual Engine circumstances by calling ` SegmentEngineIntegration.pushToSegment()` in the `onCircumstancesMet()` callback of `FactualClientReceiver`.

```java
public class ExampleFactualClientReceiver extends FactualClientReceiver {

    @Override
    public void onCircumstancesMet(List<CircumstanceResponse> responses) {
      /*
      Max number of "engine_at_" events that should be sent per "engine_" + CIRCUMSTANCE_NAME.
      Default is set to 10.
      */
      int maxAtPlaceEvents = 3;

      /*
      Max number of "engine_near_" events that should be sent per "engine_" + CIRCUMSTANCE_NAME.
      Default is set to 20.
      */
      int maxNearPlaceEvents = 5;
      for (CircumstanceResponse response : responses) {
        /* Send circumstance event to Segment */
        SegmentEngineIntegration.pushToSegment(getContext().getApplicationContext(),
              response,
              maxAtPlaceEvents,
              maxNearPlaceEvents);
    }
  }

  ...

}
```

Be sure to assign the Client Receiver when initializing Engine.
```java
public void initializeEngine() {
    FactualEngine.initialize(this,
        Configuration.ENGINE_API_KEY,
        ExampleFactualClientReceiver.class);
}
```

### Tracking Factual Engine User Journey Spans

Start tracking spans by setting the user journey receiver in the `onInitialized()` callback of `FactualClientReceiver`.
```java
@Override
public void onInitialized() {
  try {
    FactualEngine.setUserJourneyReceiver(SegmentEngineUserJourneyReceiver.class);
    FactualEngine.start();
  } catch (FactualException e) {
    Log.e("engine", e.getMessage());
  }
}
```

Then call `SegmentEngineIntegration.trackUserJourneySpans()` in the `onStarted()` method of `FactualClientReceiver`.

```java
public class ExampleFactualClientReceiver extends FactualClientReceiver {
  @Override
  public void onStarted() {
    Log.i("engine", "Engine has started.");
    /*
    Max number of "engine_span_attached_place" events that should be sent per
    "engine_span_occurred". Default is set to 20.
    */
    int maxAttachedPlaceEventsPerSpan = 10;

    /* Start tracking spans */
    SegmentEngineIntegration.trackUserJourneySpans(getContext().getApplicationContext(),
        maxAttachedPlaceEventsPerSpan);
    }

    ...
}
```

Please refer to the [Factual Developer Docs](http://developer.factual.com) for more information about Engine.

## Example App

An example app is included in this repository to demonstrate the usage of this library, see [./example](./example) for documentation and usage instructions.
