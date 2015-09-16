package com.mapzen.valhalla

import com.google.common.io.CharStreams
import com.google.gson.Gson
import retrofit.RestAdapter
import retrofit.RetrofitError
import retrofit.client.Response
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.MalformedURLException
import java.util.ArrayList

public open class ValhallaRouter : Router, Runnable {
    private val DEFAULT_URL = "http://valhalla.mapzen.com/"
    private var API_KEY = "";
    private var endpoint: String = DEFAULT_URL
    private var type = Router.Type.DRIVING
    private val locations = ArrayList<DoubleArray>()
    private var callback: RouteCallback? = null
    private var units: Router.DistanceUnits = Router.DistanceUnits.KILOMETERS

    override fun setApiKey(key: String): Router {
        API_KEY = key
        return this;
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

    override fun setLocation(point: DoubleArray): Router {
        this.locations.add(point)
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

    override fun setEndpoint(url: String): Router {
        endpoint = url;
        return this
    }

    override fun getEndpoint(): String {
        return endpoint
    }

    private fun readInputStream(`in`: InputStream?): String {
        // TODO: Parse response using GSON
        return CharStreams.toString(InputStreamReader(`in`))
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
        var restAdapter: RestAdapter = RestAdapter.Builder()
                .setEndpoint(DEFAULT_URL)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build()

        var routingService = RestAdapterFactory(restAdapter).getRoutingService();
        var gson: Gson = Gson();
        routingService.getRoute(gson.toJson(getJSONRequest()).toString(),
                API_KEY,
                ((object : retrofit.Callback<Result> {
                    override fun failure(error: RetrofitError?) {
                        callback!!.failure(207)
                    }

                    override fun success(t: Result?, response: Response) {
                        if (response.getBody() != null) {
                            try {
                                var input = response.getBody().`in`()
                                callback!!.success(Route(readInputStream(input)))
                                input.close()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }
                    }

                }) as retrofit.Callback<Result> ))
    }

    override fun getJSONRequest(): JSON {
        if (locations.size() < 2) {
            throw  MalformedURLException();
        }
        var json: JSON = JSON();
        json.locations[0] = JSON.Location()
        json.locations[1] = JSON.Location()
        json.locations[0].lat = locations.get(0)[0].toString()
        json.locations[0].lon = locations.get(0)[1].toString()
        json.locations[1].lat = locations.get(1)[0].toString()
        json.locations[1].lon = locations.get(1)[1].toString()
        json.costing = this.type.toString()
        json.directionsOptions.units = this.units.toString()
        return json
    }

    public class Result {
        // Retrofit placeholder. Replace with result object after migrating to GSON (see above).
    }
}
