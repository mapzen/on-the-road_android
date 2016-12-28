package com.mapzen.valhalla;

import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TestRoutingService {
  @GET("/route") String getRoute(@Query("json") String json);
}
