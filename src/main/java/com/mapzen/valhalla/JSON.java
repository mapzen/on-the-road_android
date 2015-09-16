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
    }

    public static class DirectionOptions {
        public String units;
    }
}
