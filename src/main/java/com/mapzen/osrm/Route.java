package com.mapzen.osrm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.logging.Logger;

import static com.mapzen.helpers.GeometryHelper.distanceBetweenPoints;
import static com.mapzen.helpers.GeometryHelper.getBearing;
import static java.lang.Math.toRadians;

public class Route {
    public static final int LOST_THRESHOLD = 100;
    private ArrayList<Node> poly = null;
    private ArrayList<Instruction> instructions = null;
    private JSONObject jsonObject;
    private int currentLeg = 0;
    static final Logger log = Logger.getLogger("RouteLogger");

    public Route(String jsonString) {
        setJsonObject(new JSONObject(jsonString));
    }

    public Route(JSONObject jsonObject) {
        setJsonObject(jsonObject);
    }

    public void setJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
        if (foundRoute()) {
            initializeTurnByTurn(jsonObject.getJSONArray("route_instructions"));
            initializePolyline(jsonObject.getString("route_geometry"));
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

    private void initializeTurnByTurn(JSONArray instructions) {
        this.instructions = new ArrayList<Instruction>();
        for (int i = 0; i < instructions.length(); i++) {
            Instruction instruction = new Instruction(instructions.getJSONArray(i));
            this.instructions.add(instruction);
        }
    }

    public ArrayList<Instruction> getRouteInstructions() {
        Node pre = null;
        double distance = 0;
        double totalDistance = 0;
        double[] markerPoint = { 0, 0 };

        int marker = 1;
        // set initial point to first instruction
        instructions.get(0).setPoint(poly.get(0).getPoint());
        for(int i = 0; i < poly.size(); i++) {
            Node node = poly.get(i);
            if(marker == instructions.size()) {
                continue;
            }
            Instruction instruction = instructions.get(marker);
            if(pre != null) {
                distance = node.getTotalDistance() - pre.getTotalDistance();
                totalDistance += distance;
            }
            // this needs the previous distance marker hence minus one
            if(Math.floor(totalDistance) > instructions.get(marker-1).getDistance()) {
                instruction.setPoint(markerPoint);
                marker++;
                totalDistance = distance;
            }
            markerPoint = node.getPoint();
            pre = node;

            // setting the last one to the destination
            if(poly.size() - 1 == i) {
                instructions.get(marker).setPoint(markerPoint);
            }
        }
        return instructions;
    }

    public ArrayList<double[]> getGeometry() {
        ArrayList<double[]> geometry = new ArrayList<double[]>();
        for(Node node : poly) {
            geometry.add(node.getPoint());
        }
        return geometry;
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

    private ArrayList<Node> initializePolyline(String encoded) {
        Node lastNode = null;
        if (poly == null) {
            poly = new ArrayList<Node>();
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
                Node node = new Node(x,y);
                if (!poly.isEmpty()) {
                    Node lastElement = poly.get(poly.size()-1);
                    double distance = distanceBetweenPoints(node.getPoint(),
                            lastElement.getPoint());
                    double totalDistance = distance + lastElement.getTotalDistance();
                    node.setTotalDistance(totalDistance);
                    if(lastNode != null) {
                        lastNode.setBearing(getBearing(lastNode.getPoint(), node.getPoint()));
                    }
                    lastNode.setLegDistance(distance);
                }

                lastNode = node;
                poly.add(node);
            }
        }
        return poly;
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
        if (currentLeg >= sizeOfPoly) {
            return null;
        }

        Node destination = poly.get(sizeOfPoly-1);

        // if close to destination
        double distanceToDestination = distanceBetweenPoints(destination.getPoint(), originalPoint);
        log.info("Snapping => distance to destination: " + String.valueOf(distanceToDestination));
        if (Math.floor(distanceToDestination) < 20) {
            return destination.getPoint();
        }

        Node current = poly.get(currentLeg);
        double[] fixedPoint = snapTo(current, originalPoint);
        if (fixedPoint == null) {
            fixedPoint = current.getPoint();
        } else {
            double distance = distanceBetweenPoints(current.getPoint(), fixedPoint);
            log.info("Snapping => distance between current and fixed: " + String.valueOf(distance));
            double bearingToOriginal = getBearing(current.getPoint(), originalPoint);
            log.info("Snapping => bearing to original: " + String.valueOf(bearingToOriginal));
                                               /// UGH somewhat arbritrary
            double bearingDiff = Math.abs(bearingToOriginal - current.getBearing());
            if (distance > current.getLegDistance() - 5 || (distance > 30 && bearingDiff > 20.0)) {
                ++currentLeg;
                log.info("Snapping => incrementing and trying again");
                log.info("Snapping => currentLeg: " + String.valueOf(currentLeg));
                return snapToRoute(originalPoint);
            }
        }

        double correctionDistance = distanceBetweenPoints(originalPoint, fixedPoint);
        log.info("Snapping => correctionDistance: " + String.valueOf(correctionDistance));
        log.info("Snapping => Lost Threshold: " + String.valueOf(LOST_THRESHOLD));
        if (correctionDistance < LOST_THRESHOLD) {
            return fixedPoint;
        } else {
            return null;
        }
    }

    private double[] snapTo(Node turnPoint, double[] location) {
        double[] correctedLocation = snapTo(turnPoint, location, 90);
        if (correctedLocation == null) {
            correctedLocation = snapTo(turnPoint, location, -90);
        }
        double distance;
        if (correctedLocation != null) {
            distance = distanceBetweenPoints(correctedLocation, location);
            if (Math.round(distance) > 1000) {
                return null;
            }
        }

        return correctedLocation;
    }

    private double[] snapTo(Node turnPoint, double[] location, int offset) {
        double lat1 = toRadians(turnPoint.getLat());
        double lon1 = toRadians(turnPoint.getLng());
        double lat2 = toRadians(location[0]);
        double lon2 = toRadians(location[1]);

        double brng13 = toRadians(turnPoint.getBearing());
        double brng23 = toRadians(turnPoint.getBearing() + offset);
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;
        if (dLon == 0) {
            dLon = 0.001;
        }

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
        double lon3 = ((lon1 + dLon13) + 3 * Math.PI) % (2 * Math.PI)
                - Math.PI;  // normalise to -180..+180º

        double[] point = { Math.toDegrees(lat3), Math.toDegrees(lon3) };
        return point;
    }
}
