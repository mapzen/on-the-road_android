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

  private static final String DEFAULT_URL = "https://valhalla.mapzen.com/";
  private static final RestAdapter.LogLevel DEFAULT_LOG_LEVEL = RestAdapter.LogLevel.NONE;

  String apiKey;
  String endpoint;
  RestAdapter.LogLevel logLevel;
  RestAdapter adapter;
  RoutingService service;

  private RequestInterceptor requestInterceptor = new RequestInterceptor() {
    @Override public void intercept(RequestFacade request) {
      addHeadersForRequest(request);
    }
  };

  public HttpHandler(String apiKey) {
    this(apiKey, DEFAULT_URL, DEFAULT_LOG_LEVEL);
  }

  public HttpHandler(String apiKey, RestAdapter.LogLevel logLevel) {
    this(apiKey, DEFAULT_URL, logLevel);
  }

  public HttpHandler(String apiKey, String endpoint, RestAdapter.LogLevel logLevel) {
    this.apiKey = apiKey;
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
    service.getRoute(routeJson, apiKey, callback);
  }

  /**
   * Subclasses can overwrite to add custom headers to each request
   * @param requestFacade
   */
  protected void addHeadersForRequest(RequestInterceptor.RequestFacade requestFacade) {

  }

}

