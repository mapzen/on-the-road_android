package com.mapzen.osrm;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.squareup.okhttp.OkHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Direction {
    static public Router getRouter() {
        return new Direction.Router();
    }

    public static class Router implements Runnable {
        protected Thread runner;
        private String endpoint = "http://osrm.test.mapzen.com";
        private OkHttpClient client = new OkHttpClient();
        private Type type = Type.DRIVING;
        private List<double[]> locations = new ArrayList<double[]>();
        private int zoomLevel = 17;
        private Callback callback;

        public enum Type {
            WALKING("foot"), BIKING("bike"), DRIVING("car");
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

        public Router setZoomLevel(int zoomLevel) {
            this.zoomLevel = zoomLevel;
            return this;
        }

        private String readInputStream(InputStream in) throws IOException {
            return CharStreams.toString(new InputStreamReader(in, Charsets.UTF_8));
        }

        public URL getRouteUrl() throws MalformedURLException {
            if (locations.size() < 2) {
                throw new MalformedURLException();
            }
            String loc = "";
            for (double[] point : locations) {
                loc += "&loc="+String.valueOf(point[0])+","+String.valueOf(point[1]);
            }
            String template = "%s/%s/viaroute?z=%d&output=json&instructions=true&%s";
            return new URL(String.format(template, endpoint, type, zoomLevel, loc));
        }

        public void setCallback(Callback callback) {
            this.callback = callback;
        }

        public void fetch() throws Exception {
            if (callback == null) {
                throw new Exception("missing callback");
            }
            runner = new Thread(this);
            runner.start();
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
                callback.success(route);
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

    }
}


