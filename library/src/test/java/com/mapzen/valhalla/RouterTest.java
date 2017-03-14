package com.mapzen.valhalla;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.net.MalformedURLException;

import static com.mapzen.TestUtils.getRouteFixture;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import static org.fest.assertions.api.Assertions.assertThat;

public class RouterTest {
    @Captor ArgumentCaptor<Route> route;
    @Captor ArgumentCaptor<Integer> statusCode;

    Router router;
    MockWebServer server;
    TestHttpHandler httpHandler;

    @Before
    public void setup() throws Exception {
        server = new MockWebServer();
        server.start();
        MockitoAnnotations.initMocks(this);
        double[] loc = new double[] {1.0, 2.0};
        router = new ValhallaRouter().setLocation(loc).setLocation(loc);
        String endpoint = server.url("").toString();
        httpHandler = new TestHttpHandler(endpoint, HttpLoggingInterceptor.Level.NONE);
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    public void shouldDefaultToEnUs() throws Exception {
        assertThat(router.getJSONRequest().directionsOptions.language).contains("en-US");
    }

    @Test
    public void shouldSetToCsCs() throws Exception {
        router.setLanguage(Router.Language.CS_CZ);
        assertThat(router.getJSONRequest().directionsOptions.language).contains("cs-CZ");
    }

    @Test
    public void shouldSetToDeDe() throws Exception {
        router.setLanguage(Router.Language.DE_DE);
        assertThat(router.getJSONRequest().directionsOptions.language).contains("de-DE");
    }

    @Test
    public void shouldSetToEnUs() throws Exception {
        router.setLanguage(Router.Language.EN_US);
        assertThat(router.getJSONRequest().directionsOptions.language).contains("en-US");
    }

    @Test
    public void shouldSetToEnUsPirate() throws Exception {
        router.setLanguage(Router.Language.PIRATE);
        assertThat(router.getJSONRequest().directionsOptions.language).contains("en-US-x-pirate");
    }

    @Test
    public void shouldSetToEsEs() throws Exception {
        router.setLanguage(Router.Language.ES_ES);
        assertThat(router.getJSONRequest().directionsOptions.language).contains("es-ES");
    }

    @Test
    public void shouldSetToFrFr() throws Exception {
        router.setLanguage(Router.Language.FR_FR);
        assertThat(router.getJSONRequest().directionsOptions.language).contains("fr-FR");
    }

    @Test
    public void shouldSetToItIt() throws Exception {
        router.setLanguage(Router.Language.IT_IT);
        assertThat(router.getJSONRequest().directionsOptions.language).contains("it-IT");
    }

    @Test
    public void shouldSetToHiIn() throws Exception {
        router.setLanguage(Router.Language.HI_IN);
        assertThat(router.getJSONRequest().directionsOptions.language).contains("hi-IN");
    }

    @Test
    public void shouldDefaultToCar() throws Exception {
        assertThat(router.getJSONRequest().costing).contains("auto");
    }

    @Test
    public void shouldSetToCar() throws Exception {
        router.setDriving();
        assertThat(router.getJSONRequest().costing).contains("auto");
    }

    @Test
    public void shouldSetToBike() throws Exception {
        router.setBiking();
        assertThat(router.getJSONRequest().costing).contains("bicycle");
    }

    @Test
    public void shouldSetToFoot() throws Exception {
        router.setWalking();
        assertThat(router.getJSONRequest().costing).contains("pedestrian");
    }

    @Test
    public void shouldSetToMultimodal() throws Exception {
        router.setMultimodal();
        assertThat(router.getJSONRequest().costing).contains("multimodal");
    }

    @Test
    public void shouldClearLocations() throws Exception {
        double[] loc1 = { 1.0, 2.0 };
        double[] loc2 = { 3.0, 4.0 };
        double[] loc3 = { 5.0, 6.0 };
        Router router = new ValhallaRouter()
                .setLocation(loc1)
                .setLocation(loc2);
        router.clearLocations();
        router.setLocation(loc2);
        router.setLocation(loc3);
        JSON json = router.getJSONRequest();
        assertThat(json.locations.get(0).lat).isNotEqualTo(1.0);
        assertThat(json.locations.get(0).lat).isEqualTo(3.0);
        assertThat(json.locations.get(1).lat).isEqualTo(5.0);
    }

    @Test(expected=MalformedURLException.class)
    public void shouldThrowErrorWhenNoLocation() throws Exception {
        new ValhallaRouter().getJSONRequest();
    }

    @Test(expected=MalformedURLException.class)
    public void shouldThrowErrorWhenOnlyOneLocation() throws Exception {
        new ValhallaRouter().setLocation(new double[]{1.0, 1.0}).getJSONRequest();
    }

    @Test
    public void shouldAddLocations() throws Exception {
        double[] loc1 = { 1.0, 2.0 };
        double[] loc2 = { 3.0, 4.0 };
        JSON json = new ValhallaRouter()
                .setLocation(loc1)
                .setLocation(loc2)
                .getJSONRequest();
        assertThat(json.locations.get(0).lat).isEqualTo(1.0);
        assertThat(json.locations.get(0).lon).isEqualTo(2.0);
        assertThat(json.locations.get(1).lat).isEqualTo(3.0);
        assertThat(json.locations.get(1).lon).isEqualTo(4.0);
    }

    @Test
    public void shouldAddWaypoints() throws Exception {
        double[] loc1 = { 1.0, 2.0 };
        double[] loc2 = { 3.0, 4.0 };
        double[] loc3 = { 5.0, 6.0 };
        double[] loc4 = { 7.0, 8.0 };
        JSON json = new ValhallaRouter()
            .setLocation(loc1)
            .setLocation(loc2)
            .setLocation(loc3)
            .setLocation(loc4)
            .getJSONRequest();
        assertThat(json.locations.get(0).lat).isEqualTo(1.0);
        assertThat(json.locations.get(0).lon).isEqualTo(2.0);
        assertThat(json.locations.get(1).lat).isEqualTo(3.0);
        assertThat(json.locations.get(1).lon).isEqualTo(4.0);
        assertThat(json.locations.get(2).lat).isEqualTo(5.0);
        assertThat(json.locations.get(2).lon).isEqualTo(6.0);
        assertThat(json.locations.get(3).lat).isEqualTo(7.0);
        assertThat(json.locations.get(3).lon).isEqualTo(8.0);
    }

    @Test
    public void shouldGetRoute() throws Exception {
        final RouteCallback callback = Mockito.mock(RouteCallback.class);
        String routeJson = getRouteFixture("brooklyn_valhalla");
        startServerAndEnqueue(new MockResponse().setBody(routeJson));
        Router router = new ValhallaRouter()
            .setHttpHandler(httpHandler)
            .setLocation(new double[] { 40.659241, -73.983776 })
            .setLocation(new double[] { 40.671773, -73.981115 });
        router.setCallback(callback);
        ((ValhallaRouter) router).run();
        Mockito.verify(callback).success(route.capture());
        assertThat(route.getValue().foundRoute()).isTrue();
    }

    @Test
    public void shouldGetError() throws Exception {
        startServerAndEnqueue(new MockResponse().setResponseCode(500));
        RouteCallback callback = Mockito.mock(RouteCallback.class);
        Router router = new ValhallaRouter()
            .setHttpHandler(httpHandler)
            .setLocation(new double[]{40.659241, -73.983776})
            .setLocation(new double[]{40.671773, -73.981115});
        router.setCallback(callback);
        ((ValhallaRouter) router).run();
        Mockito.verify(callback).failure(statusCode.capture());
        assertThat(statusCode.getValue()).isEqualTo(500);
    }

    @Test
    public void shouldGetNotFound() throws Exception {
        startServerAndEnqueue(new MockResponse().setResponseCode(404));
        RouteCallback callback = Mockito.mock(RouteCallback.class);
        Router router = new ValhallaRouter()
            .setHttpHandler(httpHandler)
            .setLocation(new double[]{40.659241, -73.983776})
            .setLocation(new double[]{40.671773, -73.981115});
        router.setCallback(callback);
        ((ValhallaRouter) router).run();
        Mockito.verify(callback).failure(statusCode.capture());
        assertThat(statusCode.getValue()).isEqualTo(404);
    }

    @Test
    public void shouldGetRouteNotFound() throws Exception {
        startServerAndEnqueue(new MockResponse().setBody(getRouteFixture("unsuccessful")).setResponseCode(400));
        RouteCallback callback = Mockito.mock(RouteCallback.class);
        Router router = new ValhallaRouter()
            .setHttpHandler(httpHandler)
            .setLocation(new double[] { 40.659241, -73.983776 })
            .setLocation(new double[] { 40.671773, -73.981115 });
        router.setCallback(callback);
        ((ValhallaRouter) router).run();
        Mockito.verify(callback).failure(statusCode.capture());
        assertThat(statusCode.getValue()).isEqualTo(400);
    }

    @Test
    public void shouldStoreRawRoute() throws Exception {
        String routeJson = getRouteFixture("brooklyn_valhalla");
        startServerAndEnqueue(new MockResponse().setBody(routeJson));
        RouteCallback callback = Mockito.mock(RouteCallback.class);
        Router router = new ValhallaRouter()
            .setHttpHandler(httpHandler)
            .setLocation(new double[] { 40.659241, -73.983776 })
            .setLocation(new double[] { 40.671773, -73.981115 });
        router.setCallback(callback);
        ((ValhallaRouter) router).run();
        Mockito.verify(callback).success(route.capture());
        assertThat(route.getValue().getRawRoute().toString())
            .isEqualTo(new JSONObject(routeJson).toString());
    }

    @Test
    public void setDistanceUnits_shouldAppendUnitsToJson() throws Exception {
        router.setDistanceUnits(Router.DistanceUnits.MILES);
        assertThat(new Gson().toJson(router.getJSONRequest()))
                .contains("\"directions_options\":{\"units\":\"miles\",\"language\":\"en-US\"}");

        router.setDistanceUnits(Router.DistanceUnits.KILOMETERS);
        assertThat(new Gson().toJson(router.getJSONRequest()))
                .contains("\"directions_options\":{\"units\":\"kilometers\",\"language\":\"en-US\"}");
    }

    @Test
    public void setLocation_shouldAppendName() throws Exception {
        double[] loc = new double[] {1.0, 2.0};
        router = new ValhallaRouter().setLocation(loc)
                .setLocation(loc, "Acme", null, null, null);
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(JSON.Location.class, new LocationSerializer())
            .create();
        assertThat(gson.toJson(router.getJSONRequest()))
                .contains("{\"lat\":1.0,\"lon\":2.0,\"name\":\"Acme\"}");
    }

    @Test
    public void setLocation_shouldNotIncludeNameParamIfNotSet() throws Exception {
        assertThat(new Gson().toJson(router.getJSONRequest()))
                .doesNotContain("\"name\"");
    }

    @Test
    public void setLocation_shouldAppendStreetAddress() throws Exception {
        double[] loc = new double[] {1.0, 2.0};
        router = new ValhallaRouter()
                .setLocation(loc).setLocation(loc, "Acme", "North Main Street", "Doylestown", "PA");
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(JSON.Location.class, new LocationSerializer())
            .create();
        assertThat(gson.toJson(router.getJSONRequest()))
                .contains("{\"lat\":1.0,\"lon\":2.0,"
                        + "\"name\":\"Acme\","
                        + "\"street\":\"North Main Street\","
                        + "\"city\":\"Doylestown\","
                        + "\"state\":\"PA\"}");
    }

    @Test
    public void setLocation_shouldIncludeHeading() throws Exception {
        double[] loc = new double[] {1.0, 2.0};
        router = new ValhallaRouter().setLocation(loc, 180).setLocation(loc);
        assertThat(new Gson().toJson(router.getJSONRequest()))
                .contains("{\"lat\":1.0,\"lon\":2.0,\"heading\":180.0}");
    }

    @Test
    public void setEndpoint_shouldUpdateBaseRequestUrl() throws Exception {
        startServerAndEnqueue(new MockResponse());
        String endpoint = server.url("").toString();
        TestHttpHandler httpHandler = new TestHttpHandler(endpoint,
            HttpLoggingInterceptor.Level.NONE);
        Router router = new ValhallaRouter()
                .setHttpHandler(httpHandler)
                .setLocation(new double[] { 40.659241, -73.983776 })
                .setLocation(new double[] { 40.671773, -73.981115 });
        ((ValhallaRouter) router).run();
        assertThat(httpHandler.route.raw().request().url().toString()).contains(endpoint);
    }

    private void startServerAndEnqueue(MockResponse response) throws Exception {
        server.enqueue(response);
    }
}
