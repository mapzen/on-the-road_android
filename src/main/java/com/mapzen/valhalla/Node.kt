package com.mapzen.valhalla

import android.location.Location


public open class Node(public val lat: Double, public val lng: Double) {
    var totalDistance: Double = 0.0
    var bearing: Double = 0.0
    var legDistance: Double = 0.0

    public fun getLocation(): Location {
        val loc = Location("snap")
        loc.setLatitude(lat)
        loc.setLongitude(lng)
        return loc
    }

    override fun toString(): String {
        return "[" + lat.toString() + "," + lng.toString() + "]" + " getLegDistance: " + legDistance.toString()
    }
}
