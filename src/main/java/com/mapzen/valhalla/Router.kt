package com.mapzen.valhalla

import android.util.Log
import android.widget.Toast
import com.google.common.base.Charsets
import com.google.common.io.CharStreams
import com.google.gson.Gson
import com.squareup.okhttp.OkHttpClient
import org.apache.commons.io.IOUtils

import org.json.JSONException
import retrofit.*

import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import retrofit.http.GET
import retrofit.client.Response
import retrofit.http.Query;
import java.net.URL
import java.net.URLEncoder
import java.util.ArrayList

public class Router : Runnable {
    private val DEFAULT_URL = "http://valhalla.mapzen.com/"
    private var API_KEY = "";
    private var endpoint: String = DEFAULT_URL
    private var type = Type.DRIVING
    private val locations = ArrayList<DoubleArray>()
    private var callback: Callback? = null

    public enum class Type(private val type: String) {
        WALKING : Type("pedestrian")
        BIKING : Type("bicycle")
        DRIVING : Type("auto")

        override fun toString(): String {
            return type
        }
    }

    public fun setApiKey(key: String): Router {
        API_KEY = key
        return this;
    }

    public fun setWalking(): Router {

        this.type = Type.WALKING
        return this
    }

    public fun setDriving(): Router {
        this.type = Type.DRIVING
        return this
    }

    public fun setBiking(): Router {
        this.type = Type.BIKING
        return this
    }

    public fun setLocation(point: DoubleArray): Router {
        this.locations.add(point)
        return this
    }

    public fun clearLocations(): Router {
        this.locations.clear()
        return this
    }

    public fun setEndpoint(url: String): Router {
        endpoint = url;
        return this
    }

    public fun getEndpoint(): String {
        return endpoint
    }

    throws(IOException::class)
    private fun readInputStream(`in`: InputStream?): String {
        return CharStreams.toString(InputStreamReader(`in`))
    }

    public fun setCallback(callback: Callback): Router {
        this.callback = callback
        return this
    }

    public fun fetch() {
        if (callback == null) {
            return
        }
        Thread(this).start()
    }

    override fun run() {
        var route: Route? = null;


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

    public fun getJSONRequest(): JSON {
        if (locations.size() < 2) {
            throw  MalformedURLException();
        }
        var json: JSON = JSON();
        json.locations[0] = JSON.location()
        json.locations[1] = JSON.location()
        json.locations[0].lat = locations.get(0)[0].toString()
        json.locations[0].lon = locations.get(0)[1].toString()
        json.locations[1].lat = locations.get(1)[0].toString()
        json.locations[1].lon = locations.get(1)[1].toString()
        json.costing = this.type.toString()
        return json
    }

    public interface Callback {
        public fun success(route: Route?)
        public fun failure(statusCode: Int)
    }

}

