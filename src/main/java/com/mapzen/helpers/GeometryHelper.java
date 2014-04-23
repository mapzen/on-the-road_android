package com.mapzen.helpers;

import android.location.Location;

public class GeometryHelper {
    public static double getBearing(Location p1, Location p2) {
        return (p1.bearingTo(p2)+360) % 360;
    }
}
