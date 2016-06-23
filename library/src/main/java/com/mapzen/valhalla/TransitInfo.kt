package com.mapzen.valhalla

import org.json.JSONObject
import java.util.ArrayList

class TransitInfo {

  companion object {
    const val KEY_TRANSIT_STOPS = "transit_stops"
    const val KEY_HEADSIGN = "headsign"
    const val KEY_LONG_NAME = "long_name"
    const val KEY_OPERATOR_URL = "operator_url"
    const val KEY_ONESTOP_ID = "onestop_id"
    const val KEY_SHORT_NAME = "short_name"
    const val KEY_COLOR = "color"
    const val KEY_DESCRIPTION = "description"
    const val KEY_TEXT_COLOR = "text_color"
    const val KEY_OPERATOR_ONESTOP_ID = "operator_onestop_id"
    const val KEY_OPERATOR_NAME = "operator_name"
  }

  private lateinit var json: JSONObject

  constructor(json: JSONObject) {
    this.json = json
  }

  fun getTransitStops(): ArrayList<TransitStop> {
    val jsonArray = json.getJSONArray(KEY_TRANSIT_STOPS)
    val stops = ArrayList<TransitStop>()
    for (i in 0..jsonArray.length() - 1) {
      stops.add(TransitStop(jsonArray.getJSONObject(i)))
    }
    return stops
  }

  fun getHeadsign(): String {
    return json.getString(KEY_HEADSIGN)
  }

  fun getLongName(): String {
    return json.getString(KEY_LONG_NAME)
  }

  fun getOperatorUrl(): String {
    return json.getString(KEY_OPERATOR_URL)
  }

  fun getOnestopId(): String {
    return json.getString(KEY_ONESTOP_ID)
  }

  fun getShortName(): String {
    return json.getString(KEY_SHORT_NAME)
  }

  fun getColor(): Int {
    return json.getInt(KEY_COLOR)
  }

  fun getDescription(): String {
    return json.getString(KEY_DESCRIPTION)
  }

  fun getTextColor(): Int {
    return json.getInt(KEY_TEXT_COLOR)
  }

  fun getOperatorOnestopId(): String {
    return json.getString(KEY_OPERATOR_ONESTOP_ID)
  }

  fun getOperatorName(): String {
    return json.getString(KEY_OPERATOR_NAME)
  }
}