package com.mapzen.valhalla;

import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.Call;

public interface RoutingService {
    @GET("/route") Call<String> getRoute(@Query("json") String json);
}
