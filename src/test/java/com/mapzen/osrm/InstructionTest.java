package com.mapzen.osrm;

import com.mapzen.helpers.DistanceFormatter;

import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.location.Location;

import java.util.ArrayList;
import java.util.Locale;

import static com.mapzen.TestUtils.getLocation;
import static com.mapzen.osrm.Instruction.ENTER_AGAINST_ALLOWED_DIRECTION;
import static com.mapzen.osrm.Instruction.ENTER_ROUND_ABOUT;
import static com.mapzen.osrm.Instruction.GO_STRAIGHT;
import static com.mapzen.osrm.Instruction.HEAD_ON;
import static com.mapzen.osrm.Instruction.LEAVE_AGAINST_ALLOWED_DIRECTION;
import static com.mapzen.osrm.Instruction.LEAVE_ROUND_ABOUT;
import static com.mapzen.osrm.Instruction.NO_TURN;
import static com.mapzen.osrm.Instruction.YOU_HAVE_ARRIVED;
import static com.mapzen.osrm.Instruction.REACH_VIA_POINT;
import static com.mapzen.osrm.Instruction.START_AT_END_OF_STREET;
import static com.mapzen.osrm.Instruction.STAY_ON_ROUND_ABOUT;
import static com.mapzen.osrm.Instruction.TURN_LEFT;
import static com.mapzen.osrm.Instruction.TURN_RIGHT;
import static com.mapzen.osrm.Instruction.TURN_SHARP_LEFT;
import static com.mapzen.osrm.Instruction.TURN_SHARP_RIGHT;
import static com.mapzen.osrm.Instruction.TURN_SLIGHT_LEFT;
import static com.mapzen.osrm.Instruction.TURN_SLIGHT_RIGHT;
import static com.mapzen.osrm.Instruction.U_TURN;
import static com.mapzen.osrm.Instruction.decodedInstructions;
import static com.mapzen.osrm.Route.SNAP_PROVIDER;
import static org.fest.assertions.api.Assertions.assertThat;

@Config(manifest=Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class InstructionTest {
    private static final JSONArray JSON = new JSONArray("[\n" +
            "\"10\",\n" +
            "\"19th Street\",\n" +
            "1609,\n" +
            "8,\n" +
            "0,\n" +
            "\"1609m\",\n" +
            "\"SE\",\n" +
            "128\n" +
            "]\n");

    private static final JSONArray NON_INT_TURN_JSON = new JSONArray("[\n" +
            "\"11-1\",\n" +
            "\"19th Street\",\n" +
            "1609,\n" +
            "8,\n" +
            "0,\n" +
            "\"1609m\",\n" +
            "\"SE\",\n" +
            "128\n" +
            "]\n");

    private static final JSONArray STREET_NOT_FOUND = new JSONArray("[\n"
            + "            \"11-3\",\n"
            + "            \"{\\\"highway\\\":\\\"unclassified\\\", \\\"message\\\":\\\"name not found\\\"}\",\n"
            + "            31,\n"
            + "            5345,\n"
            + "            5,\n"
            + "            \"30m\",\n"
            + "            \"N\",\n"
            + "            8\n"
            + "        ]");

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
        assertThat(instruction.getTurnInstruction()).isNotEqualTo(0);
    }

    @Test
    public void turnInstructionHasNoTurn() {
        instruction.setTurnInstruction(0);
        assertThat(instruction.getHumanTurnInstruction()).isEqualTo(NO_TURN);
    }

    @Test
    public void turnInstructionHasGoStraight() {
        instruction.setTurnInstruction(1);
        assertThat(instruction.getHumanTurnInstruction()).isEqualTo(GO_STRAIGHT);
    }

    @Test
    public void turnInstructionHasTurnSlightRight() {
        instruction.setTurnInstruction(2);
        assertThat(instruction.getHumanTurnInstruction()).isEqualTo(TURN_SLIGHT_RIGHT);
    }

    @Test
    public void turnInstructionHasTurnRight() {
        instruction.setTurnInstruction(3);
        assertThat(instruction.getHumanTurnInstruction()).isEqualTo(TURN_RIGHT);
    }

    @Test
    public void turnInstructionHasTurnSharpRight() {
        instruction.setTurnInstruction(4);
        assertThat(instruction.getHumanTurnInstruction()).isEqualTo(TURN_SHARP_RIGHT);
    }

    @Test
    public void turnInstructionHasUTurn() {
        instruction.setTurnInstruction(5);
        assertThat(instruction.getHumanTurnInstruction()).isEqualTo(U_TURN);
    }

    @Test
    public void turnInstructionHasTurnSharpLeft() {
        instruction.setTurnInstruction(6);
        assertThat(instruction.getHumanTurnInstruction()).isEqualTo(TURN_SHARP_LEFT);
    }

    @Test
    public void turnInstructionHasTurnLeft() {
        instruction.setTurnInstruction(7);
        assertThat(instruction.getHumanTurnInstruction()).isEqualTo(TURN_LEFT);
    }

    @Test
    public void turnInstructionHasTurnSlightLeft() {
        instruction.setTurnInstruction(8);
        assertThat(instruction.getHumanTurnInstruction()).isEqualTo(TURN_SLIGHT_LEFT);
    }

    @Test
    public void turnInstructionHasReachViaPoint() {
        instruction.setTurnInstruction(9);
        assertThat(instruction.getHumanTurnInstruction()).isEqualTo(REACH_VIA_POINT);
    }

    @Test
    public void turnInstructionHasHeadOn() {
        instruction.setTurnInstruction(10);
        assertThat(instruction.getHumanTurnInstruction()).isEqualTo(HEAD_ON);
    }

    @Test
    public void turnInstructionHasEnterRoundAbout() {
        instruction.setTurnInstruction(11);
        assertThat(instruction.getHumanTurnInstruction()).isEqualTo(ENTER_ROUND_ABOUT);
    }

    @Test
    public void turnInstructionHasLeaveRoundAbout() {
        instruction.setTurnInstruction(12);
        assertThat(instruction.getHumanTurnInstruction()).isEqualTo(LEAVE_ROUND_ABOUT);
    }

    @Test
    public void turnInstructionHasStayOnRoundAbout() {
        instruction.setTurnInstruction(13);
        assertThat(instruction.getHumanTurnInstruction()).isEqualTo(STAY_ON_ROUND_ABOUT);
    }

    @Test
    public void turnInstructionHasStartAtEndOfStreet() {
        instruction.setTurnInstruction(14);
        assertThat(instruction.getHumanTurnInstruction()).isEqualTo(START_AT_END_OF_STREET);
    }

    @Test
    public void turnInstructionHasReachedYourDestination() {
        instruction.setTurnInstruction(15);
        assertThat(instruction.getHumanTurnInstruction()).isEqualTo(YOU_HAVE_ARRIVED);
    }

    @Test
    public void turnInstructionHasEnterAgainstAllowedDirection() {
        instruction.setTurnInstruction(16);
        assertThat(instruction.getHumanTurnInstruction()).isEqualTo(ENTER_AGAINST_ALLOWED_DIRECTION);
    }

    @Test
    public void turnInstructionHasLeaveAgainstAllowedDirection() {
        instruction.setTurnInstruction(17);
        assertThat(instruction.getHumanTurnInstruction()).isEqualTo(LEAVE_AGAINST_ALLOWED_DIRECTION);
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
    public void getName_returnXwhenNotClassifed() throws Exception {
        instruction = new Instruction(STREET_NOT_FOUND);
        assertThat(instruction.getName()).isEqualTo("unclassified");
    }

    @Test
    public void hasDistance() throws Exception {
        assertThat(instruction.getDistance()).isGreaterThan(-1);
    }

    @Test
    public void hasCorrectDistance() throws Exception {
        assertThat(instruction.getDistance()).isEqualTo(1609);
    }

    @Test
    public void hasDirection() throws Exception {
        assertThat(instruction.getDirection()).isNotNull();
    }

    @Test
    public void hasCorrectDirection() throws Exception {
        assertThat(instruction.getDirection()).isEqualTo("SE");
    }


    @Test
    public void hasPolygonIndex() throws Exception {
        assertThat(instruction.getPolygonIndex()).isEqualTo(JSON.getInt(3));
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
    public void hasDirectionAngle() throws Exception {
        String json = "[\"10\",\"\", 1609,0,0,\"1609m\",\"SE\",\"128\"]";
        JSONArray jsonArray = new JSONArray(json);
        instruction = new Instruction(jsonArray);
        assertThat(instruction.getDirection()).isEqualTo("SE");
    }

    @Test
    public void hasRotationBearingAngle() throws Exception {
        String json = "[\"10\",\"\", 1609,0,0,\"1609m\",\"SE\",\"128\"]";
        JSONArray jsonArray = new JSONArray(json);
        instruction = new Instruction(jsonArray);
        assertThat(instruction.getRotationBearing()).isEqualTo(360 - 128);
    }

    @Test
    public void hasBearingAngle() throws Exception {
        String json = "[\"10\",\"\", 1609,0,0,\"1609m\",\"SE\",\"128\"]";
        JSONArray jsonArray = new JSONArray(json);
        instruction = new Instruction(jsonArray);
        assertThat(instruction.getBearing()).isEqualTo(128);
    }

    @Test
    public void hasPointCoordinates() throws Exception {
        assertThat(instruction.getLocation()).isNotNull();
    }

    @Test
    public void canSetCoordinates() throws Exception {
        Location expected = new Location(SNAP_PROVIDER);
        expected.setLatitude(3.3);
        expected.setLongitude(4.4);
        instruction.setLocation(expected);
        assertThat(instruction.getLocation()).isEqualTo(expected);

    }

    @Test
    public void testHeadOnFullInstructionBeforeAction() throws Exception {
        Instruction currentInstruction = getInstructionWithTurn(HEAD_ON);
        String actual = currentInstruction.getFullInstructionBeforeAction();
        assertThat(actual).isEqualTo(getExpectedFullInstructionBeforeActionFor(currentInstruction,
                "%s %s for %s"));
    }

    @Test
    public void testHeadOnFullInstructionAfterAction() throws Exception {
        Instruction currentInstruction = getInstructionWithTurn(HEAD_ON);
        String actual = currentInstruction.getFullInstructionAfterAction();
        String expected  = "Continue on " + instruction.getName() + " for " + DistanceFormatter.format(currentInstruction.getDistance(), true);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testGoStraightFullInstructionBeforeAction() throws Exception {
        Instruction currentInstruction = getInstructionWithTurn(GO_STRAIGHT);
        String actual = currentInstruction.getFullInstructionBeforeAction();
        assertThat(actual).isEqualTo(getExpectedFullInstructionBeforeActionFor(currentInstruction,
                "%s %s for %s"));
    }

    @Test
    public void testGoStraightFullInstructionAfterAction() throws Exception {
        Instruction currentInstruction = getInstructionWithTurn(GO_STRAIGHT);
        String actual = currentInstruction.getFullInstructionAfterAction();
        String expected  = "Continue on " + instruction.getName() + " for " + DistanceFormatter.format(currentInstruction.getDistance(), true);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testReachedYourDestinationFullInstructionBeforeAction() throws Exception {
        Instruction currentInstruction = getInstructionWithTurn(YOU_HAVE_ARRIVED);
        String actual = currentInstruction.getFullInstructionBeforeAction();
        assertThat(actual).isEqualTo(getExpectedFullInstructionBeforeActionFor(currentInstruction,
                "%s %s"));
    }

    @Test
    public void testReachedYourDestinationFullInstructionAfterAction() throws Exception {
        Instruction currentInstruction = getInstructionWithTurn(YOU_HAVE_ARRIVED);
        String actual = currentInstruction.getFullInstructionAfterAction();
        assertThat(actual).isEqualTo(getExpectedFullInstructionBeforeActionFor(currentInstruction,
                "%s %s"));
    }

    @Test
    public void testOtherFullInstructionBeforeAction() throws Exception {
        Instruction currentInstruction;
        String actual;
        for(int i = 0; i < decodedInstructions.length; i++) {
           if (!decodedInstructions[i].equals(YOU_HAVE_ARRIVED) &&
                   !decodedInstructions[i].equals(GO_STRAIGHT) &&
                       !decodedInstructions[i].equals(HEAD_ON)) {
               currentInstruction = getInstructionWithTurn(decodedInstructions[i]);
               actual = currentInstruction.getFullInstructionBeforeAction();
               assertThat(actual).isEqualTo(getExpectedFullInstructionBeforeActionFor(currentInstruction,
                       "%s %s and continue on for %s"));
           }
        }
    }

    @Test
    public void testOtherFullInstructionAfterAction() throws Exception {
        Instruction currentInstruction;
        String actual;
        for(int i = 0; i < decodedInstructions.length; i++) {
            if (!decodedInstructions[i].equals(YOU_HAVE_ARRIVED) &&
                    !decodedInstructions[i].equals(GO_STRAIGHT) &&
                    !decodedInstructions[i].equals(HEAD_ON)) {
                currentInstruction = getInstructionWithTurn(decodedInstructions[i]);
                actual = currentInstruction.getFullInstructionAfterAction();
                assertThat(actual).isEqualTo("Continue on " + currentInstruction.getName() + " for " + DistanceFormatter.format(currentInstruction.getDistance(), true));
            }
        }
    }

    @Test
    public void testSimpleInstruction() throws Exception {
        assertThat(instruction.getSimpleInstruction()).isEqualTo("Head on 19th Street");
    }

    @Test
    public void testSimpleInstructionAfterAction() {
        assertThat(instruction.getSimpleInstructionAfterAction()).isEqualTo("Continue on 19th Street");
    }

    @Test
    public void getFormattedDistance_shouldReturnListViewDistance() throws Exception {
        instruction.setDistance(1);
        assertThat(instruction.getFormattedDistance()).isEqualTo("3 ft");
    }

    @Test
    public void getFullInstructionBeforeAction_shouldReturnNavigationDistance() throws Exception {
        instruction.setDistance(1);
        assertThat(instruction.getFullInstructionBeforeAction()).contains("now");
    }

    @Test
    public void shouldHandleNonIntegerTurnInstruction() throws Exception {
        instruction = new Instruction(NON_INT_TURN_JSON);
        assertThat(instruction.getHumanTurnInstruction())
                .isEqualTo(ENTER_ROUND_ABOUT);
    }

    @Test
    public void shouldBeEqual() throws Exception {
        Instruction instruction = new Instruction(NON_INT_TURN_JSON);
        instruction.setLocation(getLocation(0, 0));
        Instruction other = new Instruction(NON_INT_TURN_JSON);
        other.setLocation(getLocation(0, 0));
        assertThat(instruction).isEqualTo(other);
    }

    @Test
    public void shouldNotBeEqual() throws Exception {
        instruction.setLocation(getLocation(0, 0));
        Instruction other = new Instruction(NON_INT_TURN_JSON);
        other.setLocation(getLocation(0, 0));
        assertThat(instruction).isNotEqualTo(other);
    }

    @Test
    public void getFullInstructionAfterActionWithLocation_shouldUseTermNow() throws Exception {
        Location location = getLocation(0, 0);
        instruction.setLocation(location);
        instruction.setDistance(1);
        String string = instruction.getFullInstructionAfterAction();
        assertThat(string).doesNotContain("now");
    }

    @Test
    public void skip_shouldBeTrue() throws Exception {
        Instruction skipInstruction = new Instruction(STREET_NOT_FOUND);
        assertThat(skipInstruction.skip()).isTrue();
    }

    @Test
    public void skip_shouldBeFalse() throws Exception {
        Instruction skipInstruction = new Instruction(JSON);
        assertThat(skipInstruction.skip()).isFalse();
    }

    // Helper methods.

    private Instruction getInstructionWithTurn(String turn) {
        ArrayList<String> withIndex = new ArrayList<String>(decodedInstructions.length);
        for(int i = 0; i < decodedInstructions.length; i++) {
            withIndex.add(decodedInstructions[i]);
        }
        instruction.setTurnInstruction(withIndex.indexOf(turn));
        return instruction;
    }

    private String getExpectedFullInstructionBeforeActionFor(Instruction currentInstruction,
            String pattern) {
        return String.format(Locale.ENGLISH, pattern,
                currentInstruction.getHumanTurnInstruction(),
                currentInstruction.getName(),
                DistanceFormatter.format(currentInstruction.getDistance(), true));
    }

    private Instruction getInstructionWithDirection(String dir) {
        String json = "[\"10\",\"\", 1609,0,0,\"1609m\",\"" + dir + "\",\"128\"]";
        return new Instruction(new JSONArray(json));
    }
}
