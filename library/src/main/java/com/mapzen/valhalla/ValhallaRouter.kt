package com.mapzen.valhalla

import android.util.Log
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.MalformedURLException
import java.util.ArrayList

open class ValhallaRouter : Router, Runnable {

    private var language = Router.Language.EN_US
    private var type = Router.Type.DRIVING
    private val locations = ArrayList<JSON.Location>()
    private var callback: RouteCallback? = null
    private var units: Router.DistanceUnits = Router.DistanceUnits.KILOMETERS

    private var httpHandler: HttpHandler? = null

    var gson: Gson = Gson()

    override fun setHttpHandler(handler: HttpHandler): Router {
        httpHandler = handler
        return this
    }

    override fun setLanguage(language: Router.Language): Router {
        this.language = language
        return this
    }

    override fun setWalking(): Router {
        this.type = Router.Type.WALKING
        return this
    }

    override fun setDriving(): Router {
        this.type = Router.Type.DRIVING
        return this
    }

    override fun setBiking(): Router {
        this.type = Router.Type.BIKING
        return this
    }

    override fun setMultimodal(): Router {
        this.type = Router.Type.MULTIMODAL
        return this
    }

    override fun setLocation(point: DoubleArray): Router {
        this.locations.add(JSON.Location(point[0].toString(), point[1].toString()))
        return this
    }

    override fun setLocation(point: DoubleArray, heading: Float): Router {
        this.locations.add(JSON.Location(point[0].toString(), point[1].toString(),
                heading.toInt().toString()))
        return this
    }

    override fun setLocation(point: DoubleArray,
            name: String?,
            street: String?,
            city: String?,
            state: String?): Router {

        this.locations.add(JSON.Location(point[0].toString(), point[1].toString(),
                name, street, city, state));
        return this
    }

    override fun setDistanceUnits(units: Router.DistanceUnits): Router {
        this.units = units
        return this
    }

    override fun clearLocations(): Router {
        this.locations.clear()
        return this
    }

    override fun setCallback(callback: RouteCallback): Router {
        this.callback = callback
        return this
    }

    override fun fetch() {
        if (callback == null) {
            return
        }
        Thread(this).start()
    }

    override fun run() {
        var jsonString = gson.toJson(getJSONRequest()).toString()
        httpHandler?.requestRoute(jsonString, object: Callback<String> {
            override fun onResponse(call: Call<String>?, response: Response<String>?) {
                if (response != null) {
                    if (response.isSuccessful) {
                        callback?.success(Route(response.body()))
                    } else {
                        callback?.failure(response.raw().code())
                    }
                }
            }

            override fun onFailure(call: Call<String>?, t: Throwable?) {
                t?.printStackTrace()
            }
        })
    }

    override fun getJSONRequest(): JSON {
        if (locations.size < 2) {
            throw  MalformedURLException()
        }
        var json: JSON = JSON()
        for ( i in 0..(locations.size-1)){
            json.locations.add(locations[i])
        }

        json.costing = this.type.toString()
        json.directionsOptions.language = this.language.toString()
        json.directionsOptions.units = this.units.toString()
        return json
    }
}
