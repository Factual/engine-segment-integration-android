package com.factual.engine.segment;

import android.location.Location;
import android.text.TextUtils;
import com.factual.engine.api.FactualPlace;
import com.factual.engine.api.mobile_state.FactualPlaceVisit;
import com.factual.engine.api.mobile_state.Geographies;
import java.util.List;

class PlaceVisitData {

  private List<FactualPlace> places = null;
  private String localities = null;
  private String country = null;
  private String postcode = null;
  private String region = null;
  private double ingressLatitude = -1;
  private double ingressLongitude = -1;
  private boolean isHome = false;
  private boolean isWork = false;

  List<FactualPlace> getPlaces() {
    return places;
  }

  String getLocalities() {
    return localities;
  }

  String getCountry() {
    return country;
  }

  String getPostcode() {
    return postcode;
  }

  String getRegion() {
    return region;
  }

  double getIngressLatitude() {
    return ingressLatitude;
  }

  double getIngressLongitude() {
    return ingressLongitude;
  }

  int getNumPlaces() {
    return places == null ? 0 : places.size();
  }

  boolean isHome() {
    return isHome;
  }

  boolean isWork() {
    return isWork;
  }

  PlaceVisitData(FactualPlaceVisit visit) {
    // Current place information
    if (visit != null) {
      places = visit.getAttachedPlaces();
      isHome = visit.isHome();
      isWork = visit.isWork();

      // Ingress information
      Location ingressLocation = visit.getIngressLocation();
      if (ingressLocation != null) {
        ingressLatitude = ingressLocation.getLatitude();
        ingressLongitude = ingressLocation.getLongitude();
      }

      // Geographies information
      Geographies geographies = visit.getGeographies();
      if (geographies != null) {
        country = geographies.getCountry();
        localities = TextUtils.join(", ", geographies.getLocalities());
        postcode = geographies.getPostcode();
        region = geographies.getRegion();
      }
    }
  }
}
