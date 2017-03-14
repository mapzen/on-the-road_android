package com.mapzen.valhalla;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.fest.assertions.api.Assertions.assertThat;

public class LocationSerializerTest {

  private LocationSerializer locationSerializer;
  private JSON.Location location;

  @Before public void setUp() throws Exception {
    double lat = 0.0;
    double lon = 0.0;
    int heading = 0;
    location = new JSON.Location(lat, lon, heading);
    locationSerializer = new LocationSerializer();
  }

  @Test public void shouldIncludeHeading() throws Exception {
    assertThat(locationSerializer.serialize(location, JSON.Location.class, null).toString())
        .contains("\"heading\":0");
  }

  @Test public void shouldExcludeHeadingLessThanZero() throws Exception {
    location.heading = -1;
    assertThat(locationSerializer.serialize(location, JSON.Location.class, null).toString())
        .doesNotContain("\"heading\":-1");
  }

  @Test public void shouldExcludeHeadingEqualTo360() throws Exception {
    location.heading = 360;
    assertThat(locationSerializer.serialize(location, JSON.Location.class, null).toString())
        .doesNotContain("\"heading\":360");
  }

  @Test public void shouldExcludeHeadingGreaterThan360() throws Exception {
    location.heading = 361;
    assertThat(locationSerializer.serialize(location, JSON.Location.class, null).toString())
        .doesNotContain("\"heading\":361");
  }

  @Test public void shouldExcludeOnlyHeadingsOutOfRange() throws Exception {
    JSON.Location location1 = new JSON.Location(1d, 2d, 0);
    JSON.Location location2 = new JSON.Location(3d, 4d);
    ArrayList<JSON.Location> locations = new ArrayList<>();
    locations.add(location1);
    locations.add(location2);
    JSON json = new JSON();
    json.locations = locations;

    Gson gson = new GsonBuilder()
        .registerTypeAdapter(JSON.Location.class, new LocationSerializer())
        .create();
    String result = gson.toJson(json);
    assertThat(result).contains("\"lat\":1.0,\"lon\":2.0,\"heading\":0");
    assertThat(result).doesNotContain("\"lat\":3.0,\"lon\":4.0,\"heading\":-1");
  }
}
