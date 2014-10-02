package com.mapzen.helpers;

import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static com.mapzen.helpers.DistanceFormatter.format;
import static org.fest.assertions.api.Assertions.assertThat;

public class DistanceFormatterMetersTest {

    @Before
    public void setup() throws Exception {
        Locale.setDefault(Locale.GERMANY);
    }

    @Test
    public void distanceTenKilometers_shouldReturnTenKilometers() throws Exception {
        assertThat(format(10000)).isEqualTo("10 km");
    }

    @Test
    public void distanceIncrements_shouldHaveHundredsIncrements() throws Exception {
        assertThat(format(10200)).isEqualTo("10,2 km");
    }

    @Test
    public void distanceOneKilometer_shouldReturnOneKilometer() throws Exception {
        assertThat(format(1000)).isEqualTo("1 km");
    }

    @Test
    public void distanceMeters_shouldReturnMeters() throws Exception {
        assertThat(format(999)).isEqualTo("999 m");
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
