package com.mapzen.valhalla

import com.mapzen.helpers.DistanceFormatter
import com.mapzen.ontheroad.R

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import android.content.Context
import android.location.Location
import android.util.Log

import java.util.Locale

import com.mapzen.osrm.Route.SNAP_PROVIDER

public class Instruction {
    public val NO_TURN: Int = 0
    public val GO_STRAIGHT: Int = 1
    public val TURN_SLIGHT_RIGHT: Int = 2
    public val TURN_RIGHT: Int = 3
    public val TURN_SHARP_RIGHT: Int = 4
    public val U_TURN: Int = 5
    public val TURN_SHARP_LEFT: Int = 6
    public val TURN_LEFT: Int = 7
    public val TURN_SLIGHT_LEFT: Int = 8
    public val REACH_VIA_POINT: Int = 9
    public val HEAD_ON: Int = 10
    public val ENTER_ROUND_ABOUT: Int = 11
    public val LEAVE_ROUND_ABOUT: Int = 12
    public val STAY_ON_ROUND_ABOUT: Int = 13
    public val START_AT_END_OF_STREET: Int = 14
    public val YOU_HAVE_ARRIVED: Int = 15
    public val ENTER_AGAINST_ALLOWED_DIRECTION: Int = 16
    public val LEAVE_AGAINST_ALLOWED_DIRECTION: Int = 17
    public val INSTRUCTION_COUNT: Int = 18
    private var json: JSONArray? = null;

    public var turnInstruction: Int = 0
    public var distance: Int = 0
    public var location: Location = Location(SNAP_PROVIDER)
    public var liveDistanceToNext: Int = -1

    throws(javaClass<JSONException>())
    public constructor(json: JSONArray) {
        if (json.length() < 8) {
            throw JSONException("too few arguments")
        }
        this.json = json
        turnInstruction = parseTurnInstruction(json)
        distance = json.getInt(2)
    }

    /**
     * Used for testing. Do not remove.
     */
    SuppressWarnings("unused")
    protected constructor() {
    }

    public fun getHumanTurnInstruction(context: Context): String {
        when (turnInstruction) {
            NO_TURN -> return context.getString(R.string.no_turn)
            GO_STRAIGHT -> return context.getString(R.string.go_straight)
            TURN_SLIGHT_RIGHT -> return context.getString(R.string.turn_slight_right)
            TURN_RIGHT -> return context.getString(R.string.turn_right)
            TURN_SHARP_RIGHT -> return context.getString(R.string.turn_sharp_right)
            U_TURN -> return context.getString(R.string.u_turn)
            TURN_SHARP_LEFT -> return context.getString(R.string.turn_sharp_left)
            TURN_LEFT -> return context.getString(R.string.turn_left)
            TURN_SLIGHT_LEFT -> return context.getString(R.string.turn_slight_left)
            REACH_VIA_POINT -> return context.getString(R.string.reach_via_point)
            HEAD_ON -> return context.getString(R.string.head_on)
            ENTER_ROUND_ABOUT -> return context.getString(R.string.enter_round_about)
            LEAVE_ROUND_ABOUT -> return context.getString(R.string.leave_round_about)
            STAY_ON_ROUND_ABOUT -> return context.getString(R.string.stay_on_round_about)
            START_AT_END_OF_STREET -> return context.getString(R.string.start_at_end_of_street)
            YOU_HAVE_ARRIVED -> return context.getString(R.string.you_have_arrived)
            ENTER_AGAINST_ALLOWED_DIRECTION -> return context.getString(R.string.enter_against_allowed_direction)
            LEAVE_AGAINST_ALLOWED_DIRECTION -> return context.getString(R.string.leave_against_allowed_direction)
            else -> return context.getString(R.string.no_turn)
        }
    }

    throws(javaClass<JSONException>())
    public fun skip(): Boolean {
        val raw = json!!.getString(1)
        return raw.startsWith("{") && raw.endsWith("}")
    }

    throws(javaClass<JSONException>())
    public fun getName(): String {
        val raw = json!!.getString(1)
        if (raw.startsWith("{") && raw.endsWith("}")) {
            val nameObject = JSONObject(raw)
            return nameObject.getString("highway")
        } else {
            return raw
        }
    }

    public fun getFormattedDistance(): String {
        return DistanceFormatter.format(distance)
    }

    throws(javaClass<JSONException>())
    public fun getDirection(): String {
        return json!!.getString(6)
    }

    throws(javaClass<JSONException>())
    public fun getPolygonIndex(): Int {
        return json!!.getInt(3)
    }

    throws(javaClass<JSONException>())
    public fun getDirectionAngle(): Float {
        val direction = getDirection()
        var angle: Float = 0.0f
        if (direction == "NE") {
            angle = 315.0.toFloat()
        } else if (direction == "E") {
            angle = 270.0.toFloat()
        } else if (direction == "SE") {
            angle = 225.0.toFloat()
        } else if (direction == "S") {
            angle = 180.0.toFloat()
        } else if (direction == "SW") {
            angle = 135.0.toFloat()
        } else if (direction == "W") {
            angle = 90.0.toFloat()
        } else if (direction == "NW") {
            angle = 45.0.toFloat()
        }
        return angle
    }

    throws(javaClass<JSONException>())
    public fun getRotationBearing(): Int {
        return 360 - json!!.getInt(7)
    }

    throws(javaClass<JSONException>())
    public fun getBearing(): Int {
        return json!!.getInt(7)
    }

    throws(javaClass<JSONException>())
    public fun getFullInstruction(context: Context): String {
        return getFullInstructionBeforeAction(context)
    }

    throws(javaClass<JSONException>())
    public fun getFullInstructionBeforeAction(context: Context): String {
        if (turnInstruction == HEAD_ON || turnInstruction == GO_STRAIGHT) {
            return context.getString(R.string.full_instruction_before_straight, getHumanTurnInstruction(context), getName(), DistanceFormatter.format(distance, true))
        } else if (turnInstruction == YOU_HAVE_ARRIVED) {
            return context.getString(R.string.full_instruction_destination, getHumanTurnInstruction(context), getName())
        } else {
            return context.getString(R.string.full_instruction_before_default, getHumanTurnInstruction(context), getName(), DistanceFormatter.format(distance, true))
        }
    }

    throws(javaClass<JSONException>())
    public fun getFullInstructionAfterAction(context: Context): String {
        if (turnInstruction == YOU_HAVE_ARRIVED) {
            return context.getString(R.string.full_instruction_destination, getHumanTurnInstruction(context), getName())
        }

        return context.getString(R.string.full_instruction_after, getName(), DistanceFormatter.format(distance, false))
    }

    throws(javaClass<JSONException>())
    public fun getSimpleInstruction(context: Context): String {
        return context.getString(R.string.simple_instruction, getHumanTurnInstruction(context), getName())
    }

    override fun toString(): String {
        var name = ""
        try {
            name = getName()
        } catch (e: JSONException) {
            Log.e("Json exception", "Unable to get name", e)
        }

        return java.lang.String.format(Locale.US, "Instruction: (%.5f, %.5f) %s %s LiveDistanceTo: %d", location.getLatitude(), location.getLongitude(), turnInstruction, name, liveDistanceToNext)
    }

    override fun equals(obj: Any?): Boolean {
        if (javaClass != obj!!.javaClass) {
            return false
        }
        val other = obj as Instruction
        try {
            return (turnInstruction == other.turnInstruction && getBearing() == other.getBearing() && location.getLatitude() == other.location.getLatitude() && location.getLongitude() == other.location.getLongitude())
        } catch (e: JSONException) {
            Log.e("Json exception", "Unable to get bearing", e)
            return false
        }

    }

    throws(javaClass<JSONException>())
    private fun parseTurnInstruction(json: JSONArray): Int {
        val turn = json.getString(0)
        val split = turn.split("-")
        return Integer.valueOf(split[0])!!
    }

    throws(javaClass<JSONException>())
    public fun getSimpleInstructionAfterAction(context: Context): String {
        if (turnInstruction == YOU_HAVE_ARRIVED) {
            return getFullInstructionBeforeAction(context)
        }

        return context.getString(R.string.simple_instruction_after, getName())
    }

}