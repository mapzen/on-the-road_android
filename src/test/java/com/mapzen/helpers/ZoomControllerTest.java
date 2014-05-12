package com.mapzen.helpers;

import org.junit.Before;
import org.junit.Test;

import static com.mapzen.helpers.ZoomController.DEFAULT_ZOOM_BIKING;
import static com.mapzen.helpers.ZoomController.DEFAULT_ZOOM_DRIVING;
import static com.mapzen.helpers.ZoomController.DEFAULT_ZOOM_WALKING;
import static com.mapzen.helpers.ZoomController.DrivingSpeed.MPH_0_TO_15;
import static com.mapzen.helpers.ZoomController.DrivingSpeed.MPH_15_TO_25;
import static com.mapzen.helpers.ZoomController.DrivingSpeed.MPH_25_TO_35;
import static com.mapzen.helpers.ZoomController.DrivingSpeed.MPH_35_TO_50;
import static com.mapzen.helpers.ZoomController.DrivingSpeed.MPH_OVER_50;
import static com.mapzen.helpers.ZoomController.milesPerHourToMetersPerSecond;
import static com.mapzen.osrm.Router.Type.BIKING;
import static com.mapzen.osrm.Router.Type.DRIVING;
import static com.mapzen.osrm.Router.Type.WALKING;
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

    @Test
    public void currentDrivingSpeedZero_shouldReturnZoomForZeroTo15Mph() throws Exception {
        controller.setCurrentSpeed(0);
        controller.setTransitMode(DRIVING);
        controller.setDrivingZoom(15, MPH_0_TO_15);
        assertThat(controller.getZoom()).isEqualTo(15);
    }

    @Test
    public void currentDrivingSpeed20_shouldReturnZoomFor15To25Mph() throws Exception {
        controller.setCurrentSpeed(milesPerHourToMetersPerSecond(20));
        controller.setTransitMode(DRIVING);
        controller.setDrivingZoom(14, MPH_15_TO_25);
        assertThat(controller.getZoom()).isEqualTo(14);
    }

    @Test
    public void currentDrivingSpeed30_shouldReturnZoomFor25To35Mph() throws Exception {
        controller.setCurrentSpeed(milesPerHourToMetersPerSecond(30));
        controller.setTransitMode(DRIVING);
        controller.setDrivingZoom(13, MPH_25_TO_35);
        assertThat(controller.getZoom()).isEqualTo(13);
    }

    @Test
    public void currentDrivingSpeed40_shouldReturnZoomFor35To50Mph() throws Exception {
        controller.setCurrentSpeed(milesPerHourToMetersPerSecond(40));
        controller.setTransitMode(DRIVING);
        controller.setDrivingZoom(12, MPH_35_TO_50);
        assertThat(controller.getZoom()).isEqualTo(12);
    }

    @Test
    public void currentDrivingSpeed50_shouldReturnZoomForOver50Mph() throws Exception {
        controller.setCurrentSpeed(milesPerHourToMetersPerSecond(50));
        controller.setTransitMode(DRIVING);
        controller.setDrivingZoom(11, MPH_OVER_50);
        assertThat(controller.getZoom()).isEqualTo(11);
    }
}
