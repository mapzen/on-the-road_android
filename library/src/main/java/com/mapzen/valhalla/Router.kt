package com.mapzen.valhalla

import retrofit2.Call

interface Router {

    enum class Language(private val languageTag: String) {
        CA_ES("ca-ES"),
        CS_CZ("cs-CZ"),
        DE_DE("de-DE"),
        EN_US("en-US"),
        PIRATE("en-US-x-pirate"),
        ES_ES("es-ES"),
        FR_FR("fr-FR"),
        HI_IN("hi-IN"),
        IT_IT("it-IT"),
        RU_RU("ru-RU"),
        SL_SI("sl-SI");

        override fun toString(): String {
            return languageTag
        }
    }

    enum class Type(private val type: String) {
        WALKING("pedestrian"),
        BIKING("bicycle"),
        DRIVING("auto"),
        MULTIMODAL("multimodal");

        override fun toString(): String {
            return type
        }
    }

    enum class DistanceUnits(private val units: String) {
        MILES("miles"),
        KILOMETERS("kilometers");

        override fun toString(): String {
            return units
        }
    }

    fun setHttpHandler(handler: HttpHandler): Router
    fun setLanguage(language: Language): Router
    fun setWalking(): Router
    fun setDriving(): Router
    fun setBiking(): Router
    fun setMultimodal(): Router
    fun setLocation(point: DoubleArray): Router
    fun setLocation(point: DoubleArray, heading: Int): Router
    fun setLocation(point: DoubleArray,
            name: String? = null,
            street: String? = null,
            city: String? = null,
            state: String? = null): Router
    fun setDistanceUnits(units: DistanceUnits): Router
    /*
     * Sets the difficulty for hiking route types
     *
     * This value indicates the maximum difficulty of hiking trails that is allowed. Values between
     * 0 and 6 are allowed. The values correspond to sac_scale values within OpenStreetMap. The
     * default value is 1 which means that well cleared trails that are mostly flat or slightly
     * sloped are allowed. Higher difficulty trails can be allowed by specifying a higher value for
     * maxHikingDifficulty.
     */
    fun setMaxHikingDifficulty(difficulty: Int): Router
    fun clearLocations(): Router
    fun setCallback(callback: RouteCallback): Router
    fun fetch(): Call<String>?
    fun getJSONRequest(): JSON
}
