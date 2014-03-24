package com.mapzen.osrm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.logging.Logger;

import static java.lang.Math.PI;
import static java.lang.Math.toRadians;

public class Route {
    public static final int KEY_LAT = 0;
    public static final int KEY_LNG = 1;
    public static final int KEY_TOTAL_DISTANCE = 2;
    public static final int KEY_BEARING = 3;
    public static final int KEY_LEG_DISTANCE = 4;
    private ArrayList<double[]> poly = null;
    private ArrayList<Instruction> turnByTurn = null;
    private JSONArray instructions;
    private JSONObject jsonObject;
    private int currentLeg = 0;
    static final Logger log = Logger.getLogger("RouteLogger");

    public Route() {
    }

    public Route(String jsonString) {
        setJsonObject(new JSONObject(jsonString));
    }

    public Route(JSONObject jsonObject) {
        setJsonObject(jsonObject);
    }

    public void setJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
        if (foundRoute()) {
            this.instructions = this.jsonObject.getJSONArray("route_instructions");
            initializeTurnByTurn();
        }
    }

    public int getTotalDistance() {
        return getSumary().getInt("total_distance");
    }

    public int getStatus() {
        return jsonObject.getInt("status");
    }

    public boolean foundRoute() {
        return getStatus() == 0;
    }

    public int getTotalTime() {
        return getSumary().getInt("total_time");
    }

    private void initializeTurnByTurn() {
        turnByTurn = new ArrayList<Instruction>();
        for(int i = 0; i < instructions.length(); i++) {
            Instruction instruction = new Instruction(instructions.getJSONArray(i));
            turnByTurn.add(instruction);
        }
    }

    public ArrayList<Instruction> getRouteInstructions() {
        double[] pre = null;
        double distance = 0;
        double totalDistance = 0;
        double[] markerPoint = {0, 0};

        int marker = 1;
        ArrayList<double[]> geometry = getGeometry();
        // set initial point to first instruction
        turnByTurn.get(0).setPoint(geometry.get(0));
        for(int i = 0; i < geometry.size(); i++) {
            double[] f = geometry.get(i);
            if(marker == turnByTurn.size()) {
                continue;
            }
            Instruction instruction = turnByTurn.get(marker);
            if(pre != null) {
                distance = f[KEY_TOTAL_DISTANCE] - pre[KEY_TOTAL_DISTANCE];
                totalDistance += distance;
            }
            // this needs the previous distance marker hence minus one
            if(Math.floor(totalDistance) > turnByTurn.get(marker-1).getDistance()) {
                instruction.setPoint(markerPoint);
                marker++;
                totalDistance = distance;
            }
            markerPoint = new double[]{f[0], f[1]};
            pre = f;

            // setting the last one to the destination
            if(geometry.size() - 1 == i) {
                turnByTurn.get(marker).setPoint(markerPoint);
            }
        }
        return turnByTurn;
    }

    public ArrayList<double[]> getGeometry() {
        return decodePolyline(jsonObject.getString("route_geometry"));
    }

    public double[] getStartCoordinates() {
        JSONArray points = getViaPoints().getJSONArray(0);
        double[] coordinates = {
            points.getDouble(0),
            points.getDouble(1)
        };
        return coordinates;
    }

    private JSONArray getViaPoints() {
        return jsonObject.getJSONArray("via_points");
    }

    private JSONObject getSumary() throws JSONException {
       return jsonObject.getJSONObject("route_summary");
    }

    private ArrayList<double[]> decodePolyline(String encoded) {
        double[] lastPair = {};
        if (poly == null) {
            poly = new ArrayList<double[]>();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;
            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;
                double x = (double) lat / 1E6;
                double y = (double) lng / 1E6;
                double[] pair = {0, 0, 0, 0, 0};
                pair[KEY_LAT] = x;
                pair[KEY_LNG] = y;
                if (!poly.isEmpty()) {
                    double[] lastElement = poly.get(poly.size()-1);
                    double distance = distanceBetweenPoints(pair, lastElement);
                    double totalDistance = distance + lastElement[KEY_TOTAL_DISTANCE];
                    pair[KEY_TOTAL_DISTANCE] = totalDistance;
                    if(lastPair.length > 0) {
                        lastPair[KEY_BEARING] = RouteHelper.getBearing(lastPair, pair);
                    }
                    lastPair[KEY_LEG_DISTANCE] = distance;
                }

                lastPair = pair;
                poly.add(pair);
            }
        }
        return poly;
    }

    private double distanceBetweenPoints(double[] pointA, double[] pointB) {
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

    public int getCurrentLeg() {
        return currentLeg;
    }

    public void setCurrentLeg(int currentLeg) {
        this.currentLeg = currentLeg;
    }

    public void rewind() {
        currentLeg = 0;
    }

    public double[] snapToRoute(double[] originalPoint) {
        log.info("Snapping => currentLeg: " + String.valueOf(currentLeg));
        log.info("Snapping => originalPoint: "
                + String.valueOf(originalPoint[0]) + ", "
                + String.valueOf(originalPoint[1]));

        int sizeOfPoly = poly.size();

        // we have exhausted options
        if(currentLeg >= sizeOfPoly) {
            return null;
        }

        double[] destination = poly.get(sizeOfPoly-1);

        // if close to destination
        double distanceToDestination = distanceBetweenPoints(destination, originalPoint);
        log.info("Snapping => distance to destination: " + String.valueOf(distanceToDestination));
        if (Math.floor(distanceToDestination) < 20) {
            return new double[] {
                    destination[KEY_LAT],
                    destination[KEY_LNG]
            };
        }

        double[] current = poly.get(currentLeg);
        double[] fixedPoint = snapTo(current, originalPoint, current[KEY_BEARING]);
        if (fixedPoint == null || (Double.isNaN(fixedPoint[0]) || Double.isNaN(fixedPoint[1]))) {
            log.info("Snapping => returning current");
            return new double[] {current[KEY_LAT], current[KEY_LNG]};
        } else {
            double distance = distanceBetweenPoints(current, fixedPoint);
            log.info("Snapping => distance between current and fixed: " + String.valueOf(distance));
            double bearingToOriginal = RouteHelper.getBearing(current, originalPoint);
            log.info("Snapping => bearing to original: " + String.valueOf(bearingToOriginal));
                                               /// UGH somewhat arbritrary
            double bearingDiff = Math.abs(bearingToOriginal - current[KEY_BEARING]);
            if (distance > current[KEY_LEG_DISTANCE] - 5 || (distance > 30 && bearingDiff > 20.0)) {
                ++currentLeg;
                log.info("Snapping => incrementing and trying again");
                log.info("Snapping => currentLeg: " + String.valueOf(currentLeg));
                return snapToRoute(originalPoint);
            }
        }

        boolean tooFarAway = true;
        for (double[] point : poly) {
            double distance = distanceBetweenPoints(point, fixedPoint);
            if (distance < 200) {
                tooFarAway = false;
                break;
            }
        }

        if (tooFarAway) {
            return null;
        } else {
            return fixedPoint;
        }
    }

    private double[] snapTo(double[] turnPoint, double[] location, double turnBearing) {
        double[] correctedLocation = snapTo(turnPoint, location, turnBearing, 90);
        if (correctedLocation == null) {
            correctedLocation = snapTo(turnPoint, location, turnBearing, -90);
        }
        double distance;
        if (correctedLocation != null) {
            distance = distanceBetweenPoints(correctedLocation, location);
            if(Math.round(distance) > 1000) {
                return null;
            }
        }

        return correctedLocation;
    }


    private double[] snapTo(double[] turnPoint, double[] location, double turnBearing, int offset) {
        double lat1 = toRadians(turnPoint[0]);
        double lon1 = toRadians(turnPoint[1]);
        double lat2 = toRadians(location[0]);
        double lon2 = toRadians(location[1]);

        double brng13 = toRadians(turnBearing);
        double brng23 = toRadians(turnBearing + offset);
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double dist12 = 2 * Math.asin(Math.sqrt(Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2)));
        if (dist12 == 0) {
            return null;
        }

        // initial/final bearings between points
        double brngA = Math.acos((Math.sin(lat2) - Math.sin(lat1) * Math.cos(dist12)) /
                (Math.sin(dist12) * Math.cos(lat1)));

        double brngB = Math.acos((Math.sin(lat1) - Math.sin(lat2) * Math.cos(dist12)) /
                (Math.sin(dist12) * Math.cos(lat2)));

        double brng12, brng21;
        if (Math.sin(lon2 - lon1) > 0) {
            brng12 = brngA;
            brng21 = 2 * Math.PI - brngB;
        } else {
            brng12 = 2 * Math.PI - brngA;
            brng21 = brngB;
        }

        double alpha1 = (brng13 - brng12 + Math.PI) % (2 * Math.PI) - Math.PI;  // angle 2-1-3
        double alpha2 = (brng21 - brng23 + Math.PI) % (2 * Math.PI) - Math.PI;  // angle 1-2-3

        if (Math.sin(alpha1) == 0 && Math.sin(alpha2) == 0) {
            return null;  // infinite intersections
        }
        if (Math.sin(alpha1) * Math.sin(alpha2) < 0) {
            return null;       // ambiguous intersection
        }

        double alpha3 = Math.acos(-Math.cos(alpha1) * Math.cos(alpha2) +
                Math.sin(alpha1) * Math.sin(alpha2) * Math.cos(dist12));
        double dist13 = Math.atan2(Math.sin(dist12) * Math.sin(alpha1) * Math.sin(alpha2),
                Math.cos(alpha2) + Math.cos(alpha1) * Math.cos(alpha3));
        double lat3 = Math.asin(Math.sin(lat1) * Math.cos(dist13) +
                Math.cos(lat1) * Math.sin(dist13) * Math.cos(brng13));
        double dLon13 = Math.atan2(Math.sin(brng13) * Math.sin(dist13) * Math.cos(lat1),
                Math.cos(dist13) - Math.sin(lat1) * Math.sin(lat3));
        double lon3 = ((lon1 + dLon13) + 3 * Math.PI) % (2 * Math.PI) - Math.PI;  // normalise to -180..+180º

        double[] point = {Math.toDegrees(lat3), Math.toDegrees(lon3)};
        return point;
    }
}
