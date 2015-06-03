package com.mapzen.valhalla;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.fest.assertions.api.Assertions.assertThat;

public class RouterTest {
    @Captor
    @SuppressWarnings("unused")
    ArgumentCaptor<Router.Callback> callback;

    @Captor
    @SuppressWarnings("unused")
    ArgumentCaptor<Route> route;

    @Captor
    @SuppressWarnings("unused")
    ArgumentCaptor<Integer> statusCode;

    Router validRouter;

    MockWebServer server;

    @Before
    public void setup() throws Exception {
        server = new MockWebServer();
        MockitoAnnotations.initMocks(this);
        double[] loc = new double[] {1.0, 2.0};
        validRouter = new Router().setLocation(loc).setLocation(loc);
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    public void shouldHaveDefaultEndpoint() throws Exception {
        URL url = validRouter.getRouteUrl();
        assertThat(url.toString()).startsWith("http://valhalla.api.dev.mapzen.com/route");
    }

    @Test
    public void shouldSetEndpoint() throws Exception {
        URL url = validRouter.setEndpoint("http://testing.com").getRouteUrl();
        assertThat(url.toString()).startsWith("http://testing.com");
    }

    @Test
    public void shouldDefaultToCar() throws Exception {
        URL url = validRouter.getRouteUrl();
        assertThat(url.toString()).contains(urlEncode("\"costing\":\"auto\""));
    }

    @Test
    public void shouldSetToCar() throws Exception {
        URL url = validRouter.setDriving().getRouteUrl();
        assertThat(url.toString()).contains(urlEncode("\"costing\":\"auto\""));
    }

    @Test
    public void shouldSetToBike() throws Exception {
        URL url = validRouter.setBiking().getRouteUrl();
        assertThat(url.toString()).contains(urlEncode("\"costing\":\"bicycle\""));
    }

    @Test
    public void shouldSetToFoot() throws Exception {
        URL url = validRouter.setWalking().getRouteUrl();
        assertThat(url.toString()).contains(urlEncode("\"costing\":\"pedestrian\""));
    }

    @Test
    public void shouldClearLocations() throws Exception {
        double[] loc1 = { 1.0, 2.0 };
        double[] loc2 = { 3.0, 4.0 };
        double[] loc3 = { 5.0, 6.0 };
        Router router = new Router()
                .setLocation(loc1)
                .setLocation(loc2);
        router.clearLocations();
        router.setLocation(loc2);
        router.setLocation(loc3);
        URL url = router.getRouteUrl();
        assertThat(url.toString()).doesNotContain(urlEncode("{\"lat\":1.000000,\"lon\":2.000000}"));
        assertThat(url.toString()).contains(urlEncode("{\"lat\":3.000000,\"lon\":4.000000}"));
        assertThat(url.toString()).contains(urlEncode("{\"lat\":5.000000,\"lon\":6.000000}"));
    }

    @Test(expected=MalformedURLException.class)
    public void shouldThrowErrorWhenNoLocation() throws Exception {
        new Router().getRouteUrl();
    }

    @Test(expected=MalformedURLException.class)
    public void shouldThrowErrorWhenOnlyOneLocation() throws Exception {
        new Router().setLocation(new double[]{1.0, 1.0}).getRouteUrl();
    }

    @Test
    public void shouldAddLocations() throws Exception {
        double[] loc1 = { 1.0, 2.0 };
        double[] loc2 = { 3.0, 4.0 };
        URL url = new Router()
                .setLocation(loc1)
                .setLocation(loc2)
                .getRouteUrl();
        assertThat(url.toString()).contains(urlEncode("[{\"lat\":1.000000,\"lon\":2.000000},"
                + "{\"lat\":3.000000,\"lon\":4.000000}]"));
    }

    @Test
    public void shouldGetRoute() throws Exception {
        startServerAndEnqueue(new MockResponse().setBody(getFixture("brooklyn")));
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                String endpoint = server.getUrl("").toString();
                Router.Callback callback = Mockito.mock(Router.Callback.class);
                Router router = new Router()
                        .setEndpoint(endpoint)
                        .setLocation(new double[]{40.659241, -73.983776})
                        .setLocation(new double[]{40.671773, -73.981115});
                router.setCallback(callback);
                router.fetch();
                Mockito.verify(callback).success(route.capture());
                try {
                    assertThat(route.getValue().foundRoute()).isTrue();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Test
    public void shouldGetError() throws Exception {
        startServerAndEnqueue(new MockResponse().setResponseCode(500));
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                Router.Callback callback = Mockito.mock(Router.Callback.class);
                String endpoint = server.getUrl("").toString();
                Router router = new Router()
                        .setEndpoint(endpoint)
                        .setLocation(new double[] { 40.659241, -73.983776 })
                        .setLocation(new double[] { 40.671773, -73.981115 });
                router.setCallback(callback);
                router.fetch();
                Mockito.verify(callback).failure(statusCode.capture());
                assertThat(statusCode.getValue()).isEqualTo(500);
            }
        });
    }

    @Test
    public void shouldGetNotFound() throws Exception {
        startServerAndEnqueue(new MockResponse().setResponseCode(404));
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                Router.Callback callback = Mockito.mock(Router.Callback.class);
                String endpoint = server.getUrl("").toString();
                Router router = new Router()
                        .setEndpoint(endpoint)
                        .setLocation(new double[] { 40.659241, -73.983776 })
                        .setLocation(new double[] { 40.671773, -73.981115 });
                router.setCallback(callback);
                router.fetch();
                Mockito.verify(callback).failure(statusCode.capture());
                assertThat(statusCode.getValue()).isEqualTo(404);
            }
        });
    }

    @Test
    public void shouldGetRouteNotFound() throws Exception {
        startServerAndEnqueue(new MockResponse().setBody(getFixture("unsuccessful")));
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                Router.Callback callback = Mockito.mock(Router.Callback.class);
                String endpoint = server.getUrl("").toString();
                Router router = new Router()
                        .setEndpoint(endpoint)
                        .setLocation(new double[] { 40.659241, -73.983776 })
                        .setLocation(new double[]{40.671773, -73.981115 });
                router.setCallback(callback);
                router.fetch();
                Mockito.verify(callback).failure(statusCode.capture());
                assertThat(statusCode.getValue()).isEqualTo(207);
            }
        });
    }

    @Test
    public void shouldStoreRawRoute() throws Exception {
        startServerAndEnqueue(new MockResponse().setBody(getFixture("brooklyn")));
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                String endpoint = server.getUrl("").toString();
                Router.Callback callback = Mockito.mock(Router.Callback.class);
                Router router = new Router()
                        .setEndpoint(endpoint)
                        .setLocation(new double[] { 40.659241, -73.983776 })
                        .setLocation(new double[] { 40.671773, -73.981115 });
                router.setCallback(callback);
                router.fetch();
                Mockito.verify(callback).success(route.capture());
                assertThat(route.getValue().getRawRoute().toString())
                        .isEqualTo(getFixture("brooklyn"));
            }
        });
    }

    private void startServerAndEnqueue(MockResponse response) throws Exception {
        server.enqueue(response);
        server.play();
    }

    private static String urlEncode(String raw) throws UnsupportedEncodingException {
        return URLEncoder.encode(raw, "utf-8");
    }

    public static String getFixture(String name) {
        String basedir = System.getProperty("user.dir");
        File file = new File(basedir + "/src/test/fixtures/" + name + ".route");
        String fixture = "";
        try {
            fixture = Files.toString(file, Charsets.UTF_8);
        } catch (Exception e) {
            fixture = "not found";
        }
        return fixture;
    }
}
