package com.mapzen.valhalla

import android.util.Log
import android.widget.Toast
import com.google.common.base.Charsets
import com.google.common.io.CharStreams
import com.google.gson.Gson
import com.squareup.okhttp.OkHttpClient

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
    private val DEFAULT_URL = "http://valhalla.dev.mapzen.com/"
    private val ROUTE_PARAMS = "?json={\"locations\":" + "[{\"lat\":%1f,\"lon\":%2f},{\"lat\":%3f,\"lon\":%4f}]," + "\"costing\":\"%s\",\"output\":\"json\"}&api_key=%s"
    private var API_KEY = "";
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

    public fun setApiKey(key : String) : Router {
        API_KEY = key
        return this;
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
            var requestInterceptor: RequestInterceptor = RequestInterceptor() {
                fun intercept(request: RequestInterceptor.RequestFacade) {
                    request.addHeader("api_key", API_KEY);
                }
            };

            var restAdapter: RestAdapter = RestAdapter.Builder()
                    .setEndpoint(DEFAULT_URL)
                    .setRequestInterceptor(requestInterceptor)
                    .build()

            var routingService = RestAdapterFactory(restAdapter).getRoutingService();
            var json: JSON = JSON();
            json.locations[0] = JSON.location()
            json.locations[1] = JSON.location()
            json.locations[0].lat = "33"
            json.locations[0].lon ="33"
            json.locations[1].lat ="33"
            json.locations[1].lon = "33"
            json.costing = this.type.toString()

            var gson : Gson  =  Gson();
        Log.d("ERER",gson.toJson(json).toString());
    //    gson.toJson(json).toString(),
        routingService.getRoute(
                    API_KEY,
                ((object: retrofit.Callback<Result> {
                    override fun failure(error: RetrofitError?) {
                        Log.d("ERROR",error!!.getUrl().toString());
                        throw UnsupportedOperationException()
                    }

                    override fun success(t: Result?, response: Response?) {
                        throw UnsupportedOperationException()
                    }

                }) as retrofit.Callback<Result> ))
//            if ( route!!.foundRoute()) {
//                callback!!.success(route as Route);
//            } else {
//                callback!!.failure(207);
//            }

    }

    public trait Callback {
        public fun success(route: Route)
        public fun failure(statusCode: Int)
    }


}
