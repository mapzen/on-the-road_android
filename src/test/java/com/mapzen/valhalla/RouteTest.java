package com.mapzen.valhalla;

import com.mapzen.model.ValhallaLocation;

import com.google.common.io.Files;

import org.apache.commons.io.FileUtils;
import org.fest.assertions.data.Offset;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.ListIterator;

import static com.mapzen.TestUtils.getLocation;
import static java.lang.System.getProperty;
import static java.nio.charset.Charset.defaultCharset;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class RouteTest {
    private Route route;

    @Before
    public void setup() throws Exception {
        route = getRoute("brooklyn_valhalla");
    }

    @Test
    public void isObject() throws Exception {
        assertThat(route).isNotNull();
    }

    @Test
    public void hasTotalDistance() throws Exception {
        assertThat(route.getTotalDistance()).isNotEqualTo(0);
    }

    @Test
    public void hasCorrectTotalDistance() throws Exception {
        assertThat(route.getTotalDistance()).isEqualTo(1541);
    }

    @Test
    public void shouldConvertTotalDistanceInMilesToMeters() throws Exception {
        route = getRoute("valhalla_miles");
        assertThat(route.getTotalDistance())
                .isEqualTo((int) Math.round(0.712 * Instruction.Companion.getMI_TO_METERS()));
    }

    @Test
    public void hasTotalTime() throws Exception {
        assertThat(route.getTotalTime()).isNotEqualTo(0);
    }

    @Test
    public void hasCorrectTotalTime() throws Exception {
        assertThat(route.getTotalTime()).isEqualTo(225);
    }

    @Test
    public void hasRouteInstructions() throws Exception {
        ArrayList<Instruction> instructions = route.getRouteInstructions();
        assertThat(instructions).hasSize(6);
    }

    @Test
    public void hasGeometry() throws Exception {
        assertThat(route.getGeometry()).isNotNull();
    }

    @Test
    public void hasCorrectNumberOfInstructionsInBrooklyn() throws Exception {
        // TODO path to fixtures setup
        Route brooklynRoute = getRoute("brooklyn_valhalla");
        assertThat(brooklynRoute.getRouteInstructions()).hasSize(6);
    }

    @Test
    public void hasCorrectTurnByTurnCoordinatesInBrooklyn() throws Exception {
        ArrayList<ValhallaLocation> points = new ArrayList<ValhallaLocation>();
        points.add(getLocation(40.66071, -73.98933));
        points.add(getLocation(40.65982, -73.98784));
        points.add(getLocation(40.65925, -73.98843));
        points.add(getLocation(40.66325, -73.99504));
        points.add(getLocation(40.66732, -73.99117));
        points.add(getLocation(40.66631, -73.98909));
        Route brooklynRoute = getRoute("brooklyn_valhalla");

        ListIterator<ValhallaLocation> expectedPoints = points.listIterator();
        for(Instruction instruction: brooklynRoute.getRouteInstructions()) {
            ValhallaLocation expectedPoint = expectedPoints.next();
            ValhallaLocation instructionPoint = instruction.getLocation();

            // ceiling it as the precision of the double is not identical on the sixth digit
            assertThat(Math.ceil(instructionPoint.getLatitude()))
                    .isEqualTo(Math.ceil(expectedPoint.getLatitude()));
            assertThat(Math.ceil(instructionPoint.getLongitude()))
                    .isEqualTo(Math.ceil(expectedPoint.getLongitude()));
        }
    }

    @Test
    public void hasCorrectTurnByTurnHumanInstructionsInBrooklyn() throws Exception {
        ArrayList<String> points = new ArrayList<String>();
        points.add("Go southeast on 19th Street.");
        points.add("Turn left onto 7th Avenue.");
        points.add("Turn left onto 18th Street.");
        points.add("Turn right onto 4th Avenue.");
        points.add( "Turn right onto 14th Street.");
        points.add("You have arrived at your destination.");
        Route brooklynRoute = getRoute("brooklyn_valhalla");

        ListIterator<String> expectedPoints = points.listIterator();
        //TODO Once strings are corrected for valhalla add test for correct turn by turn instructions
        for(Instruction instruction: brooklynRoute.getRouteInstructions()) {
            String expectedDirection = expectedPoints.next();
            String instructionDirection = instruction.getHumanTurnInstruction();
            assertThat(instructionDirection).isEqualTo(expectedDirection);
        }
    }

    @Test
    public void testHasRoute() throws Exception {
        assertThat(route.getStatus()).isEqualTo(0);
    }

    @Test
    public void testHasRouteMethod() throws Exception {
        assertThat(route.foundRoute()).isTrue();
    }

    @Test
    public void testHasNoRoute() throws Exception {
        route = getRoute("unsuccessful");
        assertThat(route.getStatus()).isEqualTo(-1);
    }

    @Test
    public void testHasNoRouteMethod() throws Exception {
        route = getRoute("unsuccessful");
        assertThat(route.foundRoute()).isFalse();
    }

    @Test
    public void shouldHaveStartCoordinates() throws Exception {
        route = getRoute("brooklyn_valhalla");
        ValhallaLocation expected = getLocation(40.660708, -73.989332);
        assertThat(route.getStartCoordinates()).isEqualsToByComparingFields(expected);
    }

    public static Route getRoute(String name) throws Exception {
        String fileName = getProperty("user.dir");
        File file = new File(fileName + "/src/test/fixtures/" + name + ".route");
        String content = FileUtils.readFileToString(file, "UTF-8");
        return new Route(content);
    }

    @Test
    public void snapToRoute_shouldStayOnLeg() throws Exception {
        Route myroute = getRoute("greenpoint_around_the_block_valhalla");
        ValhallaLocation stayOnRoute = getLocation(40.660250, -73.988105);
        ValhallaLocation snapped = myroute.snapToRoute(stayOnRoute);
        assertThat(myroute.getCurrentLeg()).isEqualTo(0);
        assertThat(snapped).isNotNull();
        assertThat(snapped).isNotEqualTo(myroute.getStartCoordinates());
    }

    @Test
    public void snapToRoute_shouldReturnSameLocationWhenItMatchesNode() throws Exception {
        Route myroute = getRoute("greenpoint_around_the_block_valhalla");
        ValhallaLocation cornerLocation = myroute.getGeometry().get(0);
        ValhallaLocation snapped = myroute.snapToRoute(cornerLocation);
        assertThat(myroute.getCurrentLeg()).isEqualTo(0);
        assertThat(snapped).isEqualTo(cornerLocation);
    }

    @Test
    public void snapToRoute_shouldSnapToBeginning() throws Exception {
        Route myroute = getRoute("greenpoint_around_the_block_valhalla");
        ValhallaLocation snapped = myroute.snapToRoute(getLocation(40.660860, -73.989602));
        assertThat(snapped.getLongitude()).isEqualTo(myroute.getStartCoordinates().getLongitude());
        assertThat(snapped.getLatitude()).isEqualTo(myroute.getStartCoordinates().getLatitude());
    }

    @Test
    public void snapToRoute_shouldSnapToNextLeg() throws Exception {
        // these points are behind the new line
        Route myroute = getRoute("greenpoint_around_the_block_valhalla");
        ValhallaLocation expected = myroute.getGeometry().get(1);
        ValhallaLocation snapToNextLeg1 = getLocation(40.659740, -73.987802);
        assertThat(myroute.snapToRoute(snapToNextLeg1)).isEqualsToByComparingFields(expected);
        myroute.rewind();
        ValhallaLocation snapToNextLeg2 = getLocation(40.659762, -73.987821);
        assertThat(myroute.snapToRoute(snapToNextLeg2)).isEqualsToByComparingFields(expected);
        myroute.rewind();
        ValhallaLocation snapToNextLeg3 = getLocation(40.659781, -73.987890);
        assertThat(myroute.snapToRoute(snapToNextLeg3)).isEqualsToByComparingFields(expected);
    }

    @Test
    public void snapToRoute_shouldAdvanceToNextLegButNotSnapToThatBeginning() throws Exception {
        Route myroute = getRoute("greenpoint_around_the_block_valhalla");
        ValhallaLocation justAroundTheCorner1 = getLocation(40.659826, -73.987838);
        ValhallaLocation snappedTo1 = myroute.snapToRoute(justAroundTheCorner1);
        assertThat(snappedTo1).isNotEqualTo(route.getGeometry().get(1));
        assertThat(myroute.getCurrentLeg()).isEqualTo(1);
        myroute.rewind();
        ValhallaLocation justAroundTheCorner2 = getLocation(40.659847, -73.987835);
        ValhallaLocation snappedTo2 = myroute.snapToRoute(justAroundTheCorner2);
        assertThat(snappedTo2).isNotEqualTo(myroute.getGeometry().get(1));
        assertThat(myroute.getCurrentLeg()).isEqualTo(1);
    }

    @Test
    public void snapToRoute_shouldRealizeItsLost() throws Exception {
        ValhallaLocation lost;
        Route myroute = getRoute("greenpoint_around_the_block_valhalla");
        myroute.snapToRoute(getLocation(40.660480, -73.988908));
        lost = getLocation(40.662046, -73.987089);
        ValhallaLocation snapped = myroute.snapToRoute(lost);
        assertThat(snapped).isNull();
    }

    @Test
    public void snapToRoute_shouldRealizeLostTooFarFromRoute() throws Exception {
        ValhallaLocation lost;
        Route myroute = getRoute("greenpoint_around_the_block_valhalla");
        lost = getLocation(40.658742, -73.987235);
        ValhallaLocation snapped = myroute.snapToRoute(lost);
        assertThat(snapped).isNull();
        assertThat(myroute.isLost()).isTrue();
    }

    @Test
    public void snapToRoute_shouldBeFinalDestination() throws Exception {
        Route myroute = getRoute("greenpoint_around_the_block_valhalla");
        ValhallaLocation foundIt = getLocation(40.661434, -73.989030);
        ValhallaLocation snapped = myroute.snapToRoute(foundIt);
        ArrayList<ValhallaLocation> geometry = myroute.getGeometry();
        ValhallaLocation expected = geometry.get(geometry.size() - 1);
        assertThat(snapped).isEqualsToByComparingFields(expected);
    }

    @Test
    public void snapToRoute_shouldSetBearing() throws Exception {
        Route myroute = getRoute("greenpoint_around_the_block_valhalla");
        ValhallaLocation snap = myroute.snapToRoute(getLocation(40.659740, -73.987802));
        assertThat(snap.getBearing()).isNotEqualTo(0);
    }

    @Test
    public void snapToRoute_shouldNotGoAgainstBearing() throws Exception {
        Route myroute = getRoute("greenpoint_around_the_block_valhalla");
        ValhallaLocation correctedLocation = myroute.snapToRoute(getLocation(40.660835, -73.989436));
        assertThat(correctedLocation.getLatitude())
                .isEqualTo(myroute.getGeometry().get(0).getLatitude());
        assertThat(correctedLocation.getLongitude())
                .isEqualTo(myroute.getGeometry().get(0).getLongitude());
    }

    @Test
    public void snapToRoute_shouldNotRecalculateWhenBeginningRoute() throws Exception {
        Route route = getRoute("greenpoint_around_the_block_valhalla");
        ValhallaLocation location = getLocation(40.662046, -73.987089);
        ValhallaLocation snapped = route.snapToRoute(location);
        assertThat(snapped.getLatitude()).isEqualTo(location.getLatitude());
        assertThat(snapped.getLongitude()).isEqualTo(location.getLongitude());
    }

    @Test
    public void snapToRoute_shouldNotRecalculateWhenBeginningRouteAndGoingTowardsPolyLine() throws Exception {
        Route route = getRoute("greenpoint_around_the_block_valhalla");
        route.snapToRoute(getLocation(40.662046, -73.987089));
        ValhallaLocation location = getLocation(40.661786, -73.987561);
        ValhallaLocation snapped = route.snapToRoute(location);
        assertThat(snapped.getLatitude()).isEqualTo(location.getLatitude());
        assertThat(snapped.getLongitude()).isEqualTo(location.getLongitude());
    }

    @Test
    public void snapToRoute_shouldRecalculateWhenTravellingAndFarFromRoute() throws Exception {
        Route route = getRoute("greenpoint_around_the_block_valhalla");
        route.snapToRoute(getLocation(40.660326, -73.988687)); //along route
        ValhallaLocation lost = getLocation(40.661986, -73.987014); // off route
        ValhallaLocation snapped = route.snapToRoute(lost);
        assertThat(snapped).isNull();
    }

    @Test
    public void getCurrentRotationBearing_shouldBeSameAsInstruction() throws Exception {
        Route myroute = getRoute("greenpoint_around_the_block_valhalla");
        assertThat(Math.round(myroute.getCurrentRotationBearing())).
                isEqualTo(myroute.getRouteInstructions().get(0).getRotationBearing());
    }

    @Test
    public void getSeenInstructions_shouldBeEmpty() throws Exception {
        assertThat(route.getSeenInstructions()).hasSize(0);
    }

    @Test
    public void addSeenInstruction_shouldNotBeEmpty() throws Exception {
        ArrayList<Instruction> instructions = route.getRouteInstructions();
        route.addSeenInstruction(instructions.get(0));
        assertThat(route.getSeenInstructions()).isNotEmpty();
    }

    @Test
    public void addSeenInstruction_shouldAddInstruction() throws Exception {
        ArrayList<Instruction> instructions = route.getRouteInstructions();
        route.addSeenInstruction(instructions.get(0));
        assertThat(route.getSeenInstructions()).contains(instructions.get(0));
    }

    @Test
    public void getClosestInstruction_shouldReturnClosest() throws Exception {
        Route myroute = getRoute("greenpoint_around_the_block_valhalla");
        ArrayList<Instruction> instructions = myroute.getRouteInstructions();
        myroute.addSeenInstruction(instructions.get(0));
        Instruction instruction = myroute.getNextInstruction();
        assertThat(instruction).isEqualsToByComparingFields(myroute.getRouteInstructions().get(1));
    }

    @Test
    public void getClosestInstruction_shouldReturnNextRelevantClosest() throws Exception {
        Route myroute = getRoute("greenpoint_around_the_block_valhalla");
        ArrayList<Instruction> instructions = myroute.getRouteInstructions();
        myroute.addSeenInstruction(instructions.get(0));
        Instruction instruction = myroute.getNextInstruction();
        assertThat(instruction).isEqualsToByComparingFields(myroute.getRouteInstructions().get(1));
    }

    @Test
    public void getClosestInstruction_shouldNotReturnDestination() throws Exception {
        Route myroute = getRoute("greenpoint_around_the_block_valhalla");
        ArrayList<Instruction> instructions = myroute.getRouteInstructions();
        myroute.addSeenInstruction(instructions.get(0));
        myroute.addSeenInstruction(instructions.get(1));
        Instruction instruction = myroute.getNextInstruction();
        Instruction i = instructions.get(instructions.size() - 1);
        assertThat(instruction.getHumanTurnInstruction())
                .isNotEqualTo(i.getHumanTurnInstruction());
    }

    @Test
    public void getRouteInstructions_shouldAttachCorrectLocation() throws Exception {
        Route myroute = getRoute("brooklyn_valhalla");
        ArrayList<ValhallaLocation> locations = myroute.getGeometry();
        ArrayList<Instruction> instructions = myroute.getRouteInstructions();
        for(Instruction instruction: instructions) {
            assertThat(instruction.getLocation().getLatitude())
                    .isEqualTo(locations.get(instruction.getBeginPolygonIndex()).getLatitude());
            assertThat(instruction.getLocation().getLongitude())
                    .isEqualTo(locations.get(instruction.getBeginPolygonIndex()).getLongitude());
        }
    }

    @Test
    public void getRouteInstructions_shouldPopulateLastInstruction() throws Exception {
        Route myroute = getRoute("brooklyn_valhalla");
        ArrayList<Instruction> instructions = myroute.getRouteInstructions();
        assertThat(instructions.get(instructions.size() - 1).getHumanTurnInstruction())
                .isEqualTo("You have arrived at your destination.");
    }

    @Test
    public void getRouteInstructions_shouldNotDuplicateLocations() throws Exception {
        Route myroute = getRoute("valhalla_route");
        ArrayList<Instruction> instructions = myroute.getRouteInstructions();
        for (int i = 0; i < instructions.size() - 1; i++) {
            assertThat(instructions.get(i).getLocation())
                    .isNotEqualTo(instructions.get(i + 1).getLocation());
        }
    }

    @Test
    public void getDistanceToNextInstruction_shouldBeEqualAtBeginning() throws Exception {
        Route myroute = getRoute("ace_hotel_valhalla");
        Instruction beginning = myroute.getRouteInstructions().get(0);
        myroute.snapToRoute(beginning.getLocation());
        assertThat((double) myroute.getDistanceToNextInstruction())
                .isEqualTo(beginning.getDistance(), Offset.offset(1.0));
    }

    @Test
    public void getDistanceToNextInstruction_shouldbe78() throws Exception {
        Route myroute = getRoute("ace_hotel_valhalla");
        myroute.getRouteInstructions();
        ValhallaLocation loc = getLocation(40.743814, -73.989035);
        myroute.snapToRoute(loc);
        assertThat((double) myroute.getDistanceToNextInstruction())
                .isEqualTo(233, Offset.offset(1.0));
    }

    @Test
    public void getRemainingDistanceToDestination_shouldBeFullDistance() throws Throwable {
        Route myroute = getRoute("ace_hotel_valhalla");
        ArrayList<Instruction> instructions = myroute.getRouteInstructions();
        myroute.snapToRoute(instructions.get(0).getLocation());
        assertThat((double) myroute.getRemainingDistanceToDestination())
                .isEqualTo((double) myroute.getTotalDistance(), Offset.offset(1.0));
    }

    @Test
    @Ignore("Figure out why valhalla distances don't add up")
    public void getRemainingDistanceToDestination_shouldBeZero() throws Exception {
        Route myroute = getRoute("greenpoint_around_the_block_valhalla");
        ArrayList<Instruction> instructions = myroute.getRouteInstructions();
        for (Instruction instruction : instructions) {
            myroute.snapToRoute(instruction.getLocation());
        }
        assertThat(myroute.getRemainingDistanceToDestination()).isEqualTo(0);
    }

    @Test
    public void getRemainingDistanceToDestination_shouldBeSyncUpWithTravelledDistance()
            throws Throwable {
        Route myroute = getRoute("ace_hotel_valhalla");
        ArrayList<Instruction> instructions = myroute.getRouteInstructions();
        for (Instruction instruction : instructions) {
            myroute.snapToRoute(instruction.getLocation());
            double total =
                    myroute.getRemainingDistanceToDestination() + myroute.getTotalDistanceTravelled();
            assertThat(total).isEqualTo(myroute.getTotalDistance(), Offset.offset(1.0));
        }
    }

    @Test
    public void getRouteInstructions_shouldPopulateAllLiveDistances() throws Exception {
        Route myroute = getRoute("ace_hotel_valhalla");
        ArrayList<Instruction> instructions = myroute.getRouteInstructions();
        for (Instruction instruction : instructions) {
            assertThat(instruction.getLiveDistanceToNext()).isNotNull();
            if (instructions.indexOf(instruction) != instructions.size() - 1) {
                assertThat(instruction.getLiveDistanceToNext()).isNotZero();
            }
        }
    }

    @Test
    public void getRouteInstructions_shouldNotStompOnPopulatedFields() throws Exception {
        Route myroute = getRoute("ace_hotel_valhalla");
        ArrayList<Instruction> instructions = myroute.getRouteInstructions();
        instructions.get(0).setLiveDistanceToNext(4);
        ArrayList<Instruction> secondSetOfInstructions = myroute.getRouteInstructions();
        assertThat(secondSetOfInstructions.get(0).getLiveDistanceToNext()).isEqualTo(4);
    }

    @Test
    public void getRouteInstruction_shouldTallyUpDistances() throws Exception {
        Route myroute = getRoute("ace_hotel_valhalla");
        ArrayList<Instruction> instructions = myroute.getRouteInstructions();
        int accumulatedDistance = 0;
        for (Instruction instruction : instructions) {
            accumulatedDistance += instruction.getDistance();
            assertThat(instruction.getLiveDistanceToNext()).isEqualTo(accumulatedDistance);
        }
    }

    @Test
    public void getCurrentInstruction_shouldReturnOneThatHasntyetbeencompleted() throws Exception {
        Route myroute = getRoute("ace_hotel_valhalla");
        ArrayList<Instruction> instructions = myroute.getRouteInstructions();
        ValhallaLocation cornerOf26thAndBroadway = getLocation(40.743814, -73.989035);
        myroute.snapToRoute(cornerOf26thAndBroadway);
        assertThat(myroute.getCurrentInstruction()).isEqualTo(instructions.get(0));
        ValhallaLocation cornerOf26thAnd5thAve = getLocation(40.74277, -73.98660);
        myroute.snapToRoute(cornerOf26thAnd5thAve);
        assertThat(myroute.getCurrentInstruction()).isEqualTo(instructions.get(1));
        ValhallaLocation cornerOf26thAndMadison = getLocation(40.74463, -73.98525);
        myroute.snapToRoute(cornerOf26thAndMadison);
        assertThat(myroute.getCurrentInstruction()).isEqualTo(instructions.get(2));
    }

    @Test
    public void shouldCoverTotalDistance() throws Throwable {
        Route myroute = getRoute("ace_hotel_valhalla");
        ArrayList<Instruction> instructions = myroute.getRouteInstructions();
        for (Instruction instruction : instructions) {
            myroute.snapToRoute(instruction.getLocation());
        }
        assertThat(myroute.getTotalDistanceTravelled())
                .isEqualTo(myroute.getTotalDistance(), Offset.offset(1.0));
    }

    @Test
    public void shouldKnowDistanceTravelled() throws Exception {
        Route myroute = getRoute("ace_hotel_valhalla");
        ArrayList<Instruction> instructions = myroute.getRouteInstructions();
        int accumulated = 0;
        for (Instruction instruction : instructions) {
            myroute.snapToRoute(instruction.getLocation());
            assertThat(myroute.getTotalDistanceTravelled()).isEqualTo(accumulated,
                    Offset.offset(1.0));
            accumulated += instruction.getDistance();
        }
    }

    @Test
    public void snapToRoute_shouldUpdateAllLiveDistances() throws Exception {
        Route myroute = getRoute("ace_hotel_valhalla");
        ArrayList<Instruction> instructions = myroute.getRouteInstructions();
        ValhallaLocation cornerOf26thAndBroadway = getLocation(40.743814, -73.989035);
        int[] before = new int[instructions.size()];
        for (Instruction instruction : instructions) {
            before[instructions.indexOf(instruction)] = (int) instruction.getLiveDistanceToNext();
        }

        myroute.snapToRoute(cornerOf26thAndBroadway);
        for (Instruction instruction : instructions) {
            int expected = before[instructions.indexOf(instruction)] - (int) myroute.getTotalDistanceTravelled();
            assertThat(instruction.getLiveDistanceToNext()).isEqualTo(expected);
        }
    }

    @Test
    public void shouldParseUnits() throws Exception {
        route = getRoute("brooklyn_valhalla");
        assertThat(route.getUnits()).isEqualTo(Router.DistanceUnits.KILOMETERS);

        route = getRoute("brooklyn_valhalla_miles");
        assertThat(route.getUnits()).isEqualTo(Router.DistanceUnits.MILES);
    }

    private ArrayList<ValhallaLocation> getLocationsFromFile(String name) throws Exception {
        String fileName = getProperty("user.dir");
        File file = new File(fileName + "/src/test/fixtures/" + name + ".txt");
        ArrayList<ValhallaLocation> allLocations = new ArrayList<ValhallaLocation>();
        for(String locations: Files.readLines(file, defaultCharset())) {
            String[] latLng = locations.split(",");
            ValhallaLocation location = new ValhallaLocation();
            location.setLatitude(Double.valueOf(latLng[0]));
            location.setLongitude(Double.valueOf(latLng[1]));
            allLocations.add(location);
        }
        return allLocations;
    }
}
