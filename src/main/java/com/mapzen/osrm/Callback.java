package com.mapzen.osrm;

public interface Callback {
    void success(Route route);
    void failure(int statusCode);
}
