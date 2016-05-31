package com.mapzen.helpers;

import com.mapzen.model.Location;

/**
 * {@link RouteEngine} callback interface.
 */
public interface RouteListener {
    /**
     * Invoked at the beginning of a new route.
     */
    public void onRouteStart();

    /**
     * Invoked when the given location is off course and the route needs to be recalculated.
     */
    public void onRecalculate(Location location);

    /**
     * Invoked each time a raw location update is snapped to the on route position.
     */
    public void onSnapLocation(Location originalLocation, Location snapLocation);

    /**
     * Invoked to give advance warning of an upcoming maneuver at designated milestones.
     */
    public void onMilestoneReached(int index, RouteEngine.Milestone milestone);

    /**
     * Invoked when next instruction maneuver is imminent.
     */
    public void onApproachInstruction(int index);

    /**
     * Invoked after instruction maneuver is completed. (ex. finished making turn)
     */
    public void onInstructionComplete(int index);

    /**
     * Invoked when distance to next instruction and destination have been updated.
     */
    public void onUpdateDistance(int distanceToNextInstruction, int distanceToDestination);

    /**
     * Invoked upon arrival at the destination.
     */
    public void onRouteComplete();
}
