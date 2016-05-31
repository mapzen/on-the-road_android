package com.mapzen.helpers;

import com.mapzen.model.Location;

public class GeometryHelper {
    public static double getBearing(Location p1, Location p2) {
        return (p1.bearingTo(p2)+360) % 360;
    }
}
