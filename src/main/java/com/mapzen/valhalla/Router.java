package com.mapzen.valhalla;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.squareup.okhttp.OkHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class Router implements Runnable {
    private static final String DEFAULT_URL = "http://valhalla.api.dev.mapzen.com/route";
    private static final String ROUTE_PARAMS = "?json={\"locations\":"
            + "[{\"lat\":%1f,\"lon\":%2f},{\"lat\":%3f,\"lon\":%4f}],"
            + "\"costing\":\"%s\",\"output\":\"json\"}";

    private Router() {
    }

    static public Router getRouter() {
        return new Router();
    }

    private String endpoint = DEFAULT_URL;
    private OkHttpClient client = new OkHttpClient();
    private Type type = Type.DRIVING;
    private List<double[]> locations = new ArrayList<double[]>();
    private Callback callback;

    public enum Type {
        WALKING("pedestrian"), BIKING("bicycle"), DRIVING("auto");
        private String type;

        Type(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }
    }

    public Router setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public Router setWalking() {
        this.type = Type.WALKING;
        return this;
    }

    public Router setDriving() {
        this.type = Type.DRIVING;
        return this;
    }

    public Router setBiking() {
        this.type = Type.BIKING;
        return this;
    }

    public Router setLocation(double[] point) {
        this.locations.add(point);
        return this;
    }

    public Router clearLocations() {
        this.locations.clear();
        return this;
    }

    private String readInputStream(InputStream in) throws IOException {
        return CharStreams.toString(new InputStreamReader(in, Charsets.UTF_8));
    }

    public URL getRouteUrl() throws MalformedURLException, UnsupportedEncodingException {
        if (locations.size() < 2) {
            throw new MalformedURLException();
        }

        final String params = String.format(ROUTE_PARAMS,
                locations.get(0)[0],
                locations.get(0)[1],
                locations.get(1)[0],
                locations.get(1)[1],
                type);

        return new URL(endpoint + URLEncoder.encode(params, "utf-8"));
    }

    public Router setCallback(Callback callback) {
        this.callback = callback;
        return this;
    }

    public void fetch() {
        if (callback == null) {
            return;
        }
        new Thread(this).start();
    }

    @Override
    public void run() {
        InputStream in = null;
        try {
            HttpURLConnection connection = client.open(getRouteUrl());
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                callback.failure(connection.getResponseCode());
                return;
            }
            in = connection.getInputStream();
            final String responseText = readInputStream(in);
            Route route = new Route(responseText);
            if (route.foundRoute()) {
                callback.success(route);
            } else {
                callback.failure(207);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public interface Callback {
        void success(Route route);
        void failure(int statusCode);
    }
}
