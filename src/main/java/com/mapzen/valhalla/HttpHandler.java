package com.mapzen.valhalla;

import com.mapzen.helpers.ResultConverter;
import com.mapzen.valhalla.RestAdapterFactory;
import com.mapzen.valhalla.RoutingService;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;

/**
 *
 */
public class HttpHandler {

  protected static final String DEFAULT_URL = "https://valhalla.mapzen.com/";
  protected static final RestAdapter.LogLevel DEFAULT_LOG_LEVEL = RestAdapter.LogLevel.NONE;

  String endpoint;
  RestAdapter.LogLevel logLevel;
  RestAdapter adapter;
  RoutingService service;

  private RequestInterceptor requestInterceptor = new RequestInterceptor() {
    @Override public void intercept(RequestFacade request) {
      onRequest(request);
    }
  };

  public HttpHandler() {
    this(DEFAULT_URL, DEFAULT_LOG_LEVEL);
  }

  public HttpHandler(String endpoint) {
    this(endpoint, DEFAULT_LOG_LEVEL);
  }

  public HttpHandler(RestAdapter.LogLevel logLevel) {
    this(DEFAULT_URL, logLevel);
  }

  public HttpHandler(String endpoint, RestAdapter.LogLevel logLevel) {
    configure(endpoint, logLevel);
  }

  protected void configure(String endpoint, RestAdapter.LogLevel logLevel) {
    this.endpoint = endpoint;
    this.logLevel = logLevel;
    this.adapter = new RestAdapter.Builder()
        .setConverter(new ResultConverter())
        .setEndpoint(endpoint)
        .setLogLevel(logLevel)
        .setRequestInterceptor(requestInterceptor)
        .build();
    this.service = new RestAdapterFactory(this.adapter).getRoutingService();
  }

  public void requestRoute(String routeJson, Callback callback) {
    service.getRoute(routeJson, callback);
  }

  /**
   * Subclasses can overwrite to add custom headers to each request
   * @param requestFacade
   */
  protected void onRequest(RequestInterceptor.RequestFacade requestFacade) {

  }

}

