package com.mapzen;

import com.mapzen.model.ValhallaLocation;

public class TestUtils {
    static public ValhallaLocation getLocation(double lat, double lng) {
        ValhallaLocation loc = new ValhallaLocation();
        loc.setLatitude(lat);
        loc.setLongitude(lng);
        return loc;
    }
}
