package com.mapzen.valhalla

enum class TravelMode(private val mode: String) {
  DRIVE("drive"),
  PEDESTRIAN("pedestrian"),
  BICYCLE("bicycle"),
  TRANSIT("transit");

  override fun toString(): String {
    return mode
  }
}
