package com.mapzen.osrm;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.net.MalformedURLException;
import java.net.URL;

import static org.fest.assertions.api.Assertions.assertThat;

public class DirectionTest {
    @Captor
    @SuppressWarnings("unused")
    ArgumentCaptor<Callback> callback;

    @Captor
    @SuppressWarnings("unused")
    ArgumentCaptor<Route> route;

    @Captor
    @SuppressWarnings("unused")
    ArgumentCaptor<Integer> statusCode;

    Direction.Router validRouter;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        double[] loc = new double[] {1.0, 2.0};
        validRouter = Direction.getRouter().setLocation(loc).setLocation(loc);
    }

    @Test
    public void shouldHaveDefaultEndpoint() throws Exception {
        URL url = validRouter.getRouteUrl();
        assertThat(url.toString()).startsWith("http://osrm.test.mapzen.com");
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

    @Test
    public void shouldGetRoute() throws Exception {
        Callback callback = Mockito.mock(Callback.class);
        Direction.Router router = Direction.getRouter().setLocation(new double[] {
                40.659241, -73.983776
        }).setLocation(new double[] {
                40.671773, -73.981115
        });
        router.setCallback(callback);
        router.fetch();
        router.runner.join();
        Mockito.verify(callback).success(route.capture());
        assertThat(route.getValue().foundRoute()).isTrue();
    }

    @Test
    public void shouldGetNotError() throws Exception {
        Callback callback = Mockito.mock(Callback.class);
        Direction.Router router = Direction.getRouter().setEndpoint("http://snitchmedia.com").setLocation(new double[] {
                40.659241, -73.983776
        }).setLocation(new double[] {
                40.671773, -73.981115
        });
        router.setCallback(callback);
        router.fetch();
        router.runner.join();
        Mockito.verify(callback).failure(statusCode.capture());
        assertThat(statusCode.getValue()).isEqualTo(500);
    }

    @Test
    public void shouldGetNotFound() throws Exception {
        Callback callback = Mockito.mock(Callback.class);
        Direction.Router router = Direction.getRouter().setEndpoint("http://example.com").setLocation(new double[] {
                40.659241, -73.983776
        }).setLocation(new double[] {
                40.671773, -73.981115
        });
        router.setCallback(callback);
        router.fetch();
        router.runner.join();
        Mockito.verify(callback).failure(statusCode.capture());
        assertThat(statusCode.getValue()).isEqualTo(404);
    }
}
