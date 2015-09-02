package com.mapzen.valhalla

public interface Router {
    public enum class Type(private val type: String) {
        WALKING("pedestrian"),
        BIKING("bicycle"),
        DRIVING("auto")

        override fun toString(): String {
            return type
        }
    }

    public fun setApiKey(key: String): Router
    public fun setWalking(): Router
    public fun setDriving(): Router
    public fun setBiking(): Router
    public fun setLocation(point: DoubleArray): Router
    public fun clearLocations(): Router
    public fun setEndpoint(url: String): Router
    public fun getEndpoint(): String
    public fun setCallback(callback: RouteCallback): Router
    public fun fetch()
    public fun getJSONRequest(): JSON
}
