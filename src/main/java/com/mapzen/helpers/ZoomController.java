package com.mapzen.helpers;

import com.mapzen.osrm.Router;

import java.util.HashMap;

import static com.mapzen.osrm.Router.Type.DRIVING;

public class ZoomController {
    public static final int DEFAULT_ZOOM = 17;
    public static final int DEFAULT_ZOOM_WALKING = 21;
    public static final int DEFAULT_ZOOM_BIKING = 19;
    public static final int DEFAULT_ZOOM_DRIVING = 17;

    private static final float ONE_METER_PER_SECOND_IN_MILES_PER_HOUR = 2.23694f;

    private int walkingZoom = DEFAULT_ZOOM_WALKING;
    private int bikingZoom = DEFAULT_ZOOM_BIKING;
    private int drivingZoom = DEFAULT_ZOOM_DRIVING;

    private Router.Type transitMode = DRIVING;
    private DrivingSpeed currentDrivingSpeed = null;
    private HashMap<DrivingSpeed, Integer> speedMap = new HashMap<DrivingSpeed, Integer>();

    public int getZoom() {
        switch (transitMode) {
            case WALKING:
                return walkingZoom;
            case BIKING:
                return bikingZoom;
            case DRIVING:
                return getZoomForCurrentDrivingSpeed();
            default:
                return DEFAULT_ZOOM;
        }
    }

    private int getZoomForCurrentDrivingSpeed() {
        Integer zoomLevelForCurrentSpeed = speedMap.get(currentDrivingSpeed);
        if (zoomLevelForCurrentSpeed != null) {
            return zoomLevelForCurrentSpeed;
        }

        return drivingZoom;
    }

    public void setTransitMode(Router.Type transitMode) {
        this.transitMode = transitMode;
    }

    public void setWalkingZoom(int walkingZoom) {
        this.walkingZoom = walkingZoom;
    }

    public void setBikingZoom(int bikingZoom) {
        this.bikingZoom = bikingZoom;
    }

    public void setDrivingZoom(int drivingZoom) {
        this.drivingZoom = drivingZoom;
    }

    public void setDrivingZoom(int zoom, DrivingSpeed speed) {
        speedMap.put(speed, zoom);
    }

    public void setCurrentSpeed(float metersPerSecond) {
        if (metersPerSecond < 0) {
            throw new IllegalArgumentException("Speed less than zero is not permitted.");
        }

        float mph = metersPerSecondToMilesPerHour(metersPerSecond);
        if (mph < 15) {
            currentDrivingSpeed = DrivingSpeed.MPH_0_TO_15;
        } else if (mph < 25) {
            currentDrivingSpeed = DrivingSpeed.MPH_15_TO_25;
        } else if (mph < 35) {
            currentDrivingSpeed = DrivingSpeed.MPH_25_TO_35;
        } else if (mph < 50) {
            currentDrivingSpeed = DrivingSpeed.MPH_35_TO_50;
        } else {
            currentDrivingSpeed = DrivingSpeed.MPH_OVER_50;
        }
    }

    public static float metersPerSecondToMilesPerHour(float metersPerSecond) {
        return metersPerSecond * ONE_METER_PER_SECOND_IN_MILES_PER_HOUR;
    }

    public static float milesPerHourToMetersPerSecond(float milesPerHour) {
        return milesPerHour / ONE_METER_PER_SECOND_IN_MILES_PER_HOUR;
    }

    public enum DrivingSpeed {
        MPH_0_TO_15,
        MPH_15_TO_25,
        MPH_25_TO_35,
        MPH_35_TO_50,
        MPH_OVER_50
    }
}
