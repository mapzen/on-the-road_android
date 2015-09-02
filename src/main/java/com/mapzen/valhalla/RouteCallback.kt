package com.mapzen.valhalla

public interface RouteCallback {
    public fun success(route: Route)
    public fun failure(statusCode: Int)
}
