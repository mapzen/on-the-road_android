package com.mapzen.helpers;

import com.mapzen.model.ValhallaLocation;
import com.mapzen.valhalla.Instruction;
import com.mapzen.valhalla.Route;

import java.util.ArrayList;

import static com.mapzen.helpers.DistanceFormatter.METERS_IN_ONE_MILE;

/**
 * In-route navigation state machine. Accepts raw location updates, attempts to snap them to the
 * route, and notifies listener of significant routing events.
 */
public class RouteEngine {
    public static final int APPROACH_RADIUS = 50;
    public static final int ALERT_RADIUS = 100;
    public static final int DESTINATION_RADIUS = 30;

    public enum RouteState {
        PRE_INSTRUCTION,
        INSTRUCTION,
        COMPLETE,
        LOST
    }

    public enum Milestone {
        TWO_MILE,
        ONE_MILE,
        QUARTER_MILE
    }

    private Route route;
    private RouteState routeState;
    private RouteListener listener;
    private ValhallaLocation location;
    private ValhallaLocation snapLocation;
    private Instruction currentInstruction;
    private ArrayList<Instruction> instructions;
    private Milestone lastMilestoneUpdate;

    /**
     * Sets {@link RouteEngine#location} and snaps it to the {@link RouteEngine#route}. Checks that
     * {@link RouteEngine#routeState} is not {@link RouteState#COMPLETE} or {@link RouteState#LOST}
     * before notifying the listener of any milestones, approach instructions, or completed
     * instructions
     *
     */
    public void onLocationChanged(final ValhallaLocation location) {
        if (routeState == RouteState.COMPLETE) {
            return;
        }

        this.location = location;
        snapLocation();

        if (routeState == RouteState.COMPLETE) {
            listener.onUpdateDistance(0, 0);
        } else {
            listener.onUpdateDistance(route.getDistanceToNextInstruction(),
                    route.getRemainingDistanceToDestination());
        }

        if (routeState == RouteState.LOST) {
            return;
        }

        checkApproachMilestone(Milestone.TWO_MILE, METERS_IN_ONE_MILE * 2);
        checkApproachMilestone(Milestone.ONE_MILE, METERS_IN_ONE_MILE);
        checkApproachMilestone(Milestone.QUARTER_MILE, METERS_IN_ONE_MILE / 4);

        if (routeState == RouteState.PRE_INSTRUCTION
                && route.getDistanceToNextInstruction() < ALERT_RADIUS
                && route.getNextInstructionIndex() != null) {
            final int nextIndex = route.getNextInstructionIndex();
            listener.onApproachInstruction(nextIndex);
            routeState = RouteState.INSTRUCTION;
            lastMilestoneUpdate = null;
        }

        final Instruction nextInstruction = route.getNextInstruction();

        if (instructions != null && !currentInstruction.equals(nextInstruction)) {
            routeState = RouteState.PRE_INSTRUCTION;
            int nextIndex = instructions.indexOf(currentInstruction);
            listener.onInstructionComplete(nextIndex);
        }

        currentInstruction = route.getNextInstruction();
    }

    private void checkApproachMilestone(Milestone milestone, double distance) {
        if (routeState == RouteState.PRE_INSTRUCTION
                && Math.abs(route.getDistanceToNextInstruction() - distance) < APPROACH_RADIUS
                && lastMilestoneUpdate != milestone
                && route.getNextInstructionIndex() != null) {
            final int nextIndex = route.getNextInstructionIndex();
            listener.onMilestoneReached(nextIndex, milestone);
            lastMilestoneUpdate = milestone;
        }
    }

    /**
     * Snap {@link RouteEngine#route} to {@link RouteEngine#location},
     * call listener method, and update {@link RouteEngine#routeState} if arrived or if lost
     */
    private void snapLocation() {
        snapLocation = route.snapToRoute(location);

        if (snapLocation != null) {
            listener.onSnapLocation(location, snapLocation);
        }

        if (youHaveArrived()) {
            routeState = RouteState.COMPLETE;
            listener.onRouteComplete();
        }

        if (route.isLost()) {
            routeState = RouteState.LOST;
            listener.onRecalculate(location);
        }
    }

    private boolean youHaveArrived() {
        return getLocationForDestination() != null
                && snapLocation != null
                && snapLocation.distanceTo(getLocationForDestination()) < DESTINATION_RADIUS;
    }

    private ValhallaLocation getLocationForDestination() {
        if (route.getRouteInstructions() == null) {
            return null;
        }

        final int destinationIndex = route.getRouteInstructions().size() - 1;
        return route.getRouteInstructions().get(destinationIndex).getLocation();
    }

    /**
     * Sets the route for engine, gets route instructions, calls listener method to notify that the
     * route has started, and updates route state to {@link RouteState.PRE_INSTRUCTION}
     *
     * Listener must be set before calling this or IllegalStateException is thrown
     * @param route
     */
    public void setRoute(Route route) {
        if (listener == null) {
            throw new IllegalStateException("Route listener is null");
        }

        this.route = route;
        instructions = route.getRouteInstructions();
        if (instructions != null) {
            currentInstruction = instructions.get(0);
        }

        listener.onRouteStart();
        routeState = RouteState.PRE_INSTRUCTION;
    }

    public Route getRoute() {
        return route;
    }

    /**
     * There must be a listener to call {@link #setRoute(Route)} on the engine
     * @param listener
     */
    public void setListener(RouteListener listener) {
        this.listener = listener;
    }
}
