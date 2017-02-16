package com.mapzen.valhalla;


import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class JSON {
    public List<JSON.Location> locations = new ArrayList<>();
    public String costing;

    @SerializedName("directions_options")
    public DirectionOptions directionsOptions = new DirectionOptions();

    public static class Location {
        public String lat;
        public String lon;
        public String name;
        public String street;
        public String city;
        public String state;
        public String heading;

        public Location(String lat, String lon) {
            this.lat = lat;
            this.lon = lon;
        }

        public Location(String lat, String lon, String heading) {
            this.lat = lat;
            this.lon = lon;
            this.heading = heading;
        }

        public Location(String lat, String lon, String name, String street,
                String city, String state) {
            this.lat = lat;
            this.lon = lon;
            this.name = name;
            this.street = street;
            this.city = city;
            this.state = state;
        }
    }

    public static class DirectionOptions {
        public String units;
        public String language;
    }
}
