package com.mapzen.valhalla

enum class TravelType(private val type: String) {
  CAR("car"), // Drive
  FOOT("foot"), // Pedestrian
  ROAD("road"), // Bicycle
  TRAM("tram"), // Tram or light rail
  METRO("metro"), // Metro or subway
  RAIL("rail"),
  BUS("bus"),
  FERRY("ferry"),
  CABLE_CAR("cable_car"),
  GONDOLA("gondola"),
  FUNICULAR("funicular");

  override fun toString(): String {
    return type
  }
}
