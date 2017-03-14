package com.mapzen.valhalla;

import org.junit.Test;

public class JSONTest {
  @Test
  public void shouldNotThrowIfHeadingEqualsZero() throws Exception {
    double lat = 0.0;
    double lon = 0.0;
    double heading = 0.0;
    new JSON.Location(lat, lon, heading);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowIfHeadingLessThanZero() throws Exception {
    double lat = 0.0;
    double lon = 0.0;
    double heading = -1.0;
    new JSON.Location(lat, lon, heading);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowIfHeadingEquals360() throws Exception {
    double lat = 0.0;
    double lon = 0.0;
    double heading = 360.0;
    new JSON.Location(lat, lon, heading);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowIfHeadingIsGreaterThan360() throws Exception {
    double lat = 0.0;
    double lon = 0.0;
    double heading = 361.0;
    new JSON.Location(lat, lon, heading);
  }
}
