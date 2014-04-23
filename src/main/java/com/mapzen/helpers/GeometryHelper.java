package com.mapzen.helpers;

import android.location.Location;

import static java.lang.Math.toRadians;

public class GeometryHelper {
    public static double getBearing(Location p1, Location p2) {
        return (p1.bearingTo(p2)+360) % 360;
    }
}
