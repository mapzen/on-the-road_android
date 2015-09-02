package com.mapzen.valhalla;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.apache.commons.io.FileUtils;
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

@RunWith(RobolectricTestRunner.class)
public class InstructionTest {
    private static final JSONObject JSON;

    static {
        JSONObject JSON1;
        try {
            JSON1 = new JSONObject(getInstructionFixture("json_valhalla"));
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
            DOUBLE_STREET_NAME1 = new JSONObject(getInstructionFixture("double_street_name_valhalla"));
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
            STREET_NOT_FOUND1 = new JSONObject(getInstructionFixture("street_not_found_valhalla"));
        } catch (JSONException e) {
            e.printStackTrace();
            STREET_NOT_FOUND1 = null;
        }
        STREET_NOT_FOUND = STREET_NOT_FOUND1;
    }

    private Instruction instruction;

    @Before
    public void setup() throws Exception {
        instruction = new Instruction(JSON);
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
        Instruction i = getInstructionWithDirection("N");
        assertThat(i.getDirectionAngle()).isEqualTo(0f);
    }

    @Test
    public void hasNEdirectionAngle() throws Exception {
        Instruction i = getInstructionWithDirection("NE");
        assertThat(i.getDirectionAngle()).isEqualTo(315f);
    }

    @Test
    public void hasEdirectionAngle() throws Exception {
        Instruction i = getInstructionWithDirection("E");
        assertThat(i.getDirectionAngle()).isEqualTo(270f);
    }

    @Test
    public void hasSEdirectionAngle() throws Exception {
        Instruction i = getInstructionWithDirection("SE");
        assertThat(i.getDirectionAngle()).isEqualTo(225f);
    }

    @Test
    public void hasSdirectionAngle() throws Exception {
        Instruction i = getInstructionWithDirection("S");
        assertThat(i.getDirectionAngle()).isEqualTo(180f);
    }

    @Test
    public void hasSWdirectionAngle() throws Exception {
        Instruction i = getInstructionWithDirection("SW");
        assertThat(i.getDirectionAngle()).isEqualTo(135f);
    }

    @Test
    public void hasWdirectionAngle() throws Exception {
        Instruction i = getInstructionWithDirection("W");
        assertThat(i.getDirectionAngle()).isEqualTo(90f);
    }

    @Test
    public void hasNWdirectionAngle() throws Exception {
        Instruction i = getInstructionWithDirection("NW");
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
        assertThat(instruction.getHumanTurnInstruction()).isEqualTo("Go southeast on 19th Street.");
    }

    @Test
    public void shouldGetCorrectNameWithDualStreetNames() throws JSONException {
        Instruction ins = new Instruction(DOUBLE_STREET_NAME);
        assertThat(ins.getName()).isEqualTo("Main Street/PA 29");
    }

    @Test
    public void shouldBeEqual() throws Exception {
        Instruction instruction = new Instruction(JSON);
        instruction.setLocation(getLocation(0, 0));
        Instruction other = new Instruction(JSON);
        other.setLocation(getLocation(0, 0));
        assertThat(instruction).isEqualTo(other);
    }

    @Test
    public void shouldNotBeEqual() throws Exception {
        instruction.setLocation(getLocation(0, 0));
        Instruction other = new Instruction(DOUBLE_STREET_NAME);
        other.setLocation(getLocation(0, 0));
        assertThat(instruction).isNotEqualTo(other);
    }

    @Test
    public void getName_shouldReturnInstructionIfStreetNameNotAvailable() throws Exception {
        Instruction instruction = new Instruction(STREET_NOT_FOUND);
        String text = STREET_NOT_FOUND.getString("instruction");
        assertThat(instruction.getName()).isEqualTo(text);
    }

    // Helper methods.
    private Instruction getInstructionWithDirection(String direction) throws JSONException {
        int angle = 0;
        switch (direction) {
            case "NE":
                angle = 315;
                break;
            case "E":
                angle = 270;
                break;
            case "SE":
                angle = 225;
                break;
            case "S":
                angle = 180;
                break;
            case "SW":
                angle = 135;
                break;
            case "W":
                angle = 90;
                break;
            case "NW":
                angle = 45;
                break;
        }

        final Instruction instruction = new Instruction(JSON);
        instruction.setBearing(angle);
        return instruction;
    }

    private Route getRoute(String name) throws Exception {
        String fileName = getProperty("user.dir");
        File file = new File(fileName + "/src/test/fixtures/" + name + ".route");
        String content = FileUtils.readFileToString(file, "UTF-8");
        return new Route(content);
    }

    public static String getInstructionFixture(String name) {
        String basedir = System.getProperty("user.dir");
        File file = new File(basedir + "/src/test/fixtures/" + name);
        String fixture = "";
        try {
            fixture = Files.toString(file, Charsets.UTF_8);
        } catch (Exception e) {
            fixture = "not found";
        }
        return fixture;
    }
}
