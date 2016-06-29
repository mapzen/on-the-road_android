package com.mapzen.valhalla

import com.mapzen.helpers.DistanceFormatter
import com.mapzen.model.ValhallaLocation
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList
import java.util.Locale

open class Instruction {
    companion object {
        const val KM_TO_METERS = 1000
        const val MI_TO_METERS = 1609.344
        //full list of types defined: https://mapzen.com/documentation/turn-by-turn/api-reference/
        const val MANEUVER_TYPE_DESTINATION : Int = 4

        const val KEY_INSTRUCTION = "instruction"
        const val KEY_LENGTH = "length"
        const val KEY_BEGIN_STREET_NAMES = "begin_street_names"
        const val KEY_STREET_NAMES = "street_names"
        const val KEY_BEGIN_SHAPE_INDEX = "begin_shape_index"
        const val KEY_END_SHAPE_INDEX = "end_shape_index"
        const val KEY_TYPE = "type"
        const val KEY_VERBAL_PRE_TRANSITION_INSTRUCTION = "verbal_pre_transition_instruction"
        const val KEY_VERBAL_TRANSITION_ALERT_INSTRUCTION = "verbal_transition_alert_instruction"
        const val KEY_VERBAL_POST_TRANSITION_INSTRUCTION = "verbal_post_transition_instruction"
        const val KEY_TRAVEL_MODE = "travel_mode"
        const val KEY_TRAVEL_TYPE = "travel_type"
        const val KEY_TRANSIT_INFO = "transit_info"
        const val KEY_TIME = "time"
    }

    lateinit var json: JSONObject

    var turnInstruction: Int = 0
    var distance: Int = 0
    var location: ValhallaLocation = ValhallaLocation()
    var liveDistanceToNext: Int = -1
    var bearing: Int = 0

    constructor(json: JSONObject) : this(json, Router.DistanceUnits.KILOMETERS) { }

    constructor(json: JSONObject, units: Router.DistanceUnits) {
        if (json.length() < 6) {
            throw JSONException("too few arguments")
        }
        this.json = json
        turnInstruction = parseTurnInstruction(json)

        val raw = json.getDouble(KEY_LENGTH)
        when (units) {
            Router.DistanceUnits.KILOMETERS -> distance = Math.round(raw * KM_TO_METERS).toInt()
            Router.DistanceUnits.MILES -> distance = Math.round(raw * MI_TO_METERS).toInt()
        }
    }

    fun getIntegerInstruction(): Int {
       return turnInstruction;
    }

    fun getHumanTurnInstruction(): String? {
        return json.getString(KEY_INSTRUCTION);
    }

    fun getBeginStreetNames(): String {
        if (json.has(KEY_BEGIN_STREET_NAMES)) {
            var streetName = "";
            val numStreetNames = (json.getJSONArray(KEY_BEGIN_STREET_NAMES).length())
            for(i in 0..numStreetNames - 1) {
                streetName += json.getJSONArray(KEY_BEGIN_STREET_NAMES).get(i);
                if((numStreetNames > 1) && (i < numStreetNames - 1)) {
                    streetName += "/"
                }
            }
            return streetName;
        }

        return ""
    }


    fun getName(): String {
        if (json.has(KEY_STREET_NAMES)) {
            var streetName = "";
            val numStreetNames = (json.getJSONArray(KEY_STREET_NAMES).length())
            for(i in 0..numStreetNames - 1) {
                streetName += json.getJSONArray(KEY_STREET_NAMES).get(i);
                if((numStreetNames > 1) && (i < numStreetNames - 1)) {
                    streetName += "/"
                }
            }
            return streetName;
        }

        return json.getString(KEY_INSTRUCTION) ?: ""
    }

    fun getFormattedDistance(): String {
        return DistanceFormatter.format(distance.toInt())
    }

    fun getTime(): Int {
        return json.getInt(KEY_TIME)
    }

    fun getBeginPolygonIndex(): Int {
        return json.getInt(KEY_BEGIN_SHAPE_INDEX);
    }

    fun getEndPolygonIndex(): Int {
        return json.getInt(KEY_END_SHAPE_INDEX);
    }

    fun getDirectionAngle(): Float {
        val direction = bearing
        var angle: Float = 0.0f
        if (direction >= 315.0 && direction <= 360.0 ) {
            angle = 315.0.toFloat()
        } else if (direction >= 270.0 && direction < 315.0) {
            angle = 270.0.toFloat()
        } else if (direction >= 225.0 && direction < 270.0) {
            angle = 225.0.toFloat()
        } else if (direction >= 180.0 && direction < 225.0) {
            angle = 180.0.toFloat()
        } else if (direction >= 135.0 && direction < 180.0) {
            angle = 135.0.toFloat()
        } else if (direction >= 90.0 && direction < 135.0) {
            angle = 90.0.toFloat()
        } else if (direction >= 45.0 && direction < 90.0) {
            angle = 45.0.toFloat()
        } else if (direction >= 0.0 && direction < 45.0) {
            angle = 0.0.toFloat()
        }
        return angle
    }

    fun getDirection(): String {
        var direction = ""
        if (bearing >= 315.0 && bearing >= 360.0 ) {
            direction = "NE"
        } else if (bearing >= 270.0 && bearing < 315.0) {
            direction = "E"
        } else if (bearing >= 225.0 && bearing < 270.0) {
            direction = "SE"
        } else if (bearing >= 180.0 && bearing < 225.0) {
            direction = "S"
        } else if (bearing >= 135.0 && bearing < 180.0) {
            direction = "SW"
        } else if (bearing >= 90.0 && bearing < 135.0) {
            direction = "W"
        } else if (bearing >= 45.0 && bearing < 90.0) {
            direction = "NW"
        } else if (bearing >= 0.0 && bearing < 45.0) {
            direction = "N"
        }
        return direction
    }

    fun getRotationBearing(): Int {
        return 360 - bearing
    }

    override fun toString(): String {
        var name = ""
        try {
            name = getName()
        } catch (e: JSONException) {
            System.out.println("Json exception unable to get name" + e.stackTrace)
        }

        return java.lang.String.format(Locale.US, "Instruction: (%.5f, %.5f) %s %s" +
                "LiveDistanceTo: %d", location.latitude, location.longitude,
                turnInstruction, name, liveDistanceToNext)
    }

    override fun equals(obj: Any?): Boolean {
        if (obj == null) {
            return false
        }

        if (javaClass != obj.javaClass) {
            return false
        }

        val other = obj as Instruction
        return (turnInstruction == other.turnInstruction
                && bearing == other.bearing
                && location.latitude == other.location.latitude
                && location.longitude == other.location.longitude)
    }

    private fun parseTurnInstruction(json: JSONObject): Int =
            json.getInt(KEY_TYPE)

    fun getVerbalPreTransitionInstruction(): String {
        if (json.has(KEY_VERBAL_PRE_TRANSITION_INSTRUCTION)) {
            return json.getString(KEY_VERBAL_PRE_TRANSITION_INSTRUCTION) ?: ""
        }

        return ""
    }

    fun getVerbalTransitionAlertInstruction(): String {
        if (json.has(KEY_VERBAL_TRANSITION_ALERT_INSTRUCTION)) {
            return json.getString(KEY_VERBAL_TRANSITION_ALERT_INSTRUCTION) ?: ""
        }

        return ""
    }

    fun getVerbalPostTransitionInstruction(): String {
        if (json.has(KEY_VERBAL_POST_TRANSITION_INSTRUCTION)) {
            return json.getString(KEY_VERBAL_POST_TRANSITION_INSTRUCTION) ?: ""
        }

        return ""
    }

    fun getTravelMode(): TravelMode {
        val mode = json.getString(KEY_TRAVEL_MODE)

        when (mode) {
            TravelMode.DRIVE.toString() -> return TravelMode.DRIVE
            TravelMode.PEDESTRIAN.toString() -> return TravelMode.PEDESTRIAN
            TravelMode.BICYCLE.toString() -> return TravelMode.BICYCLE
            TravelMode.TRANSIT.toString() -> return TravelMode.TRANSIT
            else -> return TravelMode.DRIVE
        }
    }

    fun getTravelType(): TravelType {
        val type = json.getString(KEY_TRAVEL_TYPE)

        when (type) {
            TravelType.CAR.toString() -> return TravelType.CAR
            TravelType.FOOT.toString() -> return TravelType.FOOT
            TravelType.ROAD.toString() -> return TravelType.ROAD
            TravelType.TRAM.toString() -> return TravelType.TRAM
            TravelType.METRO.toString() -> return TravelType.METRO
            TravelType.RAIL.toString() -> return TravelType.RAIL
            TravelType.BUS.toString() -> return TravelType.BUS
            TravelType.FERRY.toString() -> return TravelType.FERRY
            TravelType.CABLE_CAR.toString() -> return TravelType.CABLE_CAR
            TravelType.GONDOLA.toString() -> return TravelType.GONDOLA
            TravelType.FUNICULAR.toString() -> return TravelType.FUNICULAR
            else -> return TravelType.CAR
        }
    }

    fun getTransitInfo(): TransitInfo? {
        val transitInfoJson = json.optJSONObject(KEY_TRANSIT_INFO) ?: return null
        return TransitInfo(transitInfoJson)
    }
}
