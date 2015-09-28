package com.mapzen.valhalla;

import com.google.gson.annotations.SerializedName;

public class JSON {
    public Location[] locations = new Location[2];
    public String costing;

    @SerializedName("directions_options")
    public DirectionOptions directionsOptions = new DirectionOptions();

    public static class Location {
        public String lat;
        public String lon;
        public String name;

        public Location(String lat, String lon) {
            this.lat = lat;
            this.lon = lon;
        }

        public Location(String lat, String lon, String name) {
            this.lat = lat;
            this.lon = lon;
            this.name = name;
        }
    }

    public static class DirectionOptions {
        public String units;
    }
}
