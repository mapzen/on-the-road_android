package com.mapzen.helpers;

import org.junit.Before;
import org.junit.Test;

import static com.mapzen.helpers.ZoomController.DEFAULT_TURN_RADIUS;
import static com.mapzen.helpers.ZoomController.DEFAULT_TURN_RADIUS_BIKING;
import static com.mapzen.helpers.ZoomController.DEFAULT_TURN_RADIUS_DRIVING;
import static com.mapzen.helpers.ZoomController.DEFAULT_TURN_RADIUS_WALKING;
import static com.mapzen.helpers.ZoomController.DEFAULT_ZOOM_BIKING;
import static com.mapzen.helpers.ZoomController.DEFAULT_ZOOM_DRIVING;
import static com.mapzen.helpers.ZoomController.DEFAULT_ZOOM_WALKING;
import static com.mapzen.helpers.ZoomController.DrivingSpeed.MPH_0_TO_15;
import static com.mapzen.helpers.ZoomController.DrivingSpeed.MPH_15_TO_25;
import static com.mapzen.helpers.ZoomController.DrivingSpeed.MPH_25_TO_35;
import static com.mapzen.helpers.ZoomController.DrivingSpeed.MPH_35_TO_50;
import static com.mapzen.helpers.ZoomController.DrivingSpeed.MPH_OVER_50;
import static com.mapzen.helpers.ZoomController.milesPerHourToMetersPerSecond;
import static com.mapzen.valhalla.Router.Type.BIKING;
import static com.mapzen.valhalla.Router.Type.DRIVING;
import static com.mapzen.valhalla.Router.Type.WALKING;
import static org.fest.assertions.api.Assertions.assertThat;

public class ZoomControllerTest {
    private ZoomController controller;

    @Before
    public void setUp() throws Exception {
        controller = new ZoomController();
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(controller).isNotNull();
    }

    @Test
    public void shouldReturnDefaultZoom() throws Exception {
        assertThat(controller.getZoom()).isEqualTo(DEFAULT_ZOOM_DRIVING);
    }

    @Test
    public void shouldReturnDefaultZoomWhileWalking() throws Exception {
        controller.setTransitMode(WALKING);
        assertThat(controller.getZoom()).isEqualTo(DEFAULT_ZOOM_WALKING);
    }

    @Test
    public void shouldReturnDefaultZoomWhileBiking() throws Exception {
        controller.setTransitMode(BIKING);
        assertThat(controller.getZoom()).isEqualTo(DEFAULT_ZOOM_BIKING);
    }

    @Test
    public void shouldReturnDefaultZoomWhileDriving() throws Exception {
        controller.setTransitMode(DRIVING);
        assertThat(controller.getZoom()).isEqualTo(DEFAULT_ZOOM_DRIVING);
    }

    @Test
    public void shouldReturnNewZoomWhileWalking() throws Exception {
        controller.setWalkingZoom(15);
        controller.setTransitMode(WALKING);
        assertThat(controller.getZoom()).isEqualTo(15);
    }

    @Test
    public void shouldReturnNewZoomWhileBiking() throws Exception {
        controller.setBikingZoom(15);
        controller.setTransitMode(BIKING);
        assertThat(controller.getZoom()).isEqualTo(15);
    }

    @Test
    public void shouldReturnNewZoomWhileDriving() throws Exception {
        controller.setDrivingZoom(15);
        controller.setTransitMode(DRIVING);
        assertThat(controller.getZoom()).isEqualTo(15);
    }

    @Test(expected = IllegalArgumentException.class)
    public void currentDrivingSpeedLessThanZero_shouldThrowError() throws Exception {
        controller.setCurrentSpeed(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void averageDrivingSpeedLessThanZero_shouldThrowError() throws Exception {
        controller.setAverageSpeed(-1);
    }

    @Test
    public void averageDrivingSpeedZero_shouldReturnZoomForZeroTo15Mph() throws Exception {
        controller.setAverageSpeed(0);
        controller.setTransitMode(DRIVING);
        controller.setDrivingZoom(15, MPH_0_TO_15);
        assertThat(controller.getZoom()).isEqualTo(15);
    }

    @Test
    public void averageDrivingSpeed20_shouldReturnZoomFor15To25Mph() throws Exception {
        controller.setAverageSpeed(milesPerHourToMetersPerSecond(20));
        controller.setTransitMode(DRIVING);
        controller.setDrivingZoom(14, MPH_15_TO_25);
        assertThat(controller.getZoom()).isEqualTo(14);
    }

    @Test
    public void averageDrivingSpeed30_shouldReturnZoomFor25To35Mph() throws Exception {
        controller.setAverageSpeed(milesPerHourToMetersPerSecond(30));
        controller.setTransitMode(DRIVING);
        controller.setDrivingZoom(13, MPH_25_TO_35);
        assertThat(controller.getZoom()).isEqualTo(13);
    }

    @Test
    public void averageDrivingSpeed40_shouldReturnZoomFor35To50Mph() throws Exception {
        controller.setAverageSpeed(milesPerHourToMetersPerSecond(40));
        controller.setTransitMode(DRIVING);
        controller.setDrivingZoom(12, MPH_35_TO_50);
        assertThat(controller.getZoom()).isEqualTo(12);
    }

    @Test
    public void averageDrivingSpeed50_shouldReturnZoomForOver50Mph() throws Exception {
        controller.setAverageSpeed(milesPerHourToMetersPerSecond(50));
        controller.setTransitMode(DRIVING);
        controller.setDrivingZoom(11, MPH_OVER_50);
        assertThat(controller.getZoom()).isEqualTo(11);
    }

    @Test
    public void shouldReturnDefaultTurnRadius() throws Exception {
        assertThat(controller.getTurnRadius()).isEqualTo(DEFAULT_TURN_RADIUS);
    }

    @Test
    public void shouldReturnDefaultTurnRadiusWhileWalking() throws Exception {
        controller.setTransitMode(WALKING);
        assertThat(controller.getTurnRadius()).isEqualTo(DEFAULT_TURN_RADIUS_WALKING);
    }

    @Test
    public void shouldReturnDefaultTurnRadiusWhileBiking() throws Exception {
        controller.setTransitMode(BIKING);
        assertThat(controller.getTurnRadius()).isEqualTo(DEFAULT_TURN_RADIUS_BIKING);
    }

    @Test
    public void shouldReturnDefaultTurnRadiusWhileDriving() throws Exception {
        controller.setTransitMode(DRIVING);
        assertThat(controller.getTurnRadius()).isEqualTo(DEFAULT_TURN_RADIUS_DRIVING);
    }

    @Test
    public void shouldReturnNewTurnRadiusWhileWalking() throws Exception {
        controller.setWalkingTurnRadius(5);
        controller.setTransitMode(WALKING);
        assertThat(controller.getTurnRadius()).isEqualTo(5);
    }

    @Test
    public void shouldReturnNewTurnRadiusWhileBiking() throws Exception {
        controller.setBikingTurnRadius(10);
        controller.setTransitMode(BIKING);
        assertThat(controller.getTurnRadius()).isEqualTo(10);
    }

    @Test
    public void shouldReturnNewTurnRadiusWhileDriving() throws Exception {
        controller.setDrivingTurnRadius(15);
        controller.setTransitMode(DRIVING);
        assertThat(controller.getTurnRadius()).isEqualTo(15);
    }

    @Test
    public void currentDrivingSpeedZero_shouldReturnTurnRadiusForZeroTo15Mph() throws Exception {
        controller.setCurrentSpeed(0);
        controller.setTransitMode(DRIVING);
        controller.setDrivingTurnRadius(60, MPH_0_TO_15);
        assertThat(controller.getTurnRadius()).isEqualTo(60);
    }

    @Test
    public void currentDrivingSpeed20_shouldReturnTurnRadiusFor15To25Mph() throws Exception {
        controller.setCurrentSpeed(milesPerHourToMetersPerSecond(20));
        controller.setTransitMode(DRIVING);
        controller.setDrivingTurnRadius(70, MPH_15_TO_25);
        assertThat(controller.getTurnRadius()).isEqualTo(70);
    }

    @Test
    public void currentDrivingSpeed30_shouldReturnTurnRadiusFor25To35Mph() throws Exception {
        controller.setCurrentSpeed(milesPerHourToMetersPerSecond(30));
        controller.setTransitMode(DRIVING);
        controller.setDrivingTurnRadius(80, MPH_25_TO_35);
        assertThat(controller.getTurnRadius()).isEqualTo(80);
    }

    @Test
    public void currentDrivingSpeed40_shouldReturnTurnRadiusFor35To50Mph() throws Exception {
        controller.setCurrentSpeed(milesPerHourToMetersPerSecond(40));
        controller.setTransitMode(DRIVING);
        controller.setDrivingTurnRadius(90, MPH_35_TO_50);
        assertThat(controller.getTurnRadius()).isEqualTo(90);
    }

    @Test
    public void currentDrivingSpeed50_shouldReturnTurnRadiusForOver50Mph() throws Exception {
        controller.setCurrentSpeed(milesPerHourToMetersPerSecond(50));
        controller.setTransitMode(DRIVING);
        controller.setDrivingTurnRadius(100, MPH_OVER_50);
        assertThat(controller.getTurnRadius()).isEqualTo(100);
    }
}
