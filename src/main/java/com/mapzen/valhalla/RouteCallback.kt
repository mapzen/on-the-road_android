package com.mapzen.valhalla

interface RouteCallback {
    fun success(route: Route)
    fun failure(statusCode: Int)
}
