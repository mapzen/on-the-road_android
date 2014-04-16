package com.mapzen.osrm;

import android.location.Location;

public class Node {
    private double lat, lng, totalDistance, bearing, legDistance;

    public Node(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public double getBearing() {
        return bearing;
    }

    public void setBearing(double bearing) {
        this.bearing = bearing;
    }

    public double getLegDistance() {
        return legDistance;
    }

    public void setLegDistance(double legDistance) {
        this.legDistance = legDistance;
    }

    public Location getLocation() {
        Location loc = new Location("snap");
        loc.setLatitude(lat);
        loc.setLongitude(lng);
        return loc;
    }
}
