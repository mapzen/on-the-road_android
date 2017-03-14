package com.mapzen.valhalla;

import org.junit.Test;

public class JSONTest {
  @Test
  public void shouldNotThrowIfHeadingEqualsZero() throws Exception {
    double lat = 0.0;
    double lon = 0.0;
    int heading = 0;
    new JSON.Location(lat, lon, heading);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowIfHeadingLessThanZero() throws Exception {
    double lat = 0.0;
    double lon = 0.0;
    int heading = -1;
    new JSON.Location(lat, lon, heading);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowIfHeadingEquals360() throws Exception {
    double lat = 0.0;
    double lon = 0.0;
    int heading = 360;
    new JSON.Location(lat, lon, heading);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowIfHeadingIsGreaterThan360() throws Exception {
    double lat = 0.0;
    double lon = 0.0;
    int heading = 361;
    new JSON.Location(lat, lon, heading);
  }
}
