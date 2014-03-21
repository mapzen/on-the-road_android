package com.mapzen.osrm;

import org.junit.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class RouteHelperTest {
    @Test
    public void testGetBearing() throws Exception {
        HashMap<Integer, double[][]> collection = new HashMap<Integer, double[][]>();
        collection.put(128, new double[][] {{40.660713, -73.989341}, {40.659816, -73.98784}});
        collection.put(38, new double[][] {{40.659816, -73.98784}, {40.660202, -73.987441}});
        collection.put(328, new double[][] {{40.660202, -73.987441}, {40.660397, -73.987601}});
        collection.put(309, new double[][] {{40.660735, -73.987878}, {40.661404, -73.988983}});
        collection.put(201, new double[][] {{40.661404, -73.988983}, {40.660713, -73.989341}});

        Iterator it = collection.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<Integer, double[][]> entry =
                    (Map.Entry<Integer, double[][]>)  it.next();
            assertThat(Math.round(RouteHelper.getBearing(entry.getValue()[0],
                    entry.getValue()[1]))).isEqualTo(Math.round(entry.getKey()));
        }
    }
}
