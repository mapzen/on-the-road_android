package com.mapzen.valhalla

import retrofit.RestAdapter

public interface Router {
    public enum class Type(private val type: String) {
        WALKING("pedestrian"),
        BIKING("bicycle"),
        DRIVING("auto");

        override fun toString(): String {
            return type
        }
    }

    public enum class DistanceUnits(private val units: String) {
        MILES("miles"),
        KILOMETERS("kilometers");

        override fun toString(): String {
            return units
        }
    }

    public fun setApiKey(key: String): Router
    public fun setWalking(): Router
    public fun setDriving(): Router
    public fun setBiking(): Router
    public fun setLocation(point: DoubleArray): Router
    public fun setLocation(point: DoubleArray, heading: Float): Router
    public fun setLocation(point: DoubleArray,
            name: String? = null,
            street: String? = null,
            city: String? = null,
            state: String? = null): Router
    public fun setDistanceUnits(units: DistanceUnits): Router
    public fun clearLocations(): Router
    public fun setEndpoint(url: String): Router
    public fun getEndpoint(): String
    public fun setCallback(callback: RouteCallback): Router
    public fun fetch()
    public fun getJSONRequest(): JSON
    public fun setLogLevel(logLevel: RestAdapter.LogLevel): Router
    public fun setDntEnabled(enabled: Boolean): Router
    public fun isDntEnabled(): Boolean
}
