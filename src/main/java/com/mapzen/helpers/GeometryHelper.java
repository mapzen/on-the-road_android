package com.mapzen.helpers;

import com.mapzen.model.ValhallaLocation;

public class GeometryHelper {
    public static double getBearing(ValhallaLocation p1, ValhallaLocation p2) {
        return (p1.bearingTo(p2)+360) % 360;
    }
}
