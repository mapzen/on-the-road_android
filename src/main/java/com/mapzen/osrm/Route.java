package com.mapzen.osrm;

import com.f2prateek.ln.Ln;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static com.mapzen.helpers.GeometryHelper.getBearing;
import static java.lang.Math.toRadians;

public class Route {
    public static final String SNAP_PROVIDER = "snap";
    public static final int LOST_THRESHOLD = 100;
    private ArrayList<Node> poly = null;
    private ArrayList<Instruction> instructions = null;
    private JSONObject jsonObject;
    private int currentLeg = 0;
    private Set<Instruction> seenInstructions = new HashSet<Instruction>();
    private boolean lost = false;

    public JSONObject getRawRoute() {
        return jsonObject;
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
            initializeTurnByTurn(jsonObject.getJSONArray("route_instructions"));
            initializePolyline(jsonObject.getString("route_geometry"));
        }
    }

    public int getTotalDistance() {
        return getSummary().getInt("total_distance");
    }

    public int getStatus() {
        return jsonObject.getInt("status");
    }

    public boolean foundRoute() {
        return getStatus() == 0;
    }

    public int getTotalTime() {
        return getSummary().getInt("total_time");
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
        Location markerPoint = new Location(SNAP_PROVIDER);

        if (instructions.size() == poly.size()) {
            return getSimpleRouteInstructions();
        }

        int marker = 1;
        // set initial point to first instruction
        instructions.get(0).setLocation(poly.get(0).getLocation());
        for (int i = 0; i < poly.size(); i++) {
            Node node = poly.get(i);
            if (marker == instructions.size()) {
                continue;
            }

            Instruction instruction = instructions.get(marker);
            if (pre != null) {
                distance = node.getTotalDistance() - pre.getTotalDistance();
                totalDistance += distance;
            }

            // this needs the previous distance marker hence minus one
            if (Math.floor(totalDistance) > instructions.get(marker - 1).getDistance()) {
                instruction.setLocation(markerPoint);
                marker++;
                totalDistance = distance;
            }
            markerPoint = node.getLocation();
            pre = node;

            // setting the last one to the destination
            if (poly.size() - 1 == i) {
                Instruction lastInstruction = instructions.get(instructions.size() - 1);
                lastInstruction.setLocation(markerPoint);
            }
        }
        return instructions;
    }

    /**
     * Populates location values for a simple set of route instructions where the number of
     * instructions equals the number of nodes in the polyline.
     *
     * @return simple instruction list with location values.
     */
    private ArrayList<Instruction> getSimpleRouteInstructions() {
        for (Instruction instruction : instructions) {
            instruction.setLocation(poly.get(instructions.indexOf(instruction)).getLocation());
        }

        return instructions;
    }

    public ArrayList<Location> getGeometry() {
        ArrayList<Location> geometry = new ArrayList<Location>();
        for (Node node : poly) {
            geometry.add(node.getLocation());
        }
        return geometry;
    }

    public Location getStartCoordinates() {
        JSONArray points = getViaPoints().getJSONArray(0);
        Location location = new Location(SNAP_PROVIDER);
        location.setLatitude(points.getDouble(0));
        location.setLongitude(points.getDouble(1));
        return location;
    }

    public boolean isLost() {
        return lost;
    }

    private JSONArray getViaPoints() {
        return jsonObject.getJSONArray("via_points");
    }

    private JSONObject getSummary() throws JSONException {
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
                Node node = new Node(x, y);
                if (!poly.isEmpty()) {
                    Node lastElement = poly.get(poly.size() - 1);
                    double distance = node.getLocation().distanceTo(lastElement.getLocation());
                    double totalDistance = distance + lastElement.getTotalDistance();
                    node.setTotalDistance(totalDistance);
                    if (lastNode != null) {
                        lastNode.setBearing(getBearing(lastNode.getLocation(), node.getLocation()));
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

    public double getCurrentRotationBearing() {
        return 360 - poly.get(currentLeg).getBearing();
    }

    public void rewind() {
        currentLeg = 0;
    }

    public Location snapToRoute(Location originalPoint) {
        Ln.d("Snapping => currentLeg: " + String.valueOf(currentLeg));
        Ln.d("Snapping => originalPoint: "
                + String.valueOf(originalPoint.getLatitude()) + ", "
                + String.valueOf(originalPoint.getLongitude()));

        int sizeOfPoly = poly.size();

        // we have exhausted options
        if (currentLeg >= sizeOfPoly) {
            return null;
        }

        Node destination = poly.get(sizeOfPoly - 1);

        // if close to destination
        double distanceToDestination = destination.getLocation().distanceTo(originalPoint);
        Ln.d("Snapping => distance to destination: " + String.valueOf(distanceToDestination));
        if (Math.floor(distanceToDestination) < 20) {
            return destination.getLocation();
        }

        Node current = poly.get(currentLeg);
        Location fixedPoint = snapTo(current, originalPoint);
        if (fixedPoint == null) {
            fixedPoint = current.getLocation();
        } else {
            double distance = current.getLocation().distanceTo(fixedPoint);
            Ln.d("Snapping => distance between current and fixed: " + String.valueOf(distance));
            double bearingToOriginal = getBearing(current.getLocation(), originalPoint);
            Ln.d("Snapping => bearing to original: " + String.valueOf(bearingToOriginal));
            /// UGH somewhat arbritrary
            double bearingDiff = Math.abs(bearingToOriginal - current.getBearing());
            if (distance > current.getLegDistance() - 5 || (distance > 30 && bearingDiff > 20.0)) {
                ++currentLeg;
                Ln.d("Snapping => incrementing and trying again");
                Ln.d("Snapping => currentLeg: " + String.valueOf(currentLeg));
                return snapToRoute(originalPoint);
            }
        }

        double correctionDistance = originalPoint.distanceTo(fixedPoint);
        Ln.d("Snapping => correctionDistance: " + String.valueOf(correctionDistance));
        Ln.d("Snapping => Lost Threshold: " + String.valueOf(LOST_THRESHOLD));
        if (correctionDistance < LOST_THRESHOLD) {
            return fixedPoint;
        } else {
            lost = true;
            return null;
        }
    }

    private Location snapTo(Node turnPoint, Location location) {
        Location correctedLocation = snapTo(turnPoint, location, 90);
        if (correctedLocation == null) {
            correctedLocation = snapTo(turnPoint, location, -90);
        }
        double distance;
        if (correctedLocation != null) {
            distance = correctedLocation.distanceTo(location);
            if (Math.round(distance) > 1000) {
                return null;
            }
        }

        return correctedLocation;
    }

    private Location snapTo(Node turnPoint, Location location, int offset) {
        double lat1 = toRadians(turnPoint.getLat());
        double lon1 = toRadians(turnPoint.getLng());
        double lat2 = toRadians(location.getLatitude());
        double lon2 = toRadians(location.getLongitude());

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

        Location loc = new Location(SNAP_PROVIDER);
        loc.setLatitude(Math.toDegrees(lat3));
        loc.setLongitude(Math.toDegrees(lon3));
        return loc;
    }

    public Set<Instruction> getSeenInstructions() {
        return seenInstructions;
    }

    public void addSeenInstruction(Instruction instruction) {
        seenInstructions.add(instruction);
    }

    public Instruction getClosestInstruction(Location location) {
        Instruction closestInstruction = null;
        int closestDistance = (int) 1e8;
        for (Instruction instruction : instructions) {
            Location temporaryLocationObj = instruction.getLocation();
            final int distanceToTurn =
                    (int) Math.floor(location.distanceTo(temporaryLocationObj));
            if (!seenInstructions.contains(instruction) && distanceToTurn < closestDistance) {
                if (closestInstruction == null
                        || instructions.indexOf(closestInstruction) > instructions.indexOf(instruction)) {
                    closestDistance = distanceToTurn;
                    closestInstruction = instruction;
                }
            }
        }
        return closestInstruction;
    }
}
