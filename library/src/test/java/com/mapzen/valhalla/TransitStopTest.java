package com.mapzen.valhalla;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static com.mapzen.TestUtils.getTransitStopFixture;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Created by sarahlensing on 6/23/16.
 */
public class TransitStopTest {

  TransitStop transitStop;

  @Before
  public void setup() {
    JSONObject object = null;
    try {
      object = new JSONObject(getTransitStopFixture("json_valhalla"));
    } catch (JSONException e) {
      e.printStackTrace();
    }
    transitStop = new TransitStop(object);
  }

  @Test
  public void hasDepartureDate() {
    assertThat(transitStop.getDepartureDateTime()).isEqualTo("2016-06-23T13:39");
  }

  @Test
  public void hasName() {
    assertThat(transitStop.getName()).isEqualTo("23 St");
  }

  @Test
  public void hasOnestopId() {
    assertThat(transitStop.getOnestopId()).isEqualTo("s-dr5ru3006q-23st<d18s");
  }

  @Test
  public void hasType() {
    assertThat(transitStop.getType()).isEqualTo("station");
  }

  @Test
  public void hasArrivalDateTime() {
    assertThat(transitStop.getArrivalDateTime()).isEqualTo("2016-06-23T13:40");
  }

  @Test
  public void hasIsParentStop() {
    assertThat(transitStop.getIsParentStop()).isTrue();
  }

  @Test
  public void hasAssumedSchedule() {
    assertThat(transitStop.getAssumedSchedule()).isTrue();
  }

  @Test
  public void hasLat() {
    assertThat(transitStop.getLat()).isEqualTo(40.741302);
  }

  @Test
  public void hasLon() {
    assertThat(transitStop.getLon()).isEqualTo(-73.989342);
  }

}
