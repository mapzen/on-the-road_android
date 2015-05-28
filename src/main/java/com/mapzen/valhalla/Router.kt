package com.mapzen.valhalla

import com.google.common.base.Charsets
import com.google.common.io.CharStreams
import com.squareup.okhttp.OkHttpClient

import org.json.JSONException

import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.net.URLEncoder
import java.util.ArrayList

public class Router : Runnable {
    private val DEFAULT_URL = "http://valhalla.api.dev.mapzen.com/route"
    private val ROUTE_PARAMS = "?json={\"locations\":" + "[{\"lat\":%1f,\"lon\":%2f},{\"lat\":%3f,\"lon\":%4f}]," + "\"costing\":\"%s\",\"output\":\"json\"}"
    private var endpoint = DEFAULT_URL
    private val client = OkHttpClient()
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

    public fun getRouter(): Router {
        return Router()
    }

    public fun setEndpoint(endpoint: String): Router {
        this.endpoint = endpoint
        return this
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

    throws(javaClass<IOException>())
    private fun readInputStream(`in`: InputStream): String {
        return CharStreams.toString(InputStreamReader(`in`, Charsets.UTF_8))
    }

    throws(javaClass<MalformedURLException>(), javaClass<UnsupportedEncodingException>())
    public fun getRouteUrl(): URL {
        if (locations.size() < 2) {
            throw MalformedURLException()
        }

        val params = java.lang.String.format(ROUTE_PARAMS, locations.get(0)[0], locations.get(0)[1], locations.get(1)[0], locations.get(1)[1], type)

        return URL(endpoint + URLEncoder.encode(params, "utf-8"))
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
        var `in`: InputStream? = null
        try {
            val connection = client.open(getRouteUrl())
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                callback!!.failure(connection.getResponseCode())
                return
            }
            `in` = connection.getInputStream()
            val responseText = readInputStream(`in`!!)
            var route: Route? = null
            try {
                route = Route(responseText)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            if (route!!.foundRoute()) {
                callback!!.success(route!!)
            } else {
                callback!!.failure(207)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        } finally {
            if (`in` != null) {
                try {
                    `in`!!.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
    }

    public trait Callback {
        public fun success(route: Route)
        public fun failure(statusCode: Int)
    }

    companion object {



    }
}
