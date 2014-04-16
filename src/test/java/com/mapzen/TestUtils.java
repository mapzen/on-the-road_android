package com.mapzen;

import android.location.Location;

public class TestUtils {
    static public Location getLocation(double lat, double lng) {
        Location loc = new Location("snap");
        loc.setLatitude(lat);
        loc.setLongitude(lng);
        return loc;
    }
}
