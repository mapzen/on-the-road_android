package com.mapzen.valhalla;

/**
 * Created by peterjasko on 6/8/15.
 */
public class JSON {

    public location[] locations = new location[2];
    public String costing;

    public static class location {
        public location() {

        }
        public String lat = "";
        public String lon = "";

    }
}
