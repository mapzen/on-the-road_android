package com.mapzen.valhalla

interface Router {

    enum class Language(private val languageTag: String) {
        CS_CZ("cs-CZ"),
        DE_DE("de-DE"),
        EN_US("en-US"),
        PIRATE("en-US-x-pirate"),
        ES_ES("es-ES"),
        FR_FR("fr-FR"),
        IT_IT("it-IT"),
        HI_IN("hi-IN"),
        CA_ES("ca-ES"),
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
    fun clearLocations(): Router
    fun setCallback(callback: RouteCallback): Router
    fun fetch()
    fun getJSONRequest(): JSON
}
