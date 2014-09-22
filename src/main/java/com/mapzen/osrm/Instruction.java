package com.mapzen.osrm;

import com.mapzen.helpers.DistanceFormatter;

import org.json.JSONArray;
import org.json.JSONException;

import android.location.Location;

import java.util.Locale;

import static com.mapzen.osrm.Route.SNAP_PROVIDER;

public class Instruction {
    public static final String NO_TURN = "No turn"; // 0; (Give no instruction at all)
    public static final String GO_STRAIGHT = "Continue on"; //1; (Tell user to go straight!)
    public static final String TURN_SLIGHT_RIGHT = "Slight right on"; //2;
    public static final String TURN_RIGHT = "Right on"; // 3;
    public static final String TURN_SHARP_RIGHT = "Sharp right on"; // 4;
    public static final String U_TURN = "U Turn"; // 5;
    public static final String TURN_SHARP_LEFT = "Sharp left on"; // 6;
    public static final String TURN_LEFT = "Left on"; // 7;
    public static final String TURN_SLIGHT_LEFT = "Slight left on"; // 8;
    public static final String REACH_VIA_POINT = "Reach via point"; // 9;
    public static final String HEAD_ON = "Head on"; // 10;
    public static final String ENTER_ROUND_ABOUT = "Enter round about"; // 11;
    public static final String LEAVE_ROUND_ABOUT = "Leave round about"; // 12;
    public static final String STAY_ON_ROUND_ABOUT = "Stay on round about"; // 13;
    public static final String START_AT_END_OF_STREET = "Start at end of street"; // 14;
    public static final String YOU_HAVE_ARRIVED = "You have arrived"; // 15;
    public static final String ENTER_AGAINST_ALLOWED_DIRECTION =
            "Enter"; //16;
    public static final String LEAVE_AGAINST_ALLOWED_DIRECTION =
            "Leave"; //17;
    public static String[] decodedInstructions = {
            NO_TURN, GO_STRAIGHT, TURN_SLIGHT_RIGHT, TURN_RIGHT, TURN_SHARP_RIGHT, U_TURN,
            TURN_SHARP_LEFT, TURN_LEFT, TURN_SLIGHT_LEFT, REACH_VIA_POINT, HEAD_ON,
            ENTER_ROUND_ABOUT, LEAVE_ROUND_ABOUT, STAY_ON_ROUND_ABOUT, START_AT_END_OF_STREET,
            YOU_HAVE_ARRIVED, ENTER_AGAINST_ALLOWED_DIRECTION, LEAVE_AGAINST_ALLOWED_DIRECTION
    };
    private JSONArray json;
    private int turn, distanceInMeters;
    private Location location = new Location(SNAP_PROVIDER);
    private int liveDistanceToNext = -1;

    public int getLiveDistanceToNext() {
        return liveDistanceToNext;
    }

    public void setLiveDistanceToNext(int liveDistanceToNext) {
        this.liveDistanceToNext = liveDistanceToNext;
    }

    public Instruction(JSONArray json) {
        if (json.length() < 8) {
            throw new JSONException("too few arguments");
        }
        this.json = json;
        setTurnInstruction(parseTurnInstruction(json));
        setDistance(json.getInt(2));
    }

    /**
     * Used for testing. Do not remove.
     */
    @SuppressWarnings("unused")
    protected Instruction() {
    }

    public int getTurnInstruction() {
        return turn;
    }

    public void setTurnInstruction(int turn) {
        this.turn = turn;
    }

    public String getHumanTurnInstruction() {
        return decodedInstructions[turn];
    }

    public String getName() {
        return json.getString(1);
    }

    public int getDistance() {
        return distanceInMeters;
    }

    public int getRemainingDistance(Location currentLocation) {
        return getDistance() - Math.round(currentLocation.distanceTo(location));
    }

    public void setDistance(int distanceInMeters) {
        this.distanceInMeters = distanceInMeters;
    }

    public String getFormattedDistance() {
        return DistanceFormatter.format(distanceInMeters);
    }

    public String getDirection() {
        return json.getString(6);
    }

    public int getPolygonIndex() {
        return json.getInt(3);
    }

    public float getDirectionAngle() {
        String direction = getDirection();
        float angle = 0;
        if (direction.equals("NE")) {
            angle = 315.0f;
        } else if (direction.equals("E")) {
            angle = 270.0f;
        } else if (direction.equals("SE")) {
            angle = 225.0f;
        } else if (direction.equals("S")) {
            angle = 180.0f;
        } else if (direction.equals("SW")) {
            angle = 135.0f;
        } else if (direction.equals("W")) {
            angle = 90.0f;
        } else if (direction.equals("NW")) {
            angle = 45.0f;
        }
        return angle;
    }

    public int getRotationBearing() {
        return 360 - json.getInt(7);
    }

    public int getBearing() {
        return json.getInt(7);
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    private String getFullInstructionBeforePattern() {
        String controllingGluePhrase = "and continue on for";
        String pattern = "%s %s " + controllingGluePhrase + " %s";
        if (getHumanTurnInstruction().equals(HEAD_ON) ||
                getHumanTurnInstruction().equals(GO_STRAIGHT)) {
            controllingGluePhrase = "for";
            pattern = "%s %s " + controllingGluePhrase + " %s";
        } else if (getHumanTurnInstruction().equals(YOU_HAVE_ARRIVED)) {
            pattern = "%s %s";
        }
        return pattern;
    }

    public String getFullInstruction() {
        return getFullInstructionBeforeAction();
    }

    public String getFullInstructionBeforeAction() {
        return String.format(Locale.US,
                getFullInstructionBeforePattern(),
                getHumanTurnInstruction(),
                getName(),
                DistanceFormatter.format(distanceInMeters, true));
    }

    public String getFullInstructionAfterAction() {
        if (getHumanTurnInstruction().equals(YOU_HAVE_ARRIVED)) {
            return getFullInstructionBeforeAction();
        }
        String pattern = "Continue on %s for %s";
        return String.format(Locale.US, pattern, getName(), DistanceFormatter.format(getDistance(),
                false));
    }

    public String getFullInstructionAfterAction(Location currentLocation) {
        if (getHumanTurnInstruction().equals(YOU_HAVE_ARRIVED)) {
            return getFullInstructionBeforeAction();
        }
        String pattern = "Continue on %s for %s";
        return String.format(Locale.US, pattern, getName(),
                DistanceFormatter.format(getRemainingDistance(currentLocation), false));
    }

    public String getSimpleInstruction() {
        return String.format(Locale.US, "%s %s", getHumanTurnInstruction(), getName());
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "Instruction: (%.5f, %.5f) %s %s LiveDistanceTo: %d",
                location.getLatitude(), location.getLongitude(), getHumanTurnInstruction(),
                getName(), liveDistanceToNext);
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass() != obj.getClass()) {
            return false;
        }
        Instruction other = (Instruction) obj;
        return (getTurnInstruction() == other.getTurnInstruction()
                && getBearing() == other.getBearing()
                && getLocation().getLatitude() == other.getLocation().getLatitude()
                && getLocation().getLongitude() == other.getLocation().getLongitude());
    }

    private int parseTurnInstruction(JSONArray json) {
        String turn = json.getString(0);
        String[] split = turn.split("-");
        return Integer.valueOf(split[0]);
    }

    public String getSimpleInstructionAfterAction() {
        if (getHumanTurnInstruction().equals(YOU_HAVE_ARRIVED)) {
            return getFullInstructionBeforeAction();
        }
        String pattern = "Continue on %s";
        return String.format(Locale.US, pattern, getName(), false);
    }
}
