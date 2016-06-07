package com.mapzen.valhalla;

import retrofit.http.GET;
import retrofit.http.Query;

public interface RoutingService {
    @GET("/route") void getRoute(
            @Query("json")
            String json,
            retrofit.Callback<String> callback);
}
