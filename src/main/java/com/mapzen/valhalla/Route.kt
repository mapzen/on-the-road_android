package com.mapzen.valhalla

import com.f2prateek.ln.Ln

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import android.location.Location

import java.util.ArrayList
import java.util.HashSet

import com.mapzen.helpers.GeometryHelper.getBearing
import java.lang.Math.toRadians

public class Route {
    public val SNAP_PROVIDER: String = "snap"
    public val LOST_THRESHOLD: Int = 50
    public val CLOCKWISE: Int = 90
    public val COUNTERCLOCKWISE: Int = -90
    public val CORRECTION_THRESHOLD: Int = 1000
    public val REVERSE: Int = 180
    private var poly: ArrayList<Node>? = null
    private var instructions: ArrayList<Instruction>? = null
    public var rawRoute: JSONObject? = null
        private set
    public var currentLeg: Int = 0
        protected set
    private val seenInstructions = HashSet<Instruction>()
    private var lost: Boolean = false
    private var lastFixedPoint: Location? = null
    private var currentInstructionIndex: Int = 0
    public var totalDistanceTravelled: Double = 0.0
        private set

    throws(javaClass<JSONException>())
    public constructor(jsonString: String) {
        setJsonObject(JSONObject(jsonString))
    }

    public constructor(jsonObject: JSONObject) {
        setJsonObject(jsonObject)
    }

    public fun setJsonObject(jsonObject: JSONObject) {
        this.rawRoute = jsonObject
        if (foundRoute()) {
            initializePolyline(jsonObject.getJSONObject("trip").getJSONArray("legs").getJSONObject(0).getString("shape"))
            initializeTurnByTurn(jsonObject.getJSONObject("trip").getJSONArray("legs").getJSONObject(0).getJSONArray("maneuvers"))
        }
    }

    throws(javaClass())
    public fun getTotalDistance(): Int {
        return Math.round(poly!!.get(poly!!.size() - 1).totalDistance).toInt()
    }

    [throws(javaClass<JSONException>())]
    public fun getStatus(): Int? {
        if(rawRoute!!.optJSONObject("trip") == null) {
            return -1
        }
        return rawRoute!!.optJSONObject("trip").getInt("status")
    }

    throws(javaClass<JSONException>())
    public fun foundRoute(): Boolean {
        return getStatus() == 0
    }

    throws(javaClass<JSONException>())
    public fun getTotalTime(): Int {
        return getSummary().getInt("time")
    }

    public fun getDistanceToNextInstruction(): Int {
        return getCurrentInstruction().liveDistanceToNext
    }

    public fun getRemainingDistanceToDestination(): Int {
        return instructions!!.get(instructions!!.size() - 1).liveDistanceToNext
    }

    throws(javaClass<JSONException>())
    private fun initializeTurnByTurn(instructions: JSONArray) {
        var gapDistance = 0
        this.instructions = ArrayList<Instruction>()
        for (i in 0..instructions.length() - 1) {
            val instruction = Instruction(instructions.getJSONObject(i))
            instruction.bearing = Math.ceil(poly!!.get(instruction.getBeginPolygonIndex()).bearing).toInt()
            if (!instruction.skip()) {
                var distance = instruction.distance
                distance += gapDistance
                instruction.distance = distance
                gapDistance = 0
                this.instructions!!.add(instruction)
            } else {
                gapDistance += instruction.distance
            }
        }
    }

    throws(javaClass<JSONException>())
    public fun getRouteInstructions(): ArrayList<Instruction> {
        var accumulatedDistance = 0
        for (instruction in instructions!!) {
            instruction.location = poly!!.get(instruction.getBeginPolygonIndex()).getLocation()
            if (instruction.liveDistanceToNext < 0) {
                accumulatedDistance += instruction.distance
                instruction.liveDistanceToNext = accumulatedDistance
            }
        }
        return instructions!!    }


    public fun getGeometry(): ArrayList<Location> {
        val geometry = ArrayList<Location>()
        for (node in poly!!) {
            geometry.add(node.getLocation())
        }
        return geometry
    }

    throws(javaClass<JSONException>())
    public fun getStartCoordinates(): Location {
        val location = Location(SNAP_PROVIDER)
        location.setLatitude(poly!!.get(0).lat)
        location.setLongitude(poly!!.get(0).lng)
        return location
    }

    public fun isLost(): Boolean {
        return lost
    }

    throws(javaClass<JSONException>())
    private fun getViaPoints(): JSONArray {
        return rawRoute!!.getJSONObject("trip").getJSONArray("locations")
    }

    throws(javaClass<JSONException>())
    private fun getSummary(): JSONObject {
        return rawRoute!!.getJSONObject("trip").getJSONArray("legs").getJSONObject(0).getJSONObject("summary")
    }
    private fun initializePolyline(encoded: String): ArrayList<Node> {
        var lastNode: Node? = null
        if (poly == null) {
            poly = ArrayList<Node>()
            var index = 0
            val len = encoded.length()
            var lat = 0
            var lng = 0
            while (index < len) {
                var b: Int
                var shift = 0
                var result = 0
                do {
                    b = encoded.charAt(index++).toInt() - 63
                    result = result or ((b and 31) shl shift)
                    shift += 5
                } while (b >= 32)
                val dlat = (if ((result and 1) != 0) (result shr 1).inv() else (result shr 1))
                lat += dlat
                shift = 0
                result = 0
                do {
                    b = encoded.charAt(index++).toInt() - 63
                    result = result or ((b and 31) shl shift)
                    shift += 5
                } while (b >= 32)
                val dlng = (if ((result and 1) != 0) (result shr 1).inv() else (result shr 1))
                lng += dlng
                val x = lat.toDouble() / 1E6.toDouble()
                val y = lng.toDouble() / 1E6.toDouble()
                val node = Node(x, y)
                if (!poly!!.isEmpty()) {
                    val lastElement = poly!!.get(poly!!.size() - 1)
                    val distance = node.getLocation().distanceTo(lastElement.getLocation()).toDouble()
                    val totalDistance = distance + lastElement.totalDistance
                    node.totalDistance = totalDistance
                    if (lastNode != null) {
                        lastNode!!.bearing = getBearing(lastNode!!.getLocation(), node.getLocation())
                    }
                    lastNode!!.legDistance = distance
                }

                lastNode = node
                poly!!.add(node)
            }
        }
        return poly!!
    }

    public fun getCurrentRotationBearing(): Double {
        return 360 - poly!!.get(currentLeg).bearing
    }

    public fun rewind() {
        currentLeg = 0
    }

    throws(javaClass<JSONException>())
    public fun snapToRoute(originalPoint: Location): Location? {
        Ln.d("Snapping => currentLeg: " + currentLeg.toString())
        Ln.d("Snapping => originalPoint: " + originalPoint.getLatitude().toString() + ", " + originalPoint.getLongitude().toString())

        val sizeOfPoly = poly!!.size()

        // we have exhausted options
        if (currentLeg >= sizeOfPoly) {
            lost = true
            return null
        }

        val destination = poly!!.get(sizeOfPoly - 1)

        // if close to destination
        val distanceToDestination = destination.getLocation().distanceTo(originalPoint).toDouble()
        Ln.d("Snapping => distance to destination: " + distanceToDestination.toString())
        if (Math.floor(distanceToDestination) < 20) {
            updateDistanceTravelled(destination)
            return destination.getLocation()
        }

        val current = poly!!.get(currentLeg)
        lastFixedPoint = snapTo(current, originalPoint)
        if (lastFixedPoint == null) {
            lastFixedPoint = current.getLocation()
        } else {
            if (current.getLocation().distanceTo(lastFixedPoint) > current.legDistance - 5) {
                ++currentLeg
                updateCurrentInstructionIndex()
                Ln.d("Snapping => incrementing and trying again")
                Ln.d("Snapping => currentLeg: " + currentLeg.toString())
                return snapToRoute(originalPoint)
            }
        }

        val correctionDistance = originalPoint.distanceTo(lastFixedPoint).toDouble()
        Ln.d("Snapping => correctionDistance: " + correctionDistance.toString())
        Ln.d("Snapping => Lost Threshold: " + LOST_THRESHOLD.toString())
        Ln.d("original point => " + originalPoint.getLatitude() + ", " + originalPoint.getLongitude())
        Ln.d("fixed point => " + lastFixedPoint!!.getLatitude() + ", " + lastFixedPoint!!.getLongitude())
        if (correctionDistance < LOST_THRESHOLD) {
            updateDistanceTravelled(current)
            return lastFixedPoint
        } else {
            lost = true
            return null
        }
    }

    private fun updateDistanceTravelled(current: Node) {
        totalDistanceTravelled = 0.0
        var tempDist: Double = 0.0
        for (i in 0..currentLeg - 1) {

            tempDist += poly!!.get(i).legDistance
        }
        if (lastFixedPoint != null) {
            totalDistanceTravelled = Math.ceil(tempDist + current.getLocation().distanceTo(lastFixedPoint).toDouble())
        }
        updateAllInstructions()
    }

    public fun updateAllInstructions() {
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

    private fun snapTo(turnPoint: Node, location: Location): Location {
        if (java.lang.Double.compare(turnPoint.lat, location.getLatitude()) == 0 && java.lang.Double.compare(turnPoint.lng, location.getLongitude()) == 0) {
            updateDistanceTravelled(turnPoint)
            return location
        }

        var correctedLocation = snapTo(turnPoint, location, CLOCKWISE)
        if (correctedLocation == null) {
            correctedLocation = snapTo(turnPoint, location, COUNTERCLOCKWISE)
        }

        if (correctedLocation != null) {
            val distance = correctedLocation!!.distanceTo(location).toDouble()
            // check if results are on the otherside of the globe
            if (Math.round(distance) > CORRECTION_THRESHOLD) {
                val tmpNode = Node(turnPoint.lat, turnPoint.lng)
                tmpNode.bearing = turnPoint.bearing - REVERSE.toDouble()
                correctedLocation = snapTo(tmpNode, location, CLOCKWISE)
                if (correctedLocation == null) {
                    correctedLocation = snapTo(tmpNode, location, COUNTERCLOCKWISE)
                }
            }
        }

        val bearingDelta = turnPoint.bearing - turnPoint.getLocation().bearingTo(correctedLocation).toDouble()
        if (Math.abs(bearingDelta) > 10 && Math.abs(bearingDelta) < 350) {
            correctedLocation = turnPoint.getLocation()
        }
        return correctedLocation!!
    }

    private fun snapTo(turnPoint: Node, location: Location, offset: Int): Location? {
        val lat1 = toRadians(turnPoint.lat)
        val lon1 = toRadians(turnPoint.lng)
        val lat2 = toRadians(location.getLatitude())
        val lon2 = toRadians(location.getLongitude())

        val brng13 = toRadians(turnPoint.bearing)
        val brng23 = toRadians(turnPoint.bearing + offset.toDouble())
        val dLat = lat2 - lat1
        var dLon = lon2 - lon1
        if (dLon == 0.0) {
            dLon = 0.001
        }

        val dist12 = 2 * Math.asin(Math.sqrt(Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2)))
        if (dist12 == 0.0) {
            return null
        }

        // initial/final bearings between points
        val brngA = Math.acos((Math.sin(lat2) - Math.sin(lat1) * Math.cos(dist12)) / (Math.sin(dist12) * Math.cos(lat1)))

        val brngB = Math.acos((Math.sin(lat1) - Math.sin(lat2) * Math.cos(dist12)) / (Math.sin(dist12) * Math.cos(lat2)))

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

        val alpha3 = Math.acos(-Math.cos(alpha1) * Math.cos(alpha2) + Math.sin(alpha1) * Math.sin(alpha2) * Math.cos(dist12))
        val dist13 = Math.atan2(Math.sin(dist12) * Math.sin(alpha1) * Math.sin(alpha2), Math.cos(alpha2) + Math.cos(alpha1) * Math.cos(alpha3))
        val lat3 = Math.asin(Math.sin(lat1) * Math.cos(dist13) + Math.cos(lat1) * Math.sin(dist13) * Math.cos(brng13))
        val dLon13 = Math.atan2(Math.sin(brng13) * Math.sin(dist13) * Math.cos(lat1), Math.cos(dist13) - Math.sin(lat1) * Math.sin(lat3))
        val lon3 = ((lon1 + dLon13) + 3 * Math.PI) % (2 * Math.PI) - Math.PI  // normalise to -180..+180º

        val loc = Location(SNAP_PROVIDER)
        loc.setLatitude(Math.toDegrees(lat3))
        loc.setLongitude(Math.toDegrees(lon3))
        return loc
    }

    public fun getSeenInstructions(): Set<Instruction> {
        return seenInstructions
    }

    public fun addSeenInstruction(instruction: Instruction) {
        seenInstructions.add(instruction)
    }

    public fun getNextInstruction(): Instruction? {
        val nextInstructionIndex = currentInstructionIndex + 1
        if (nextInstructionIndex >= instructions!!.size()) {
            return null
        } else {
            return instructions!!.get(nextInstructionIndex)
        }
    }

    public fun getCurrentInstruction(): Instruction {
        return instructions!!.get(currentInstructionIndex)
    }

    throws(javaClass<JSONException>())
    private fun updateCurrentInstructionIndex() {
        val next = getNextInstruction()
        if (next == null) {
            return
        } else if (currentLeg >= next.getBeginPolygonIndex()) {
            currentInstructionIndex++
        }
    }

    public fun getAccurateStartPoint() : Location {
        return poly!!.get(0).getLocation()
    }
}
