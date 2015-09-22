package com.mapzen.helpers;

import com.mapzen.valhalla.Router;

import org.junit.Test;

import java.util.Locale;

import static com.mapzen.helpers.DistanceFormatter.format;
import static org.fest.assertions.api.Assertions.assertThat;

public class DistanceFormatterConfigTest {

    @Test
    public void shouldUseDefaultLocale() throws Exception {
        Locale.setDefault(Locale.GERMANY);
        assertThat(format(1000)).isEqualTo("1 km");

        Locale.setDefault(Locale.US);
        assertThat(format(1000)).isEqualTo("0.6 mi");
    }

    @Test
    public void shouldUseGivenLocale() throws Exception {
        Locale.setDefault(Locale.GERMANY);
        assertThat(format(1000, false, Locale.US)).isEqualTo("0.6 mi");

        Locale.setDefault(Locale.US);
        assertThat(format(1000, false, Locale.GERMANY)).isEqualTo("1 km");
    }

    @Test
    public void shouldUseGivenDistanceUnits() throws Exception {
        Locale.setDefault(Locale.GERMANY);
        assertThat(format(1000, false, Router.DistanceUnits.MILES)).isEqualTo("0,6 mi");

        Locale.setDefault(Locale.US);
        assertThat(format(1000, false, Router.DistanceUnits.KILOMETERS)).isEqualTo("1 km");
    }
}
