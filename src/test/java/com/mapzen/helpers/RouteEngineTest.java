package com.mapzen.helpers;

import com.mapzen.model.Location;
import com.mapzen.valhalla.Instruction;
import com.mapzen.valhalla.Route;
import com.mapzen.valhalla.RouteTest;

import org.fest.assertions.data.Offset;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static com.mapzen.helpers.DistanceFormatter.METERS_IN_ONE_MILE;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class RouteEngineTest {
    private RouteEngine routeEngine;
    private Route route;
    private TestRouteListener listener;

    @Before
    public void setUp() throws Exception {
        route = RouteTest.getRoute("ace_hotel_valhalla");
        listener = new TestRouteListener();
        routeEngine = new RouteEngine();
        routeEngine.setListener(listener);
        routeEngine.setRoute(route);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(routeEngine).isNotNull();
    }

    @Test(expected = IllegalStateException.class)
    public void setRoute_shouldCheckForListener() throws Exception {
        routeEngine.setListener(null);
        routeEngine.setRoute(route);
    }

    @Test
    public void setRoute_shouldNotifyOnRouteStart() throws Exception {
        assertThat(listener.started).isTrue();
    }

    @Test
    public void onRecalculate_shouldNotifyWhenLost() throws Exception {
        routeEngine.onLocationChanged(route.getRouteInstructions().get(0).getLocation());
        routeEngine.onLocationChanged(getTestLocation(0, 0));
        assertThat(listener.recalculating).isTrue();
    }

    @Test
    public void onSnapLocation_shouldReturnCorrectedLocation() throws Exception {
        Location location = getTestLocation(40.7444114, -73.9904202);
        routeEngine.onLocationChanged(location);
        assertThat(listener.originalLocation).isEqualsToByComparingFields(location);
        assertThat(listener.snapLocation).isEqualsToByComparingFields(route.snapToRoute(location));
    }

    @Test
    public void onApproachInstruction_shouldReturnIndex() throws Exception {
        TestRoute route = new TestRoute();
        route.distanceToNextInstruction = METERS_IN_ONE_MILE;
        route.distanceToNextInstruction = RouteEngine.ALERT_RADIUS - 1;
        routeEngine.setRoute(route);
        routeEngine.onLocationChanged(getTestLocation());
        assertThat(listener.approachIndex).isEqualTo(1);
    }

    @Test
    public void onApproachInstruction_shouldNotFireForDestination() throws Exception {
        routeEngine.onLocationChanged(route.getRouteInstructions().get(3).getLocation());
        assertThat(listener.approachIndex).isNotEqualTo(3);
    }

    @Test
    public void onApproachInstruction_shouldNotFireAtStart() throws Exception {
        routeEngine.onLocationChanged(route.getRouteInstructions().get(0).getLocation());
        assertThat(listener.approachIndex).isEqualTo(-1);
    }

    @Test
    public void onInstructionComplete_shouldReturnIndex() throws Exception {
        routeEngine.onLocationChanged(route.getRouteInstructions().get(0).getLocation());
        routeEngine.onLocationChanged(route.getRouteInstructions().get(1).getLocation());
        assertThat(listener.completeIndex).isEqualTo(1);
    }

    @Test
    public void onUpdateDistance_shouldReturnDistanceToNextInstruction() throws Exception {
        routeEngine.onLocationChanged(route.getRouteInstructions().get(0).getLocation());
        assertThat((double) listener.distanceToNextInstruction)
                .isEqualTo(route.getRouteInstructions().get(0).getDistance(), Offset.offset(1.0));
    }

    @Test
    public void onUpdateDistance_shouldHaveFullDistanceToDestinationAtStart() throws Exception {
        routeEngine.onLocationChanged(route.getRouteInstructions().get(0).getLocation());
        assertThat((double) listener.distanceToDestination)
                .isEqualTo((double) route.getTotalDistance(), Offset.offset(1.0));
    }

    @Test
    public void onUpdateDistance_shouldHaveZeroDistanceToNextInstructionAtStart() throws Exception {
        routeEngine.onLocationChanged(route.getRouteInstructions().get(0).getLocation());
        assertThat((double) listener.distanceToNextInstruction)
                .isEqualTo(route.getRouteInstructions().get(0).getDistance(), Offset.offset(1.0));
    }

    @Test
    public void onUpdateDistance_shouldCountdownDistanceToDestinationAtTurn() throws Exception {
        routeEngine.onLocationChanged(route.getRouteInstructions().get(0).getLocation());
        routeEngine.onLocationChanged(route.getRouteInstructions().get(1).getLocation());
        assertThat((double) listener.distanceToDestination).isEqualTo((double)
                        (route.getTotalDistance() - route.getRouteInstructions().get(0)
                                .getDistance()), Offset.offset(2.0));
    }

    @Test
    public void onUpdateDistance_shouldCountdownInstructionDistance() throws Exception {
        Location location = getTestLocation(40.743810, -73.989053); // 26th & Broadway
        routeEngine.onLocationChanged(route.getRouteInstructions().get(0).getLocation());
        routeEngine.onLocationChanged(location);

        int expected = route.getDistanceToNextInstruction();
        assertThat(listener.distanceToNextInstruction).isEqualTo(expected);
    }

    @Test
    public void onUpdateDistance_shouldCountdownDistanceToDestinationAlongRoute() throws Exception {
        Instruction instruction = route.getRouteInstructions().get(0);
        Location location = getTestLocation(40.743810, -73.989053); // 26th & Broadway
        routeEngine.onLocationChanged(instruction.getLocation());
        routeEngine.onLocationChanged(location);

        Location snapLocation = route.snapToRoute(location);
        Location nextInstruction = route.getRouteInstructions().get(1).getLocation();
        int distanceToNextInstruction = (int) snapLocation.distanceTo(nextInstruction);
        int expected = route.getTotalDistance() - instruction.getDistance()
                + distanceToNextInstruction;
        assertThat((double) listener.distanceToDestination).isEqualTo(expected, Offset.offset(2.0));
    }

    @Test
    public void onUpdateDistance_shouldFireWhenLost() throws Exception {
        routeEngine.onLocationChanged(getTestLocation(0, 0));
        assertThat((double) listener.distanceToDestination)
                .isEqualTo(route.getTotalDistance(), Offset.offset(1.0));
    }

    @Test
    public void onUpdateDistance_shouldReturnZeroAtDestination() throws Exception {
        int size = route.getRouteInstructions().size();
        routeEngine.onLocationChanged(route.getRouteInstructions().get(size - 1).getLocation());
        assertThat(listener.distanceToNextInstruction).isEqualTo(0);
        assertThat(listener.distanceToDestination).isEqualTo(0);
    }

    @Test
    public void onRouteComplete_shouldTriggerAtDestination() throws Exception {
        routeEngine.onLocationChanged(route.getRouteInstructions().get(3).getLocation());
        assertThat(listener.routeComplete).isTrue();
    }

    @Test
    public void onRouteComplete_shouldOnlyTriggerOnce() throws Exception {
        routeEngine.onLocationChanged(route.getRouteInstructions().get(1).getLocation());
        routeEngine.onLocationChanged(route.getRouteInstructions().get(2).getLocation());
        routeEngine.onLocationChanged(route.getRouteInstructions().get(3).getLocation());
        listener.routeComplete = false;
        routeEngine.onLocationChanged(route.getRouteInstructions().get(3).getLocation());
        assertThat(listener.routeComplete).isFalse();
    }

    @Test
    public void onMilestoneReached_shouldNotifyAtOneMile() throws Exception {
        TestRoute route = new TestRoute();
        route.distanceToNextInstruction = METERS_IN_ONE_MILE;
        routeEngine.setRoute(route);
        routeEngine.onLocationChanged(getTestLocation());
        assertThat(listener.milestoneIndex).isEqualTo(1);
        assertThat(listener.milestone).isEqualTo(RouteEngine.Milestone.ONE_MILE);
    }

    @Test
    public void onMilestoneReached_shouldNotifyAtQuarterMile() throws Exception {
        TestRoute route = new TestRoute();
        route.distanceToNextInstruction = METERS_IN_ONE_MILE / 4;
        routeEngine.setRoute(route);
        routeEngine.onLocationChanged(getTestLocation());
        assertThat(listener.milestoneIndex).isEqualTo(1);
        assertThat(listener.milestone).isEqualTo(RouteEngine.Milestone.QUARTER_MILE);
    }

    @Test
    public void onMilestoneReached_shouldNotNotifyTwiceForOneMile() throws Exception {
        TestRoute route = new TestRoute();
        route.distanceToNextInstruction = METERS_IN_ONE_MILE;
        routeEngine.setRoute(route);
        routeEngine.onLocationChanged(getTestLocation());
        listener.milestoneIndex = -1;
        routeEngine.onLocationChanged(getTestLocation());
        assertThat(listener.milestoneIndex).isEqualTo(-1);
    }

    @Test
    public void onMilestoneReached_shouldNotNotifyTwiceForQuarterMile() throws Exception {
        TestRoute route = new TestRoute();
        route.distanceToNextInstruction = METERS_IN_ONE_MILE / 4;
        routeEngine.setRoute(route);
        routeEngine.onLocationChanged(getTestLocation());
        listener.milestoneIndex = -1;
        routeEngine.onLocationChanged(getTestLocation());
        assertThat(listener.milestoneIndex).isEqualTo(-1);
    }

    private static class TestRoute extends Route {
        private double distanceToNextInstruction = 0;

        public TestRoute() {
            super(new JSONObject());
        }

        @Nullable @Override public Location snapToRoute(@NotNull Location originalPoint) {
            return getTestLocation();
        }

        @Override public boolean isLost() {
            return false;
        }

        @Override public int getDistanceToNextInstruction() {
            return (int) distanceToNextInstruction;
        }

        @Nullable @Override public Integer getNextInstructionIndex() {
            return 1;
        }

        @Override public int getRemainingDistanceToDestination() {
            return 0;
        }

        @Nullable @Override public Instruction getNextInstruction() {
            return null;
        }
    }

    private static class TestRouteListener implements RouteListener {
        private Location originalLocation;
        private Location snapLocation;

        private boolean started = false;
        private boolean recalculating = false;
        private int milestoneIndex = -1;
        private int approachIndex = -1;
        private int completeIndex = -1;
        private int distanceToNextInstruction = -1;
        private int distanceToDestination = -1;
        private boolean routeComplete = false;
        private RouteEngine.Milestone milestone;

        @Override
        public void onRouteStart() {
            started = true;
        }

        @Override
        public void onRecalculate(Location location) {
            recalculating = true;
        }

        @Override
        public void onSnapLocation(Location originalLocation, Location snapLocation) {
            this.originalLocation = originalLocation;
            this.snapLocation = snapLocation;
        }

        @Override
        public void onMilestoneReached(int index, RouteEngine.Milestone milestone) {
            milestoneIndex = index;
            this.milestone = milestone;
        }

        @Override
        public void onApproachInstruction(int index) {
            approachIndex = index;
        }

        @Override
        public void onInstructionComplete(int index) {
            completeIndex = index;
        }

        @Override
        public void onUpdateDistance(int distanceToNextInstruction, int distanceToDestination) {
            this.distanceToNextInstruction = distanceToNextInstruction;
            this.distanceToDestination = distanceToDestination;
        }

        @Override
        public void onRouteComplete() {
            routeComplete = true;
        }
    }

    public static Location getTestLocation() {
        return getTestLocation(0, 0);
    }

    public static Location getTestLocation(double lat, double lng) {
        Location location = new Location("testing");
        location.setLatitude(lat);
        location.setLongitude(lng);
        return location;
    }
}
