package com.mapzen.valhalla;

import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Query;
import retrofit.Callback;


public interface RoutingService {
    @GET("/route")
    public void getRoute(
                          @Query("api_key") String api_key,
                     Callback<Result> callback );
        }