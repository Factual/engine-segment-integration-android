# Description

This repository contains the code for an integration between Segment Analytics [Track API](https://segment.com/docs/sources/mobile/ios/#track)
and Factual [Engine SDK for Android](http://developer.factual.com/engine/android).

While this is not a Segment [packaged integration](https://segment.com/docs/guides/partners/packaged-integration.md), it's just as simple to use
and it follows Segment's guidelines for tracking location events. 

# Requirements

* an Engine API key
  * please log in to Factual.com or contact Factual for your key
* an implementation of a `FactualClientReceiver` or `FactualClientListener`
  * see our [example](http://developer.factual.com/engine/android/#example-implementation-code) for reference
  * we highly recommend an implemenation of `FactualClientReceiver` in order to capture background events
* a Segment write key
  * see [here](https://segment.com/docs/guides/setup/how-do-i-find-my-write-key/)
* a initialized `Analytics` client
  * as described [here](https://segment.com/docs/sources/mobile/android/#step-2-initialize-the-client)

# Installation

## Gradle

```
// repository for the Factual artifacts
repositories {
  maven {
    url "https://factual.bintray.com/maven"
  }
}

...

dependencies {
  compile 'com.segment.analytics.android:analytics:4.+'
  compile 'com.factual:engine-sdk:5.3.0'
  compile 'com.factual.engine.analytics:analytics-engine:1.0.0'
}
```

## Manual installation

Download the library from [Bintray](https://factual.bintray.com/maven) and add it to your project.

**Note**: You must have Engine SDK already added to your project in order to use the library.

# Quickstart

Import the utility class `com.factual.engine.analytics.AnalyticsEngineUtil` in your implementation of `FactualClientReceiver` or `FactualClientListener`, and inside of the `onStarted` method add the following line of code:

```
AnalyticsEngineUtil.trackAllPlaceVisits();
```

**That's it!**

Engine will invoke Segment's Track API for every "Place Entered". You can verify that the app is calling the Track API by using the Segment source debugger.

# How to use

We bundle classes that make it easy to call the Track API upon Circumstance detection.

Import the following to use them:

```
import com.factual.engine.analytics.*;
```

## Use within your own custom Engine client receiver/listener

In your implementation of `FactualClientReceiver` or `FactualClientListener` (see [Requirements](#requirements)), simply add one line of code to the `onStarted` method.

### To track the entire user journey

```
AnalyticsEngineUtil.trackAllPlaceVisits();
```

This method call configures Engine in two ways:

* it registers an action with Engine that calls Segment's Track API
* it registers a Circumstance mapped to this action that detects whenever you are **at** any one
  of the 100+ millon places in Factual's [Global Places](http://www.factual.com/products/global) dataset, at any time

### To track user defined Circumstances only

First register the bundled tracking action handler with Engine:

```
AnalyticsEngineUtil.addDefaultActionReceiver();
```

Then for programmatically created Circumstances, use `AnalyticsEngineActionReceiver.ACTION_ID` as the action id to map your Circumstance to. For
user defined Circumstances created in the Engine Garage, map them to a new action id called `factual-segment-default-action-id`.

## Roll your own location tracking strategy

The bundled utility class [AnalyticsEngineUtil](https://github.com/Factual/segment-analytics-factual-engine-android/blob/master/library/src/main/java/com/factual/engine/analytics/AnalyticsEngineUtil.java)
provides other methods  you can invoke from your own custom action handler(s) to fine tune which location events you wish to track.

## Reference implementation of FactualClientReceiver

Easily test this library by starting Engine with the bundled [AnalyticsEngineClientReceiver](https://github.com/Factual/segment-analytics-factual-engine-android/blob/master/library/src/main/java/com/factual/engine/analytics/AnalyticsEngineClientReceiver.java),
which is a reference implemenation of `FactualClientReceiver`.

**Note**: We strongly encourage you to use your own implementation of `FactualClientReceiver` in Production to have full control over Engine behavior in addition to invoking Segment's Track API.

# License

```
WWWWWW||WWWWWW
 W W W||W W W
      ||
    ( OO )__________
     /  |           \
    /o o|    MIT     \
    \___/||_||__||_|| *
         || ||  || ||
        _||_|| _||_||
       (__|__|(__|__|

The MIT License (MIT)

Copyright (c) 2018 Factual, Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```