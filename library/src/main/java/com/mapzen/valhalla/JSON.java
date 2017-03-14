package com.mapzen.valhalla;


import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class JSON {
    public static final double HEADING_NONE = -1;

    public List<JSON.Location> locations = new ArrayList<>();
    public String costing;

    @SerializedName("directions_options")
    public DirectionOptions directionsOptions = new DirectionOptions();

    public static class Location {
        public double lat;
        public double lon;
        public String name;
        public String street;
        public String city;
        public String state;
        public double heading = HEADING_NONE;

        public Location(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }

        public Location(double lat, double lon, double heading) {
            if (heading < 0 || heading >= 360) {
                throw new IllegalArgumentException("Heading value must in the range [0, 360)");
            }

            this.lat = lat;
            this.lon = lon;
            this.heading = heading;
        }

        public Location(double lat, double lon, String name, String street,
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
