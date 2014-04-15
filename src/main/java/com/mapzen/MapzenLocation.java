package com.mapzen;

public class MapzenLocation implements Location {
    private double lat;
    private double lng;

    public MapzenLocation(double[] loc) {
        this.lat = loc[0];
        this.lng = loc[1];
    }

    public MapzenLocation(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    @Override
    public double getLatitude() {
        return lat;
    }

    @Override
    public double getLongitude() {
        return lng;
    }
}
