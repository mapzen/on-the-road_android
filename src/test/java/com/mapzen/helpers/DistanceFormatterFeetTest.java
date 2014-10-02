package com.mapzen.helpers;

import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static com.mapzen.helpers.DistanceFormatter.METERS_IN_ONE_MILE;
import static com.mapzen.helpers.DistanceFormatter.format;
import static org.fest.assertions.api.Assertions.assertThat;

public class DistanceFormatterFeetTest {

    @Before
    public void setup() throws Exception {
        Locale.setDefault(Locale.US);
    }

    @Test
    public void distanceTenMiles_shouldReturnTenMiles() throws Exception {
        assertThat(format((int) METERS_IN_ONE_MILE * 10)).isEqualTo("10 mi");
    }

    @Test
    public void distanceOneMile_shouldReturnOneMile() throws Exception {
        assertThat(format((int) METERS_IN_ONE_MILE)).isEqualTo("1 mi");
    }

    @Test
    public void distanceTenthOfMile_shouldReturnOneTenthOfAMile() throws Exception {
        assertThat(format((int) METERS_IN_ONE_MILE / 10 + 1)).isEqualTo("0.1 mi");
    }

    @Test
    public void distanceLessThanTenthOfMile_shouldReturnFeetRoundedDown() throws Exception {
        assertThat(format((int) METERS_IN_ONE_MILE / 10 - 2)).isEqualTo("510 ft");
    }

    @Test
    public void distanceLessThanTenFeet_shouldReturnActualNumberOfFeet() throws Exception {
        assertThat(format(1)).isEqualTo("3 ft");
        assertThat(format(2)).isEqualTo("6 ft");
        assertThat(format(3)).isEqualTo("9 ft");
    }

    @Test
    public void distanceLessThanTenFeet_shouldReturnNowForRealTimeFormat() throws Exception {
        assertThat(format(1, true)).isEqualTo("now");
        assertThat(format(2, true)).isEqualTo("now");
        assertThat(format(3, true)).isEqualTo("now");
    }

    @Test
    public void distanceZero_shouldReturnEmptyString() throws Exception {
        assertThat(format(0)).isEmpty();
    }
}
