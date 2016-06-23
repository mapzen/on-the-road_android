package com.mapzen.valhalla;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static com.mapzen.TestUtils.getTransitInfoFixture;
import static org.fest.assertions.api.Assertions.assertThat;

public class TransitInfoTest {

  TransitInfo transitInfo;

  @Before
  public void setup() {
    JSONObject json = null;
    try {
      json = new JSONObject(getTransitInfoFixture("json_valhalla"));
    } catch (JSONException e) {
      e.printStackTrace();
    }
    transitInfo = new TransitInfo(json);
  }

  @Test
  public void hasTransitStops() {
    assertThat(transitInfo.getTransitStops()).hasSize(1);
  }

  @Test
  public void hasHeadSign() {
    assertThat(transitInfo.getHeadsign()).isEqualTo("CONEY ISLAND - STILLWELL AV");
  }

  @Test
  public void hasLongName() {
    assertThat(transitInfo.getLongName()).isEqualTo("Queens Blvd Express/ 6 Av Local");
  }

  @Test
  public void hasOperatorUrl() {
    assertThat(transitInfo.getOperatorUrl()).isEqualTo("http://web.mta.info/");
  }

  @Test
  public void hasOnestopId() {
    assertThat(transitInfo.getOnestopId()).isEqualTo("r-dr5r-f");
  }

  @Test
  public void hasShortName() {
    assertThat(transitInfo.getShortName()).isEqualTo("F");
  }

  @Test
  public void hasColor() {
    assertThat(transitInfo.getColor()).isEqualTo(16737049);
  }

  @Test
  public void hasDescription() {
    assertThat(transitInfo.getDescription()).isEqualTo("Trains operate at all times between");
  }

  @Test
  public void hasTextColor() {
    assertThat(transitInfo.getTextColor()).isEqualTo(0);
  }

  @Test
  public void hasOperatorOnestopId() {
    assertThat(transitInfo.getOperatorOnestopId()).isEqualTo("o-dr5r-nyct");
  }

  @Test
  public void hasOperatorName() {
    assertThat(transitInfo.getOperatorName()).isEqualTo("MTA");
  }

}
