package com.mapzen.valhalla;

import com.mapzen.helpers.DistanceFormatter;
import com.mapzen.ontheroad.R;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import android.location.Location;

import java.io.File;
import java.util.Locale;

import static com.mapzen.TestUtils.getLocation;
import static java.lang.System.getProperty;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.application;


@RunWith(RobolectricTestRunner.class)
public class InstructionTest {
    private static final JSONObject JSON;

    static {
        JSONObject JSON1;
        try {
            JSON1 = new JSONObject("{\n" +
                    "begin_shape_index: 0,\n" +
                    "length: 0.161,\n" +
                    "end_shape_index: 1,\n" +
                    "instruction: \"Go southeast on 19th Street.\",\n" +
                    "street_names: [\n" +
                    "\"19th Street\"\n" +
                    "],\n" +
                    "type: 1,\n" +
                    "time: 0\n" +
                    "}");
        } catch (JSONException e) {
            e.printStackTrace();
            JSON1 = null;
        }
        JSON = JSON1;
    }

    private static final JSONObject DOUBLE_STREET_NAME;

    static {
        JSONObject DOUBLE_STREET_NAME1;
        try {
            DOUBLE_STREET_NAME1 = new JSONObject(" {  \n" +
                    "                  \"begin_shape_index\":241,\n" +
                    "                  \"length\":0.466,\n" +
                    "                  \"end_shape_index\":249,\n" +
                    "                  \"instruction\":\"Turn left onto Main Street\\/PA 29.\",\n" +
                    "                  \"street_names\":[  \n" +
                    "                     \"Main Street\",\n" +
                    "                     \"PA 29\"\n" +
                    "                  ],\n" +
                    "                  \"type\":15,\n" +
                    "                  \"time\":329\n" +
                    "               }");
        } catch (JSONException e) {
            e.printStackTrace();
            DOUBLE_STREET_NAME1 = null;
        }
        DOUBLE_STREET_NAME = DOUBLE_STREET_NAME1;
    }

    private static final JSONObject STREET_NOT_FOUND;

    static {
        JSONObject STREET_NOT_FOUND1;
        try {
            STREET_NOT_FOUND1 = new JSONObject(" {  \n" +
                    "                  \"begin_shape_index\":219,\n" +
                    "                  \"length\":0.166,\n" +
                    "                  \"end_shape_index\":222,\n" +
                    "                  \"instruction\":\"Continue.\",\n" +
                    "                  \"type\":8,\n" +
                    "                  \"time\":117\n" +
                    "               }");
        } catch (JSONException e) {
            e.printStackTrace();
            STREET_NOT_FOUND1 = null;
        }
        STREET_NOT_FOUND = STREET_NOT_FOUND1;
    }

    private com.mapzen.valhalla.Instruction instruction;

    @Before
    public void setup() throws Exception {
        instruction = new com.mapzen.valhalla.Instruction(JSON);
        Locale.setDefault(Locale.US);
    }

    @Test
    public void isObject() throws Exception {
        assertThat(instruction).isNotNull();
    }

    @Test
    public void hasTurnInstruction() throws Exception {
        assertThat(instruction.getTurnInstruction()).isNotEqualTo(0);
    }

    @Test
    public void hasCorrectTurnInstruction() throws Exception {
        assertThat(instruction.getTurnInstruction()).isEqualTo(1);
    }

    @Test
    public void hasName() throws Exception {
        assertThat(instruction.getName()).isNotNull();
    }

    @Test
    public void hasCorrectName() throws Exception {
        assertThat(instruction.getName()).isEqualTo("19th Street");
    }

    @Test
    public void hasDistance() throws Exception {
        assertThat(instruction.getDistance()).isGreaterThan(-1);
    }

    @Test
    public void hasCorrectDistance() throws Exception {
        assertThat(instruction.getDistance()).isEqualTo(161);
    }

    @Test
    public void hasDirection() throws Exception {
        assertThat(instruction.getDirection()).isNotNull();
    }

    @Test
    public void hasCorrectDirection() throws Exception {
        Route myRoute = getRoute("brooklyn_valhalla");
        instruction = myRoute.getCurrentInstruction();
        assertThat(instruction.getDirection()).isEqualTo("W");
    }


    @Test
    public void hasBeginPolygonIndex() throws Exception {
        assertThat(instruction.getBeginPolygonIndex()).isEqualTo(0);
    }

    @Test
    public void hasEndPolygonIndex() throws Exception {
        assertThat(instruction.getEndPolygonIndex()).isEqualTo(1);
    }

    @Test
    public void hasNdirectionAngle() throws Exception {
        com.mapzen.valhalla.Instruction i = getInstructionWithDirection("N");
        assertThat(i.getDirectionAngle()).isEqualTo(0f);
    }

    @Test
    public void hasNEdirectionAngle() throws Exception {
        com.mapzen.valhalla.Instruction i = getInstructionWithDirection("NE");
        assertThat(i.getDirectionAngle()).isEqualTo(315f);
    }

    @Test
    public void hasEdirectionAngle() throws Exception {
        com.mapzen.valhalla.Instruction i = getInstructionWithDirection("E");
        assertThat(i.getDirectionAngle()).isEqualTo(270f);
    }

    @Test
    public void hasSEdirectionAngle() throws Exception {
        com.mapzen.valhalla.Instruction i = getInstructionWithDirection("SE");
        assertThat(i.getDirectionAngle()).isEqualTo(225f);
    }

    @Test
    public void hasSdirectionAngle() throws Exception {
        com.mapzen.valhalla.Instruction i = getInstructionWithDirection("S");
        assertThat(i.getDirectionAngle()).isEqualTo(180f);
    }

    @Test
    public void hasSWdirectionAngle() throws Exception {
        com.mapzen.valhalla.Instruction i = getInstructionWithDirection("SW");
        assertThat(i.getDirectionAngle()).isEqualTo(135f);
    }

    @Test
    public void hasWdirectionAngle() throws Exception {
        com.mapzen.valhalla.Instruction i = getInstructionWithDirection("W");
        assertThat(i.getDirectionAngle()).isEqualTo(90f);
    }

    @Test
    public void hasNWdirectionAngle() throws Exception {
        com.mapzen.valhalla.Instruction i = getInstructionWithDirection("NW");
        assertThat(i.getDirectionAngle()).isEqualTo(45f);
    }

    @Test
    public void hasRotationBearingAngle() throws Exception {
        Route myRoute = getRoute("brooklyn_valhalla");
        instruction = myRoute.getCurrentInstruction();
        assertThat(instruction.getRotationBearing()).isEqualTo(360 - 129);
    }

    @Test
    public void hasBearingAngle() throws Exception {
        Route myRoute = getRoute("brooklyn_valhalla");
        instruction = myRoute.getCurrentInstruction();
        assertThat(instruction.getBearing()).isEqualTo(129);
    }

    @Test
    public void hasPointCoordinates() throws Exception {
        assertThat(instruction.getLocation()).isNotNull();
    }

    @Test
    public void canSetCoordinates() throws Exception {
        Location expected = new Location("snap");
        expected.setLatitude(3.3);
        expected.setLongitude(4.4);
        instruction.setLocation(expected);
        assertThat(instruction.getLocation()).isEqualTo(expected);

    }

    @Test
    public void shouldGetCorrectHumanTurnInstruction() {
        assertThat(instruction.getHumanTurnInstruction(application)).isEqualTo("Go southeast on 19th Street.");
    }

    @Test
    public void shouldGetCorrectNameWithDualStreetNames() throws JSONException {
        Instruction ins = new com.mapzen.valhalla.Instruction(DOUBLE_STREET_NAME);
        assertThat(ins.getName()).isEqualTo("Main Street/PA 29");
    }

    @Test
    public void shouldBeEqual() throws Exception {
        com.mapzen.valhalla.Instruction instruction = new com.mapzen.valhalla.Instruction(JSON);
        instruction.setLocation(getLocation(0, 0));
        com.mapzen.valhalla.Instruction other = new com.mapzen.valhalla.Instruction(JSON);
        other.setLocation(getLocation(0, 0));
        assertThat(instruction).isEqualTo(other);
    }

    @Test
    public void shouldNotBeEqual() throws Exception {
        instruction.setLocation(getLocation(0, 0));
        com.mapzen.valhalla.Instruction other = new com.mapzen.valhalla.Instruction(DOUBLE_STREET_NAME);
        other.setLocation(getLocation(0, 0));
        assertThat(instruction).isNotEqualTo(other);
    }

    @Test
    public void skip_shouldBeTrue() throws Exception {
        com.mapzen.valhalla.Instruction skipInstruction = new com.mapzen.valhalla.Instruction(STREET_NOT_FOUND);
        assertThat(skipInstruction.skip()).isTrue();
    }

    @Test
    public void skip_shouldBeFalse() throws Exception {
        com.mapzen.valhalla.Instruction skipInstruction = new com.mapzen.valhalla.Instruction(JSON);
        assertThat(skipInstruction.skip()).isFalse();
    }

    // Helper methods.
    private com.mapzen.valhalla.Instruction getInstructionWithDirection(String direction) throws JSONException {
        int angle = 0;
        if (direction.equals("NE")) {
            angle = 315;
        } else if (direction.equals("E")) {
            angle = 270;
        } else if (direction.equals("SE")) {
            angle = 225;
        } else if (direction.equals("S")) {
            angle = 180;
        } else if (direction.equals("SW")) {
            angle = 135;
        } else if (direction.equals("W")) {
            angle = 90;
        } else if (direction.equals("NW")) {
            angle = 45;
        }
        Instruction ins = new com.mapzen.valhalla.Instruction(JSON);
        ins.setBearing(angle);
        return ins;
    }

    private com.mapzen.valhalla.Route getRoute(String name) throws Exception {
        String fileName = getProperty("user.dir");
        File file = new File(fileName + "/src/test/fixtures/" + name + ".route");
        String content = FileUtils.readFileToString(file, "UTF-8");
        return new Route(content);
    }
}


