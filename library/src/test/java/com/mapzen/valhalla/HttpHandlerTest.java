package com.mapzen.valhalla;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;

import java.io.IOException;

import static com.mapzen.TestUtils.getRouteFixture;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import static org.fest.assertions.api.Assertions.assertThat;

public class HttpHandlerTest {

  String endpoint = "https://valhalla.mapzen.com/";
  HttpHandler httpHandler;

  @Before public void setup() throws IOException {
    httpHandler = new HttpHandler(endpoint, HttpLoggingInterceptor.Level.NONE);
  }

  @Test public void shouldHaveEndpoint() {
    final String endpoint =
        (String) Whitebox.getInternalState(httpHandler, "endpoint");
    assertThat(endpoint).startsWith("https://valhalla.mapzen.com/");
  }

  @Test public void shouldHaveLogLevel() {
    final HttpLoggingInterceptor.Level logLevel =
        (HttpLoggingInterceptor.Level) Whitebox.getInternalState(httpHandler, "logLevel");
    assertThat(logLevel).isEqualTo(HttpLoggingInterceptor.Level.NONE);
  }

  @Test public void shouldAddHeaders() throws IOException {
    final MockWebServer server = new MockWebServer();
    server.start();
    server.enqueue(new MockResponse().setBody(getRouteFixture("brooklyn_valhalla")));
    String endpoint = server.url("").toString();
    TestHttpHandler httpHandler = new TestHttpHandler(endpoint, HttpLoggingInterceptor.Level.NONE);
    Router router = new ValhallaRouter()
        .setHttpHandler(httpHandler)
        .setLocation(new double[] { 40.659241, -73.983776 })
        .setLocation(new double[] { 40.671773, -73.981115 });
    RouteCallback callback = Mockito.mock(RouteCallback.class);
    router.setCallback(callback);
    ((ValhallaRouter) router).run();
    assertThat(httpHandler.headersAdded).isTrue();
  }
}
