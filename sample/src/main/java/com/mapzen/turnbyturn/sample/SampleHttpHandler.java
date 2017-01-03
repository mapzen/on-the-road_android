package com.mapzen.turnbyturn.sample;

import com.mapzen.valhalla.HttpHandler;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * HttpHandler to set the sample api key
 */
public class SampleHttpHandler extends HttpHandler {

  private static final String NAME_API_KEY = "api_key";

  public SampleHttpHandler(HttpLoggingInterceptor.Level logLevel) {
    configure(DEFAULT_URL, logLevel);
  }

  @Override protected Response onRequest(Interceptor.Chain chain) throws IOException {
    final HttpUrl url = chain.request()
        .url()
        .newBuilder()
        .addQueryParameter(NAME_API_KEY, BuildConfig.API_KEY)
        .build();

    return chain.proceed(chain.request().newBuilder().url(url).build());
  }
}
