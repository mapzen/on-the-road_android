package com.mapzen.valhalla;

import retrofit2.Retrofit;

public class RestAdapterFactory {
    Retrofit adapter;

    public RestAdapterFactory(Retrofit adapter) {
        this.adapter = adapter;
    }

    public RoutingService getRoutingService() {
      return adapter.create(RoutingService.class);
    }
}
