package com.mapzen.valhalla;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

class LocationSerializer implements JsonSerializer<JSON.Location> {
  @Override public JsonElement serialize(JSON.Location src, Type typeOfSrc,
      JsonSerializationContext context) {
    JsonObject jsonObject = (JsonObject) new GsonBuilder().create().toJsonTree(src);
    if (src.heading < 0 || src.heading >= 360) {
      jsonObject.remove("heading");
    }

    return jsonObject;
  }
}
