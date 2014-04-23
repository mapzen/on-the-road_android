package com.mapzen;

import android.location.Location;

import static com.mapzen.osrm.Route.SNAP_PROVIDER;

public class TestUtils {
    static public Location getLocation(double lat, double lng) {
        Location loc = new Location(SNAP_PROVIDER);
        loc.setLatitude(lat);
        loc.setLongitude(lng);
        return loc;
    }
}
