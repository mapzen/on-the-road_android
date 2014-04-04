package com.mapzen.osrm;

public class RouteHelper {
    public static double getBearing(double[] p1, double[] p2) {
        double lat1 = Math.toRadians(p1[0]);
        double lat2 = Math.toRadians(p2[0]);
        double dLon = Math.toRadians(p2[1] - p1[1]);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) -
                Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
        double brng = Math.atan2(y, x);

        return (Math.toDegrees(brng) + 360) % 360;
    }
}
