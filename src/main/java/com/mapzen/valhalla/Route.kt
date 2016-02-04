package com.mapzen.valhalla

import android.location.Location
import com.mapzen.helpers.GeometryHelper.getBearing
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Math.toRadians
import java.util.ArrayList
import java.util.HashSet

public open class Route {

    private val TAG = Route::class.java.simpleName

    companion object {
        public const val KEY_TRIP = "trip"
        public const val KEY_LEGS = "legs"
        public const val KEY_SHAPE = "shape"
        public const val KEY_MANEUVERS = "maneuvers"
        public const val KEY_UNITS = "units"
        public const val KEY_LENGTH = "length"
        public const val KEY_STATUS = "status"
        public const val KEY_TIME = "time"
        public const val KEY_LOCATIONS = "locations"
        public const val KEY_SUMMARY = "summary"
        public const val SNAP_PROVIDER: String = "snap"
        public const val CLOSE_TO_DESTINATION_THRESHOLD_METERS: Int = 20
        public const val CLOSE_TO_NEXT_LEG_THRESHOLD_METERS: Int = 5
        public const val LOST_THRESHOLD_METERS: Int = 50
        public const val CORRECTION_THRESHOLD_METERS: Int = 1000
        public const val CLOCKWISE_DEGREES: Double = 90.0
        public const val COUNTERCLOCKWISE_DEGREES: Double = -90.0
        public const val REVERSE_DEGREES: Int = 180
        public const val LOCATION_FUZZY_EQUAL_THRESHOLD_DEGREES: Double = 0.00001
    }

    public lateinit var rawRoute: JSONObject
    /**
     * Because https://valhalla.mapzen.com/route does not use http status codes, "status" key
     * in response can indicate too many requests in which case no poly line will be present
     */
    private var poly: ArrayList<Node>? = null
    /**
     * Because https://valhalla.mapzen.com/route does not use http status codes, "status" key
     * in response can indicate too many requests in which case no instructions will be present
     */
    private var instructions: ArrayList<Instruction>? = null
    public var units: Router.DistanceUnits = Router.DistanceUnits.KILOMETERS
    public var currentLeg: Int = 0
    private val seenInstructions = HashSet<Instruction>()
    private var lost: Boolean = false
    /**
     * Snapped location along route poly line
     */
    private var lastFixedLocation: Location? = null
    private var currentInstructionIndex: Int = 0
    public var totalDistanceTravelled: Double = 0.0
    private var beginningRouteLostThresholdMeters: Int? = null

    public constructor(jsonString: String) {
        setJsonObject(JSONObject(jsonString))
    }

    public constructor(jsonObject: JSONObject) {
        setJsonObject(jsonObject)
    }

    public fun setJsonObject(jsonObject: JSONObject) {
        this.rawRoute = jsonObject
        if (foundRoute()) {
            initializeDistanceUnits(jsonObject)
            initializePolyline(jsonObject.getJSONObject(KEY_TRIP).getJSONArray(KEY_LEGS).
                    getJSONObject(0).getString(KEY_SHAPE))
            initializeTurnByTurn(jsonObject.getJSONObject(KEY_TRIP).getJSONArray(KEY_LEGS).
                    getJSONObject(0).getJSONArray(KEY_MANEUVERS))
        }
    }

    private fun initializeDistanceUnits(jsonObject: JSONObject) {
        when (jsonObject.getJSONObject(KEY_TRIP).getString(KEY_UNITS)) {
            Router.DistanceUnits.KILOMETERS.toString() -> units = Router.DistanceUnits.KILOMETERS
            Router.DistanceUnits.MILES.toString() -> units = Router.DistanceUnits.MILES
        }
    }

    private fun initializePolyline(encoded: String): ArrayList<Node> {
        var lastNode: Node? = null
        poly = ArrayList<Node>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or ((b and 31) shl shift)
                shift += 5
            } while (b >= 32)
            val dlat = (if ((result and 1) != 0) (result shr 1).inv() else (result shr 1))
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or ((b and 31) shl shift)
                shift += 5
            } while (b >= 32)
            val dlng = (if ((result and 1) != 0) (result shr 1).inv() else (result shr 1))
            lng += dlng
            val x = lat.toDouble() / 1E6.toDouble()
            val y = lng.toDouble() / 1E6.toDouble()
            val node = Node(x, y)
            if (!poly!!.isEmpty()) {
                val lastElement = poly!![poly!!.size - 1]
                val distance = node.getLocation().distanceTo(lastElement.getLocation()).toDouble()
                val totalDistance = distance + lastElement.totalDistance
                node.totalDistance = totalDistance
                if (lastNode != null) {
                    lastNode.bearing = getBearing(lastNode.getLocation(), node.getLocation())
                }
                lastNode!!.legDistance = distance
            }

            lastNode = node
            poly!!.add(node)
        }
        return poly!!
    }

    private fun initializeTurnByTurn(instructions: JSONArray) {
        var gapDistance = 0
        this.instructions = ArrayList<Instruction>()
        for (i in 0..instructions.length() - 1) {
            val instruction = Instruction(instructions.getJSONObject(i), units)
            instruction.bearing = Math.ceil(poly!!.get(instruction.getBeginPolygonIndex()).bearing)
                    .toInt()
            var distance = instruction.distance
            distance += gapDistance
            instruction.distance = distance
            gapDistance = 0
            this.instructions!!.add(instruction)
        }
    }

    public open fun getTotalDistance(): Int {
        var distance = getSummary().getDouble(KEY_LENGTH)
        when (units) {
            Router.DistanceUnits.KILOMETERS -> distance *= Instruction.KM_TO_METERS
            Router.DistanceUnits.MILES -> distance *= Instruction.MI_TO_METERS
        }

        return Math.round(distance).toInt()
    }

    public open fun getStatus(): Int? {
        if (rawRoute.optJSONObject(KEY_TRIP) == null) {
            return -1
        }
        return rawRoute.optJSONObject(KEY_TRIP).getInt(KEY_STATUS)
    }

    public open fun foundRoute(): Boolean {
        return getStatus() == 0
    }

    public open fun getTotalTime(): Int {
        return getSummary().getInt(KEY_TIME)
    }

    public open fun getDistanceToNextInstruction(): Int {
        return getCurrentInstruction().liveDistanceToNext
    }

    public open fun getRemainingDistanceToDestination(): Int {
        return instructions!![instructions!!.size - 1].liveDistanceToNext
    }

    public open fun getRouteInstructions(): ArrayList<Instruction>? {
        if (instructions == null) {
            return null
        }
        var accumulatedDistance = 0
        for (instruction in instructions!!) {
            instruction.location = poly!![instruction.getBeginPolygonIndex()].getLocation()
            if (instruction.liveDistanceToNext < 0) {
                accumulatedDistance += instruction.distance
                instruction.liveDistanceToNext = accumulatedDistance
            }
        }
        return instructions
    }

    public open fun getGeometry(): ArrayList<Location> {
        val geometry = ArrayList<Location>()
        val polyline = poly
        if (polyline is ArrayList<Node>) {
            for (node in polyline) {
                geometry.add(node.getLocation())
            }
        }

        return geometry
    }

    public open fun getStartCoordinates(): Location {
        val location = Location(SNAP_PROVIDER)
        location.latitude = poly!![0].lat
        location.longitude = poly!![0].lng
        return location
    }

    public open fun isLost(): Boolean {
        return lost
    }

    private fun getViaPoints(): JSONArray {
        return rawRoute.getJSONObject(KEY_TRIP).getJSONArray(KEY_LOCATIONS)
    }

    private fun getSummary(): JSONObject {
        return rawRoute.getJSONObject(KEY_TRIP).getJSONObject(KEY_SUMMARY)
    }

    public open fun getCurrentRotationBearing(): Double {
        return 360 - poly!!.get(currentLeg).bearing
    }

    public open fun rewind() {
        currentLeg = 0
    }

    /**
     *  Takes current location and tries to snap it to a location along the route. If we are past
     *  the end of the poly line, consider user lost and don't return location to snap to. If we are
     *  close to destination, snap to the destination location. If we are close to the next leg of
     *  route, increment current leg and rerun this function otherwise get fixed location along
     *  route which is closest to user's current location. If user's location is within certain
     *  distance to route, snap to that location along path, otherwise consider user lost, dont
     *  snap to anything
     *
     *  @param currentLocation User's current location
     *  @return location along path that user's location is snapped to, or null if lost
     */
    public open fun snapToRoute(currentLocation: Location): Location? {
        val sizeOfPoly = poly!!.size

        // we are lost
        if (pastEndOfPoly()) {
            lost = true
            return null
        }

        // snap to destination location
        if (closeToDestination(currentLocation)) {
            val destination = poly!!.get(sizeOfPoly - 1)
            updateDistanceTravelled(destination)
            return destination.getLocation()
        }

        // snap currentNode's location to a location along the route, if we are close
        // to the next leg, go to next leg and then retry snapping
        val currentNode = poly!![currentLeg]
        lastFixedLocation = snapTo(currentNode, currentLocation)
        if (lastFixedLocation == null) {
            lastFixedLocation = currentNode.getLocation()
        } else {
            if (closeToNextLeg(currentNode.getLocation(), currentNode.legDistance)) {
                ++currentLeg
                updateCurrentInstructionIndex()
                return snapToRoute(currentLocation)
            }
        }

        if (beginningRouteLostThresholdMeters == null) {
            val distanceToFirstLoc = currentLocation.distanceTo(poly!![0].getLocation()).toInt()
            beginningRouteLostThresholdMeters = distanceToFirstLoc + LOST_THRESHOLD_METERS
        }

        // if we are close to the route, return snapped location on route, if we havent started
        // route and we arent close to another part of the route, dont consider user lost.
        // otherwise user is in middle of route but far from fixed location along route and
        // is therefore lost
        var distanceToRoute = currentLocation.distanceTo(lastFixedLocation).toDouble()
        if (distanceToRoute < LOST_THRESHOLD_METERS) {
            updateDistanceTravelled(currentNode)
            return lastFixedLocation
        } else if (totalDistanceTravelled == 0.0 && currentLeg == 0
                && distanceToRoute < beginningRouteLostThresholdMeters!!) {
            return currentLocation
        } else {
            lost = true
            return null
        }
    }

    private fun pastEndOfPoly(): Boolean {
        return currentLeg >= poly!!.size
    }

    /**
     * If the distance from {@param Location} to last node in poly is less than
     * {@link CLOSE_TO_DESTINATION_THRESHOLD} user is close to destination
     */
    private fun closeToDestination(location: Location): Boolean {
        val destination = poly!![poly!!.size - 1]
        val distanceToDestination = destination.getLocation().distanceTo(location).toDouble()
        return (Math.floor(distanceToDestination) < CLOSE_TO_DESTINATION_THRESHOLD_METERS)
    }

    /**
     * If the distance from this location to the last fixed location is almost the length of the
     * leg, then we are close to the next leg
     */
    private fun closeToNextLeg(location: Location, legDistance: Double): Boolean {
        return location.distanceTo(lastFixedLocation) >
                legDistance - CLOSE_TO_NEXT_LEG_THRESHOLD_METERS
    }

    private fun updateDistanceTravelled(current: Node) {
        totalDistanceTravelled = 0.0
        var tempDist: Double = 0.0
        for (i in 0..currentLeg - 1) {

            tempDist += poly!![i].legDistance
        }
        if (lastFixedLocation != null) {
            totalDistanceTravelled = Math.ceil(tempDist
                    + current.getLocation().distanceTo(lastFixedLocation).toDouble())
        }
        updateAllInstructions()
    }

    public open fun updateAllInstructions() {
        // this constructs a distance table
        // and calculates from it
        // 3 instruction has the distance of
        // first 3 combined
        var combined = 0
        for (instruction in instructions!!) {
            combined += instruction.distance
            val remaining = (combined) - Math.ceil(totalDistanceTravelled).toInt()
            instruction.liveDistanceToNext = remaining
        }
    }

    /**
     * Returns the closes location along the current route segment that the location should snap to
     *
     *  @param node Current node user is at along poly line (potentially near a turn along route)
     *  @param location Current location of user
     *  @return Location along route to snap to
     */
    private fun snapTo(node: Node, location: Location): Location {
        // if lat/lng of node and location are same, just update location's bearing to node
        // and snap to it
        if (fuzzyEqual(node.getLocation(), location)) {
            updateDistanceTravelled(node)
            location.bearing = node.bearing.toFloat()
            return location
        }

        var correctedLocation = snapTo(node, location, CLOCKWISE_DEGREES)
        if (correctedLocation == null) {
            correctedLocation = snapTo(node, location, COUNTERCLOCKWISE_DEGREES)
        }

        if (correctedLocation != null) {
            val distance = correctedLocation.distanceTo(location).toDouble()
            // check if results are on the otherside of the globe
            if (Math.round(distance) > CORRECTION_THRESHOLD_METERS) {
                val tmpNode = Node(node.lat, node.lng)
                tmpNode.bearing = node.bearing - REVERSE_DEGREES.toDouble()
                correctedLocation = snapTo(tmpNode, location, CLOCKWISE_DEGREES)
                if (correctedLocation == null) {
                    correctedLocation = snapTo(tmpNode, location, COUNTERCLOCKWISE_DEGREES)
                }
            }
        }

        val bearingDelta = node.bearing - node.getLocation().bearingTo(correctedLocation).toDouble()
        if (Math.abs(bearingDelta) > 10 && Math.abs(bearingDelta) < 350) {
            correctedLocation = node.getLocation()
        }

        correctedLocation?.bearing = node.getLocation().bearing
        return correctedLocation!!
    }

    /**
     * Uses haversine formula (http://www.movable-type.co.uk/scripts/latlong.html) to calculate
     * closest location along current route segment
     *
     * @param node Current node
     * @param location User's current location
     * @param degreeOffset Degrees to offset node bearing
     */
    private fun snapTo(node: Node, location: Location, degreeOffset: Double): Location? {
        val lat1 = toRadians(node.lat)
        val lon1 = toRadians(node.lng)
        val lat2 = toRadians(location.latitude)
        val lon2 = toRadians(location.longitude)

        val brng13 = toRadians(node.bearing)
        val brng23 = toRadians(node.bearing + degreeOffset)
        val dLat = lat2 - lat1
        var dLon = lon2 - lon1
        if (dLon == 0.0) {
            dLon = 0.001
        }

        val dist12 = 2 * Math.asin(Math.sqrt(Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2)))
        if (dist12 == 0.0) {
            return null
        }

        // initial/final bearings between points
        val brngA = Math.acos((Math.sin(lat2) - Math.sin(lat1) * Math.cos(dist12))
                / (Math.sin(dist12) * Math.cos(lat1)))

        val brngB = Math.acos((Math.sin(lat1) - Math.sin(lat2) * Math.cos(dist12))
                / (Math.sin(dist12) * Math.cos(lat2)))

        val brng12: Double
        val brng21: Double
        if (Math.sin(lon2 - lon1) > 0) {
            brng12 = brngA
            brng21 = 2 * Math.PI - brngB
        } else {
            brng12 = 2 * Math.PI - brngA
            brng21 = brngB
        }

        val alpha1 = (brng13 - brng12 + Math.PI) % (2 * Math.PI) - Math.PI  // angle 2-1-3
        val alpha2 = (brng21 - brng23 + Math.PI) % (2 * Math.PI) - Math.PI  // angle 1-2-3

        if (Math.sin(alpha1) == 0.0 && Math.sin(alpha2) == 0.0) {
            return null  // infinite intersections
        }
        if (Math.sin(alpha1) * Math.sin(alpha2) < 0) {
            return null       // ambiguous intersection
        }

        val alpha3 = Math.acos(-Math.cos(alpha1) * Math.cos(alpha2) + Math.sin(alpha1)
                * Math.sin(alpha2) * Math.cos(dist12))
        val dist13 = Math.atan2(Math.sin(dist12) * Math.sin(alpha1) * Math.sin(alpha2),
                Math.cos(alpha2) + Math.cos(alpha1) * Math.cos(alpha3))
        val lat3 = Math.asin(Math.sin(lat1) * Math.cos(dist13) + Math.cos(lat1)
                * Math.sin(dist13) * Math.cos(brng13))
        val dLon13 = Math.atan2(Math.sin(brng13) * Math.sin(dist13) * Math.cos(lat1),
                Math.cos(dist13) - Math.sin(lat1) * Math.sin(lat3))
        // normalise to -180..+180º
        val lon3 = ((lon1 + dLon13) + 3 * Math.PI) % (2 * Math.PI) - Math.PI

        val loc = Location(SNAP_PROVIDER)
        loc.latitude = Math.toDegrees(lat3)
        loc.longitude = Math.toDegrees(lon3)
        return loc
    }

    /**
     * Determine if these two locations are more or less the same to avoid doing extra calculations
     */
    private fun fuzzyEqual(l1: Location, l2: Location): Boolean {
        val deltaLat = Math.abs(l1.latitude - l2.latitude)
        val deltaLng = Math.abs(l1.longitude - l2.longitude)
        return (deltaLat <= LOCATION_FUZZY_EQUAL_THRESHOLD_DEGREES)
                && (deltaLng <= LOCATION_FUZZY_EQUAL_THRESHOLD_DEGREES)
    }

    public open fun getSeenInstructions(): Set<Instruction> {
        return seenInstructions
    }

    public open fun addSeenInstruction(instruction: Instruction) {
        seenInstructions.add(instruction)
    }

    public open fun getNextInstruction(): Instruction? {
        val nextInstructionIndex = currentInstructionIndex + 1
        if (nextInstructionIndex >= instructions!!.size) {
            return null
        } else {
            return instructions!![nextInstructionIndex]
        }
    }

    public open fun getNextInstructionIndex(): Int? {
        return instructions?.indexOf(getNextInstruction())
    }

    public open fun getCurrentInstruction(): Instruction {
        return instructions!![currentInstructionIndex]
    }

    private fun updateCurrentInstructionIndex() {
        val next = getNextInstruction()
        if (next == null) {
            return
        } else if (currentLeg >= next.getBeginPolygonIndex()) {
            currentInstructionIndex++
        }
    }

    public open fun getAccurateStartPoint(): Location {
        return poly!![0].getLocation()
    }

}
