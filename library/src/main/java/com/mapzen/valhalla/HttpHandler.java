package com.mapzen.valhalla;

import com.mapzen.http.Tls12OkHttpClientFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 *  Handles all things http including setting the route engine's url and request log level. Route
 *  requests can be made using this object. To customize headers and params that are sent with each
 *  request, subclass this object and overwrite
 *  {@link HttpHandler#onRequest(Interceptor.Chain)}
 */
public class HttpHandler {

  protected static final String DEFAULT_URL = "https://valhalla.mapzen.com/";
  protected static final HttpLoggingInterceptor.Level DEFAULT_LOG_LEVEL =
      HttpLoggingInterceptor.Level.NONE;

  String endpoint;
  HttpLoggingInterceptor.Level logLevel;
  Retrofit adapter;
  RoutingService service;

  private Interceptor requestInterceptor = new Interceptor() {
    @Override public Response intercept(Chain chain) throws IOException {
      return onRequest(chain);
    }
  };

  private Gson gson = new GsonBuilder()
      .registerTypeAdapter(JSON.Location.class, new LocationSerializer())
      .create();

  public HttpHandler() {
    this(DEFAULT_URL, DEFAULT_LOG_LEVEL);
  }

  public HttpHandler(String endpoint) {
    this(endpoint, DEFAULT_LOG_LEVEL);
  }

  public HttpHandler(HttpLoggingInterceptor.Level logLevel) {
    this(DEFAULT_URL, logLevel);
  }

  public HttpHandler(String endpoint, HttpLoggingInterceptor.Level logLevel) {
    configure(endpoint, logLevel);
  }

  protected void configure(String endpoint, HttpLoggingInterceptor.Level logLevel) {
    final OkHttpClient.Builder builder = Tls12OkHttpClientFactory.Companion.
        enableTls12OnPreLollipop(new OkHttpClient.Builder());
    final OkHttpClient client = builder
        .addNetworkInterceptor(requestInterceptor)
        .addNetworkInterceptor(new HttpLoggingInterceptor().setLevel(logLevel))
        .build();

    this.endpoint = endpoint;
    this.logLevel = logLevel;
    this.adapter = new Retrofit.Builder()
        .baseUrl(endpoint)
        .client(client)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(new Converter.Factory() {
          @Override public Converter<?, String> stringConverter(Type type, Annotation[] annotations,
              Retrofit retrofit) {
            return new Converter<Object, String>() {
              @Override public String convert(Object value) throws IOException {
                return gson.toJson(value);
              }
            };
          }
        })
        .build();
    this.service = new RestAdapterFactory(this.adapter).getRoutingService();
  }

  public Call<String> requestRoute(JSON routeJson, Callback<String> callback) {
    Call call = service.getRoute(routeJson);
    call.enqueue(callback);
    return call;
  }

  /**
   * Subclasses can overwrite to add custom headers to each request.
   * @param chain used to modify outgoing requests.
   */
  protected Response onRequest(Interceptor.Chain chain) throws IOException {
    return chain.proceed(chain.request());
  }
}
