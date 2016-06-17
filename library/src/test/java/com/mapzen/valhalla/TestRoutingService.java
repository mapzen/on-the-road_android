package com.mapzen.valhalla;

import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by sarahlensing on 6/16/16.
 */
public interface TestRoutingService {
  @GET("/route") String getRoute(
      @Query("json")
      String json);
}
