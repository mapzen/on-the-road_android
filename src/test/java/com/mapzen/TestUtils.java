package com.mapzen;

import com.mapzen.model.Location;

public class TestUtils {
    static public Location getLocation(double lat, double lng) {
        Location loc = new Location();
        loc.setLatitude(lat);
        loc.setLongitude(lng);
        return loc;
    }
}
