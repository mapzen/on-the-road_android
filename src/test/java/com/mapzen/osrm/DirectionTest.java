package com.mapzen.osrm;

import com.squareup.okhttp.OkHttpClient;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

import java.net.MalformedURLException;
import java.net.URL;

import retrofit.Callback;

import static org.fest.assertions.api.Assertions.assertThat;

public class DirectionTest {
    @Captor
    @SuppressWarnings("unused")
    ArgumentCaptor<Callback> callback;

    Direction.Router validRouter;

    @Before
    public void setup() throws Exception {
        double[] loc = new double[] {1.0, 2.0};
        validRouter = Direction.getRouter().setLocation(loc).setLocation(loc);
    }

    @Test
    public void shouldHaveDefaultEndpoint() throws Exception {
        URL url = validRouter.getRouteUrl();
        assertThat(url.toString()).startsWith("http://example.com");
    }

    @Test
    public void shouldSetEndpoint() throws Exception {
        URL url = validRouter.setEndpoint("http://testing.com").getRouteUrl();
        assertThat(url.toString()).startsWith("http://testing.com");
    }

    @Test
    public void shouldHaveDefaultZoom() throws Exception {
        URL url = validRouter.getRouteUrl();
        assertThat(url.toString()).contains("z=17");
    }

    @Test
    public void shouldSetZoom() throws Exception {
        URL url = validRouter.setZoomLevel(11).getRouteUrl();
        assertThat(url.toString()).contains("z=11");
    }

    @Test
    public void shouldDefaultToCar() throws Exception {
        URL url = validRouter.getRouteUrl();
        assertThat(url.toString()).contains("car/viaroute");
    }

    @Test
    public void shouldSetToCar() throws Exception {
        URL url = validRouter.setDriving().getRouteUrl();
        assertThat(url.toString()).contains("car/viaroute");
    }

    @Test
    public void shouldSetToBike() throws Exception {
        URL url = validRouter.setBiking().getRouteUrl();
        assertThat(url.toString()).contains("bike/viaroute");
    }

    @Test
    public void shouldSetToFoot() throws Exception {
        URL url = validRouter.setWalking().getRouteUrl();
        assertThat(url.toString()).contains("foot/viaroute");
    }

    @Test(expected=MalformedURLException.class)
    public void shouldThrowErrorWhenNoLocation() throws Exception {
        Direction.getRouter().getRouteUrl();
    }

    @Test(expected=MalformedURLException.class)
    public void shouldThrowErrorWhenOnlyOneLocation() throws Exception {
        Direction.getRouter().setLocation(new double[] {1.0, 1.0}).getRouteUrl();
    }

    @Test
    public void shouldAddLocations() throws Exception {
        double[] loc1 = { 1.0, 2.0 };
        double[] loc2 = { 3.0, 4.0 };
        double[] loc3 = { 5.0, 6.0 };
        URL url = Direction.getRouter()
                .setLocation(loc1)
                .setLocation(loc2)
                .setLocation(loc3).getRouteUrl();
        assertThat(url.toString()).contains("&loc=1.0,2.0&loc=3.0,4.0&loc=5.0,6.0");
    }

}
