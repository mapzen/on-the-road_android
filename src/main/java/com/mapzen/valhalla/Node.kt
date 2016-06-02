package com.mapzen.valhalla

import com.mapzen.model.Location

open class Node(val lat: Double, val lng: Double) {
    var totalDistance: Double = 0.0
    var bearing: Double = 0.0
    var legDistance: Double = 0.0

    fun getLocation(): Location {
        val loc = Location()
        loc.latitude = lat
        loc.longitude = lng
        loc.bearing = bearing.toFloat()
        return loc
    }

    override fun toString(): String {
        return "[" + lat.toString() + "," + lng.toString() + "]" + " getLegDistance: " +
                legDistance.toString()
    }
}
