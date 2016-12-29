package com.mapzen.valhalla;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Executes route requests synchronously in testing environment.
 */
class TestHttpHandler extends HttpHandler {
  boolean headersAdded = false;
  Response<String> route = null;

  TestHttpHandler(String endpoint, HttpLoggingInterceptor.Level logLevel) {
    super(endpoint, logLevel);
  }

  @Override public void requestRoute(String routeJson, Callback<String> callback) {
    try {
      route = service.getRoute(routeJson).execute();
    } catch (IOException e) {
      e.printStackTrace();
    }
    callback.onResponse(null, route);
  }

  @Override protected okhttp3.Response onRequest(Interceptor.Chain chain) throws IOException {
    headersAdded = true;
    return chain.proceed(chain.request());
  }
}
