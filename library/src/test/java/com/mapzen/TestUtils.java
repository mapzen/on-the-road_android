package com.mapzen;

import com.mapzen.model.ValhallaLocation;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.io.File;

public class TestUtils {
    public static ValhallaLocation getLocation(double lat, double lng) {
        ValhallaLocation loc = new ValhallaLocation();
        loc.setLatitude(lat);
        loc.setLongitude(lng);
        return loc;
    }

    public static String getFixture(String name) {
        String basedir = System.getProperty("user.dir");
        File file = new File(basedir + "/src/test/fixtures/" + name + ".route");
        String fixture = "";
        try {
            fixture = Files.toString(file, Charsets.UTF_8);
        } catch (Exception e) {
            fixture = "not found";
        }
        return fixture;
    }
}
