package com.mapzen.helpers;

import static java.lang.Math.toRadians;

public class GeometryHelper {
    public static double getBearing(double[] p1, double[] p2) {
        double lat1 = toRadians(p1[0]);
        double lat2 = toRadians(p2[0]);
        double dLon = toRadians(p2[1] - p1[1]);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1)*Math.sin(lat2) -
                Math.sin(lat1)*Math.cos(lat2)*Math.cos(dLon);
        double brng = Math.atan2(y, x);

        return (Math.toDegrees(brng)+360) % 360;
    }

    public static double distanceBetweenPoints(double[] pointA, double[] pointB) {
        double R = 6371;
        double lat = toRadians(pointB[0] - pointA[0]);
        double lon = toRadians(pointB[1] - pointA[1]);
        double a = Math.sin(lat / 2) * Math.sin(lat / 2) +
                Math.cos(toRadians(pointA[0])) * Math.cos(toRadians(pointB[0])) *
                        Math.sin(lon / 2) * Math.sin(lon / 2);
        double c = 2 * Math.asin(Math.min(1, Math.sqrt(a)));
        double d = R * c;
        return d * 1000;
    }

}
