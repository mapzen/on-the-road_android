package com.mapzen.osrm;

public class Node {
    private double lat, lng, totalDistance, bearing, legDistance;

    public Node(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
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

    public double[] getPoint() {
        return new double[] { lat, lng };
    }
}
