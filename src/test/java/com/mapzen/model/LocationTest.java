package com.mapzen.model;

import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class LocationTest {

  Location location;

  @Before
  public void setup() {
    location = new Location();
  }

  @Test
  public void setLatitude_shouldSetLatitude() {
    assertThat(location.getLatitude()).isEqualTo(0.0);
    location.setLatitude(8);
    assertThat(location.getLatitude()).isEqualTo(8);
  }

  @Test
  public void setLongitude_shouldSetLatitude() {
    assertThat(location.getLongitude()).isEqualTo(0.0);
    location.setLongitude(8);
    assertThat(location.getLongitude()).isEqualTo(8);
  }

  @Test
  public void setBearing_shouldSetBearing() {
    assertThat(location.getBearing()).isEqualTo(0.0f);
    location.setBearing(8);
    assertThat(location.getBearing()).isEqualTo(8);
  }

  @Test
  public void setBearing_shouldSetHasBearing() {
    assertThat(location.hasBearing()).isFalse();
    location.setBearing(8);
    assertThat(location.hasBearing()).isTrue();
  }

  @Test
  public void distanceTo_shouldReturnThreeHundredThousand() {
    Location l1 = new Location();
    l1.setLatitude(37.5);
    l1.setLongitude(-74.0);
    l1.setBearing(1.5f);

    Location l2 = new Location();
    l2.setLatitude(40.5);
    l2.setLongitude(-75.0);
    l2.setBearing(0.5f);

    float distanceTo = l1.distanceTo(l2);
    assertThat(distanceTo).isEqualTo(344120.63f);
  }

  @Test
  public void bearingTo_shouldReturnNegativeFourteen() {
    Location l1 = new Location();
    l1.setLatitude(37.5);
    l1.setLongitude(-74.0);
    l1.setBearing(1.5f);

    Location l2 = new Location();
    l2.setLatitude(40.5);
    l2.setLongitude(-75.0);
    l2.setBearing(0.5f);

    float bearingTo = l1.bearingTo(l2);
    assertThat(bearingTo).isEqualTo(-14.266873f);
  }

}
