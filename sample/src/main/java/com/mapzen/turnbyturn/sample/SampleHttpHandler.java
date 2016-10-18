package com.mapzen.turnbyturn.sample;

import com.mapzen.valhalla.HttpHandler;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;

/**
 * HttpHandler to set the sample api key
 */
public class SampleHttpHandler extends HttpHandler {

  private static final String NAME_API_KEY = "api_key";

  public SampleHttpHandler(RestAdapter.LogLevel logLevel) {
    configure(DEFAULT_URL, logLevel);
  }

  @Override protected void onRequest(RequestInterceptor.RequestFacade requestFacade) {
    String apiKey = BuildConfig.API_KEY;
    requestFacade.addQueryParam(NAME_API_KEY, apiKey);
  }
}
