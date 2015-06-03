package com.mapzen.osrm;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.mapzen.helpers.DistanceFormatter;
import com.mapzen.ontheroad.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import android.location.Location;

import java.io.File;
import java.util.Locale;

import static com.mapzen.TestUtils.getLocation;
import static com.mapzen.osrm.Instruction.ENTER_AGAINST_ALLOWED_DIRECTION;
import static com.mapzen.osrm.Instruction.ENTER_ROUND_ABOUT;
import static com.mapzen.osrm.Instruction.GO_STRAIGHT;
import static com.mapzen.osrm.Instruction.HEAD_ON;
import static com.mapzen.osrm.Instruction.INSTRUCTION_COUNT;
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
import static com.mapzen.osrm.Route.SNAP_PROVIDER;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.application;

@RunWith(RobolectricTestRunner.class)
public class InstructionTest {

    private static final JSONArray JSON;

    static {JSONArray JSON1;
        try {
            JSON1 = new JSONArray(getFixture("JSON").toString());
        } catch (JSONException e) {
            e.printStackTrace();
            JSON1 = null;
        }
        JSON = JSON1;
    }

    private static final JSONArray NON_INT_TURN_JSON;

    static {JSONArray NON_INT_TURN_JSON1;
        try {
            NON_INT_TURN_JSON1 = new JSONArray(getFixture("non_int_turn_json").toString());
        } catch (JSONException e) {
            e.printStackTrace();
            NON_INT_TURN_JSON1 = null;
        }
        NON_INT_TURN_JSON = NON_INT_TURN_JSON1;
    }

    private static final JSONArray STREET_NOT_FOUND;

    static {JSONArray STREET_NOT_FOUND1;
        try {
            STREET_NOT_FOUND1 = new JSONArray(getFixture("street_not_found").toString());
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
        assertThat(instruction.getTurnInstruction()).isNotEqualTo(0);
    }

    @Test
    public void turnInstructionHasNoTurn() {
        assertThat(getInstructionWithTurn(NO_TURN).getHumanTurnInstruction(application))
                .isEqualTo(application.getString(R.string.no_turn));
    }

    @Test
    public void turnInstructionHasGoStraight() {
        assertThat(getInstructionWithTurn(GO_STRAIGHT).getHumanTurnInstruction(application))
                .isEqualTo(application.getString(R.string.go_straight));
    }

    @Test
    public void turnInstructionHasTurnSlightRight() {
        assertThat(getInstructionWithTurn(TURN_SLIGHT_RIGHT).getHumanTurnInstruction(application))
                .isEqualTo(application.getString(R.string.turn_slight_right));
    }

    @Test
    public void turnInstructionHasTurnRight() {
        assertThat(getInstructionWithTurn(TURN_RIGHT).getHumanTurnInstruction(application))
                .isEqualTo(application.getString(R.string.turn_right));
    }

    @Test
    public void turnInstructionHasTurnSharpRight() {
        assertThat(getInstructionWithTurn(TURN_SHARP_RIGHT).getHumanTurnInstruction(application))
                .isEqualTo(application.getString(R.string.turn_sharp_right));
    }

    @Test
    public void turnInstructionHasUTurn() {
        assertThat(getInstructionWithTurn(U_TURN).getHumanTurnInstruction(application))
                .isEqualTo(application.getString(R.string.u_turn));
    }

    @Test
    public void turnInstructionHasTurnSharpLeft() {
        assertThat(getInstructionWithTurn(TURN_SHARP_LEFT).getHumanTurnInstruction(application))
                .isEqualTo(application.getString(R.string.turn_sharp_left));
    }

    @Test
    public void turnInstructionHasTurnLeft() {
        assertThat(getInstructionWithTurn(TURN_LEFT).getHumanTurnInstruction(application))
                .isEqualTo(application.getString(R.string.turn_left));
    }

    @Test
    public void turnInstructionHasTurnSlightLeft() {
        assertThat(getInstructionWithTurn(TURN_SLIGHT_LEFT).getHumanTurnInstruction(application))
                .isEqualTo(application.getString(R.string.turn_slight_left));
    }

    @Test
    public void turnInstructionHasReachViaPoint() {
        assertThat(getInstructionWithTurn(REACH_VIA_POINT).getHumanTurnInstruction(application))
                .isEqualTo(application.getString(R.string.reach_via_point));
    }

    @Test
    public void turnInstructionHasHeadOn() {
        assertThat(getInstructionWithTurn(HEAD_ON).getHumanTurnInstruction(application))
                .isEqualTo(application.getString(R.string.head_on));
    }

    @Test
    public void turnInstructionHasEnterRoundAbout() {
        assertThat(getInstructionWithTurn(ENTER_ROUND_ABOUT).getHumanTurnInstruction(application))
                .isEqualTo(application.getString(R.string.enter_round_about));
    }

    @Test
    public void turnInstructionHasLeaveRoundAbout() {
        assertThat(getInstructionWithTurn(LEAVE_ROUND_ABOUT).getHumanTurnInstruction(application))
                .isEqualTo(application.getString(R.string.leave_round_about));
    }

    @Test
    public void turnInstructionHasStayOnRoundAbout() {
        assertThat(getInstructionWithTurn(STAY_ON_ROUND_ABOUT).getHumanTurnInstruction(application))
                .isEqualTo(application.getString(R.string.stay_on_round_about));
    }

    @Test
    public void turnInstructionHasStartAtEndOfStreet() {
        assertThat(getInstructionWithTurn(START_AT_END_OF_STREET).getHumanTurnInstruction(application))
                .isEqualTo(application.getString(R.string.start_at_end_of_street));
    }

    @Test
    public void turnInstructionHasReachedYourDestination() {
        assertThat(getInstructionWithTurn(YOU_HAVE_ARRIVED).getHumanTurnInstruction(application))
                .isEqualTo(application.getString(R.string.you_have_arrived));
    }

    @Test
    public void turnInstructionHasEnterAgainstAllowedDirection() {
        assertThat(getInstructionWithTurn(ENTER_AGAINST_ALLOWED_DIRECTION).getHumanTurnInstruction(application))
                .isEqualTo(application.getString(R.string.enter_against_allowed_direction));
    }

    @Test
    public void turnInstructionHasLeaveAgainstAllowedDirection() {
        assertThat(getInstructionWithTurn(LEAVE_AGAINST_ALLOWED_DIRECTION).getHumanTurnInstruction(application))
                .isEqualTo(application.getString(R.string.leave_against_allowed_direction));
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
        assertThat(instruction.getDistance()).isEqualTo(160);
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
        String actual = currentInstruction.getFullInstructionBeforeAction(application);
        assertThat(actual).isEqualTo(getExpectedFullInstructionBeforeActionFor(currentInstruction,
                "%s %s for %s"));
    }

    @Test
    public void testHeadOnFullInstructionAfterAction() throws Exception {
        Instruction currentInstruction = getInstructionWithTurn(HEAD_ON);
        String actual = currentInstruction.getFullInstructionAfterAction(application);
        String expected  = "Continue on " + instruction.getName() + " for " + DistanceFormatter.format(currentInstruction.getDistance(), true);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testGoStraightFullInstructionBeforeAction() throws Exception {
        Instruction currentInstruction = getInstructionWithTurn(GO_STRAIGHT);
        String actual = currentInstruction.getFullInstructionBeforeAction(application);
        assertThat(actual).isEqualTo(getExpectedFullInstructionBeforeActionFor(currentInstruction,
                "%s %s for %s"));
    }

    @Test
    public void testGoStraightFullInstructionAfterAction() throws Exception {
        Instruction currentInstruction = getInstructionWithTurn(GO_STRAIGHT);
        String actual = currentInstruction.getFullInstructionAfterAction(application);
        String expected  = "Continue on " + instruction.getName() + " for " + DistanceFormatter.format(currentInstruction.getDistance(), true);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testReachedYourDestinationFullInstructionBeforeAction() throws Exception {
        Instruction currentInstruction = getInstructionWithTurn(YOU_HAVE_ARRIVED);
        String actual = currentInstruction.getFullInstructionBeforeAction(application);
        assertThat(actual).isEqualTo(getExpectedFullInstructionBeforeActionFor(currentInstruction,
                "%s %s"));
    }

    @Test
    public void testReachedYourDestinationFullInstructionAfterAction() throws Exception {
        Instruction currentInstruction = getInstructionWithTurn(YOU_HAVE_ARRIVED);
        String actual = currentInstruction.getFullInstructionAfterAction(application);
        assertThat(actual).isEqualTo(getExpectedFullInstructionBeforeActionFor(currentInstruction,
                "%s %s"));
    }

    @Test
    public void testOtherFullInstructionBeforeAction() throws Exception {
        Instruction currentInstruction;
        String actual;
        for(int i = 0; i < INSTRUCTION_COUNT; i++) {
           if (i != YOU_HAVE_ARRIVED && i != GO_STRAIGHT && i != HEAD_ON) {
               currentInstruction = getInstructionWithTurn(i);
               actual = currentInstruction.getFullInstructionBeforeAction(application);
               assertThat(actual).isEqualTo(getExpectedFullInstructionBeforeActionFor(currentInstruction,
                       "%s %s and continue on for %s"));
           }
        }
    }

    @Test
    public void testOtherFullInstructionAfterAction() throws Exception {
        Instruction currentInstruction;
        String actual;
        for(int i = 0; i < INSTRUCTION_COUNT; i++) {
            if (i != YOU_HAVE_ARRIVED && i != GO_STRAIGHT && i != HEAD_ON) {
                currentInstruction = getInstructionWithTurn(i);
                actual = currentInstruction.getFullInstructionAfterAction(application);
                assertThat(actual).isEqualTo("Continue on " + currentInstruction.getName() + " for " + DistanceFormatter.format(currentInstruction.getDistance(), true));
            }
        }
    }

    @Test
    public void testSimpleInstruction() throws Exception {
        assertThat(instruction.getSimpleInstruction(application))
                .isEqualTo(application.getString(R.string.head_on) + " 19th Street");
    }

    @Test
    public void testSimpleInstructionAfterAction() throws JSONException {
        assertThat(instruction.getSimpleInstructionAfterAction(application))
                .isEqualTo("Continue on 19th Street");
    }

    @Test
    public void getFormattedDistance_shouldReturnListViewDistance() throws Exception {
        instruction.setDistance(1);
        assertThat(instruction.getFormattedDistance()).isEqualTo("3 ft");
    }

    @Test
    public void getFullInstructionBeforeAction_shouldReturnNavigationDistance() throws Exception {
        instruction.setDistance(1);
        assertThat(instruction.getFullInstructionBeforeAction(application)).contains("now");
    }

    @Test
    public void shouldHandleNonIntegerTurnInstruction() throws Exception {
        instruction = new Instruction(NON_INT_TURN_JSON);
        assertThat(instruction.getTurnInstruction()).isEqualTo(ENTER_ROUND_ABOUT);
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
        String string = instruction.getFullInstructionAfterAction(application);
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

    private Instruction getInstructionWithTurn(int turn) {
        instruction.setTurnInstruction(turn);
        return instruction;
    }

    private String getExpectedFullInstructionBeforeActionFor(Instruction currentInstruction,
            String pattern) throws JSONException {
        return String.format(Locale.ENGLISH, pattern,
                currentInstruction.getHumanTurnInstruction(application),
                currentInstruction.getName(),
                DistanceFormatter.format(currentInstruction.getDistance(), true));
    }

    private Instruction getInstructionWithDirection(String dir) throws JSONException {
        String json = "[\"10\",\"\", 1609,0,0,\"1609m\",\"" + dir + "\",\"128\"]";
        return new Instruction(new JSONArray(json));
    }

    public static String getFixture(String name) {
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
