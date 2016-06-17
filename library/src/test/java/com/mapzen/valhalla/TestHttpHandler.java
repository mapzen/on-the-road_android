package com.mapzen.valhalla;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Header;
import retrofit.client.Response;

public class TestHttpHandler extends HttpHandler {

  TestRoutingService testService;

  public TestHttpHandler(String endpoint, RestAdapter.LogLevel logLevel) {
    configure(endpoint, logLevel);
  }

  protected void configure(String endpoint, RestAdapter.LogLevel logLevel) {
    super.configure(endpoint, logLevel);
    testService = adapter.create(TestRoutingService.class);
  }

  @Override
  public void requestRoute(String routeJson, Callback callback) {
    try {
      String route = testService.getRoute(routeJson);
      List<Header> headers = new ArrayList<>();
      Response response = new Response("test", 200, "test", headers, null);
      callback.success(route, response);
    } catch (RetrofitError error) {
      callback.failure(error);
    }
  }

}
