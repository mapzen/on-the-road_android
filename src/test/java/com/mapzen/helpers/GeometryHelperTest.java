package com.mapzen.helpers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import android.location.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.mapzen.TestUtils.getLocation;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class GeometryHelperTest {
    @Test
    public void testGetBearing() throws Exception {
        HashMap<Integer, List<Location>> collection = new HashMap<Integer, List<Location>>();
        List<Location> l1 = new ArrayList<Location>();
        l1.add(getLocation(40.660713, -73.989341));
        l1.add(getLocation(40.659816, -73.98784));
        collection.put(128, l1);

        List<Location> l2 = new ArrayList<Location>();
        l2.add(getLocation(40.659816, -73.98784));
        l2.add(getLocation(40.660202, -73.987441));
        collection.put(38, l2);

        List<Location> l3 = new ArrayList<Location>();
        l3.add(getLocation(40.660202, -73.987441));
        l3.add(getLocation(40.660397, -73.987601));
        collection.put(328, l3);

        List<Location> l4 = new ArrayList<Location>();
        l4.add(getLocation(40.660735, -73.987878));
        l4.add(getLocation(40.661404, -73.988983));
        collection.put(309, l4);

        List<Location> l5 = new ArrayList<Location>();
        l5.add(getLocation(40.661404, -73.988983));
        l5.add(getLocation(40.660713, -73.989341));
        collection.put(201, l5);

        Iterator it = collection.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, List<Location>> entry =
                    (Map.Entry<Integer, List<Location>>) it.next();
            assertThat(Math.round(GeometryHelper.getBearing(entry.getValue().get(0),
                    entry.getValue().get(1)))).isEqualTo(Math.round(entry.getKey()));
        }
    }
}
