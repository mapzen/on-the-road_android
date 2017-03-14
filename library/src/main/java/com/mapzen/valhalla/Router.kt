package com.mapzen.valhalla

interface Router {

    enum class Language(private val languageTag: String) {
        CS_CZ("cs"),
        DE_DE("de"),
        EN_US("en"),
        PIRATE("pirate"),
        ES_ES("es"),
        FR_FR("fr"),
        IT_IT("it"),
        HI_IN("hi");

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
