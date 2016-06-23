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

    public static String getRouteFixture(String name) {
        return getFixture(name + ".route");
    }

    public static String getInstructionFixture(String name) {
        return getFixture(name + ".instruction");
    }

    public static String getTransitInfoFixture(String name) {
        return getFixture(name + ".transitinfo");
    }

    public static String getTransitStopFixture(String name) {
        return getFixture(name + ".transitstop");
    }

    private static String getFixture(String name) {
        String basedir = System.getProperty("user.dir");
        File file = new File(basedir + "/src/test/fixtures/" + name);
        String fixture = "";
        try {
            fixture = Files.toString(file, Charsets.UTF_8);
        } catch (Exception e) {
            fixture = "not found";
        }
        return fixture;
    }
}
