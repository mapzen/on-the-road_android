package com.mapzen.helpers;

import com.mapzen.Location;

import static java.lang.Math.toRadians;

public class GeometryHelper {
    public static double getBearing(Location p1, Location p2) {
        double lat1 = toRadians(p1.getLatitude());
        double lat2 = toRadians(p2.getLatitude());
        double dLon = toRadians(p2.getLongitude() - p1.getLongitude());

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1)*Math.sin(lat2) -
                Math.sin(lat1)*Math.cos(lat2)*Math.cos(dLon);
        double brng = Math.atan2(y, x);

        return (Math.toDegrees(brng)+360) % 360;
    }

    public static double distanceBetweenPoints(Location pointA, Location pointB) {
        double R = 6371;
        double lat = toRadians(pointB.getLatitude() - pointA.getLatitude());
        double lon = toRadians(pointB.getLongitude() - pointA.getLongitude());
        double a = Math.sin(lat / 2) * Math.sin(lat / 2) +
                Math.cos(toRadians(pointA.getLatitude())) * Math.cos(toRadians(pointB.getLatitude())) *
                        Math.sin(lon / 2) * Math.sin(lon / 2);
        double c = 2 * Math.asin(Math.min(1, Math.sqrt(a)));
        double d = R * c;
        return d * 1000;
    }

}
