package com.mapzen.valhalla;

import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Callback;
import retrofit2.Response;

public class TestHttpHandler extends HttpHandler {

  TestRoutingService testService;

  public TestHttpHandler(String endpoint, HttpLoggingInterceptor.Level logLevel) {
    configure(endpoint, logLevel);
  }

  protected void configure(String endpoint, HttpLoggingInterceptor.Level logLevel) {
    super.configure(endpoint, logLevel);
    testService = adapter.create(TestRoutingService.class);
  }

  @Override public void requestRoute(String routeJson, Callback callback) {
    String route = testService.getRoute(routeJson);
    callback.onResponse(null, Response.success(route));
  }
}
