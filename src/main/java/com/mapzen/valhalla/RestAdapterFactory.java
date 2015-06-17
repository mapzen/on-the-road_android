package com.mapzen.valhalla;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;

/**
 * Created by peterjasko on 6/8/15.
 */
public class RestAdapterFactory {
    RestAdapter adapter;

    public RestAdapterFactory(RestAdapter adapter) {
        this.adapter = adapter;
    }

    public RoutingService getRoutingService() {
      return adapter.create(RoutingService.class);
    }
}
