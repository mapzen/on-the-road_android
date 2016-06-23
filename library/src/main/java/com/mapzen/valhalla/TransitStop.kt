package com.mapzen.valhalla

import org.json.JSONObject

class TransitStop {

  companion object {
    const val KEY_TYPE = "type"
    const val KEY_ONESTOP_ID = "onestop_id"
    const val KEY_NAME = "name"
    const val KEY_ARRIVAL_DATE_TIME = "arrival_date_time"
    const val KEY_DEPARTURE_DATE_TIME = "departure_date_time"
    const val KEY_IS_PARENT_STOP = "is_parent_stop"
    const val KEY_ASSUMED_SCHEDULE = "assumed_schedule"
  }

  lateinit var json: JSONObject

  constructor(json: JSONObject) {
      this.json = json
  }

  fun getType(): String {
    return json.getString(KEY_TYPE)
  }

  fun getOnestopId(): String {
    return json.getString(KEY_ONESTOP_ID)
  }

  fun getName(): String {
    return json.getString(KEY_NAME)
  }

  fun getDepartureDateTime(): String {
    return json.getString(KEY_DEPARTURE_DATE_TIME)
  }

  fun getIsParentStop(): Boolean {
    return json.getBoolean(KEY_IS_PARENT_STOP)
  }

  fun getAssumedSchedule(): Boolean {
    return json.getBoolean(KEY_ASSUMED_SCHEDULE)
  }

  fun getArrivalDateTime(): String {
    return json.getString(KEY_ARRIVAL_DATE_TIME)
  }

}