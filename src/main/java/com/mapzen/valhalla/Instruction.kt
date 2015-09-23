package com.mapzen.valhalla

import android.location.Location
import android.util.Log
import com.mapzen.helpers.DistanceFormatter
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale

public open class Instruction {
    companion object {
        val KM_TO_METERS = 1000
        val MI_TO_METERS = 1609.344
    }

    public val NONE : Int = 0
    public val START : Int = 1
    public val START_RIGHT : Int = 2
    public val START_LEFT : Int = 3
    public val DESTINATION : Int = 4
    public val DESTINATION_RIGHT : Int = 5
    public val DESTINATION_LEFT : Int = 6
    public val BECOMES : Int = 7
    public val CONTINUE : Int = 8
    public val SLIGHT_RIGHT : Int = 9
    public val RIGHT : Int = 10
    public val SHARP_RIGHT : Int = 11
    public val U_TURN_RIGHT: Int = 12
    public val U_TURN_LEFT : Int = 13
    public val SHARP_LEFT : Int = 14
    public val LEFT : Int = 15
    public val SLIGHT_LEFT : Int = 16
    public val RAMP_STRAIGHT : Int = 17
    public val RAMP_RIGHT : Int = 18
    public val RAMP_LEFT : Int = 19
    public val EXIT_RIGHT : Int = 20
    public val EXIT_LEFT : Int = 21
    public val STAY_STRAIGHT : Int = 22
    public val STAY_RIGHT : Int = 23
    public val STAY_LEFT : Int = 24
    public val MERGE : Int = 25
    public val ROUNDABOUT_ENTER : Int = 26
    public val ROUNDABOUT_EXIT: Int = 27
    public val FERRY_ENTER: Int = 28
    public val FERRY_EXIT: Int = 29

    private var json: JSONObject? = null;

    public var turnInstruction: Int = 0
    public var distance: Int = 0
    public var location: Location = Location("snap")
    public var liveDistanceToNext: Int = -1
    public var bearing: Int = 0

    public constructor(json: JSONObject) : this(json, Router.DistanceUnits.KILOMETERS) { }

    public constructor(json: JSONObject, units: Router.DistanceUnits) {
        if (json.length() < 6) {
            throw JSONException("too few arguments")
        }
        this.json = json
        turnInstruction = parseTurnInstruction(json)

        val raw = json.getDouble("length")
        when (units) {
            Router.DistanceUnits.KILOMETERS -> distance = Math.round(raw * KM_TO_METERS).toInt()
            Router.DistanceUnits.MILES -> distance = Math.round(raw * MI_TO_METERS).toInt()
        }
    }

    /**
     * Used for testing. Do not remove.
     */
    @SuppressWarnings("unused")
    protected constructor() {
    }

    public fun getIntegerInstruction(): Int {
       return turnInstruction;
    }

    public fun getHumanTurnInstruction(): String? {
        return json?.getString("instruction");
    }

    public fun skip(): Boolean {
        if(json!!.optJSONArray("street_names") == null && json!!.getInt("type") != DESTINATION ) {
            return true
        }
        return false;
    }

    public fun getName(): String {
        if (json?.getInt("type") == DESTINATION) {
            return "You have arrived at your destination."
        }

        if (json?.has("street_names") ?: false) {
            var streetName = "";
            val numStreetNames = (json!!.getJSONArray("street_names").length())
            for(i in 0..numStreetNames - 1) {
                streetName += json!!.getJSONArray("street_names").get(i);
                if((numStreetNames > 1) && (i < numStreetNames - 1)) {
                    streetName += "/"
                }
            }
            return streetName;
        }

        return json?.getString("instruction") ?: ""
    }

    public fun getFormattedDistance(): String {
        return DistanceFormatter.format(distance.toInt())
    }

    public fun getBeginPolygonIndex(): Int {
        return json!!.getInt("begin_shape_index");
    }

    public fun getEndPolygonIndex(): Int {
        return json!!.getInt("end_shape_index");
    }

    public fun getDirectionAngle(): Float {
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

    public fun getDirection(): String {
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

    public fun getRotationBearing(): Int {
        return 360 - bearing
    }

    override fun toString(): String {
        var name = ""
        try {
            name = getName()
        } catch (e: JSONException) {
            Log.e("Json exception", "Unable to get name", e)
        }

        return java.lang.String.format(Locale.US, "Instruction: (%.5f, %.5f) %s %s" +
                "LiveDistanceTo: %d", location.getLatitude(), location.getLongitude(),
                turnInstruction, name, liveDistanceToNext)
    }

    override fun equals(obj: Any?): Boolean {
        if (obj == null) {
            return false
        }

        if (javaClass != obj!!.javaClass) {
            return false
        }

        val other = obj as Instruction
        return (turnInstruction == other.turnInstruction
                && bearing == other.bearing
                && location.getLatitude() == other.location.getLatitude()
                && location.getLongitude() == other.location.getLongitude())
    }

    private fun parseTurnInstruction(json: JSONObject): Int =
            json.getInt("type")

    public fun getVerbalPreTransitionInstruction(): String {
        if (json?.has("verbal_pre_transition_instruction") ?: false) {
            return json?.getString("verbal_pre_transition_instruction") ?: ""
        }

        return ""
    }

    public fun getVerbalTransitionAlertInstruction(): String {
        if (json?.has("verbal_transition_alert_instruction") ?: false) {
            return json?.getString("verbal_transition_alert_instruction") ?: ""
        }

        return ""
    }

    public fun getVerbalPostTransitionInstruction(): String {
        if (json?.has("verbal_post_transition_instruction") ?: false) {
            return json?.getString("verbal_post_transition_instruction") ?: ""
        }

        return ""
    }
}
