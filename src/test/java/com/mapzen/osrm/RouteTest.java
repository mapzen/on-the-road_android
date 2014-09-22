package com.mapzen.osrm;

import org.apache.commons.io.FileUtils;
import org.fest.assertions.data.Offset;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.location.Location;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.ListIterator;

import static com.mapzen.TestUtils.getLocation;
import static java.lang.System.getProperty;
import static java.nio.charset.Charset.defaultCharset;
import static org.fest.assertions.api.Assertions.assertThat;

@Config(manifest=Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class RouteTest {
    private Route route;

    @Before
    public void setup() throws Exception {
        route = getRoute("brooklyn");
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
        assertThat(route.getTotalDistance()).isEqualTo(1721);
    }

    @Test
    public void hasTotalTime() throws Exception {
        assertThat(route.getTotalTime()).isNotEqualTo(0);
    }

    @Test
    public void hasCorrectTotalTime() throws Exception {
        assertThat(route.getTotalTime()).isEqualTo(128);
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
        Route brooklynRoute = getRoute("brooklyn");
        assertThat(brooklynRoute.getRouteInstructions()).hasSize(6);
    }

    @Test
    public void hasCorrectTurnByTurnCoordinatesInBrooklyn() throws Exception {
        ArrayList<Location> points = new ArrayList<Location>();
        points.add(getLocation(40.66071, -73.98933));
        points.add(getLocation(40.65982, -73.98784));
        points.add(getLocation(40.65925, -73.98843));
        points.add(getLocation(40.66325, -73.99504));
        points.add(getLocation(40.66732, -73.99117));
        points.add(getLocation(40.66631, -73.98909));
        Route brooklynRoute = getRoute("brooklyn");

        ListIterator<Location> expectedPoints = points.listIterator();
        for(Instruction instruction: brooklynRoute.getRouteInstructions()) {
            Location expectedPoint = expectedPoints.next();
            Location instructionPoint = instruction.getLocation();

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
        points.add("Head on");
        points.add("Right on");
        points.add("Right on");
        points.add("Right on");
        points.add("Right on");
        points.add("You have arrived");
        Route brooklynRoute = getRoute("brooklyn");

        ListIterator<String> expectedPoints = points.listIterator();
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
        assertThat(route.getStatus()).isEqualTo(207);
    }

    @Test
    public void testHasNoRouteMethod() throws Exception {
        route = getRoute("unsuccessful");
        assertThat(route.foundRoute()).isFalse();
    }

    @Test
    public void shouldHaveStartCoordinates() throws Exception {
        route = getRoute("brooklyn");
        Location expected = getLocation(40.660708, -73.989332);
        assertThat(route.getStartCoordinates()).isEqualsToByComparingFields(expected);
    }

    private Route getRoute(String name) throws Exception {
        String fileName = getProperty("user.dir");
        File file = new File(fileName + "/src/test/fixtures/" + name + ".route");
        String content = FileUtils.readFileToString(file, "UTF-8");
        return new Route(content);
    }

    @Test
    public void snapToRoute_shouldStayOnLeg() throws Exception {
        Route myroute = getRoute("greenpoint_around_the_block");
        Location stayOnRoute = getLocation(40.660250, -73.988105);
        Location snapped = myroute.snapToRoute(stayOnRoute);
        assertThat(myroute.getCurrentLeg()).isEqualTo(0);
        assertThat(snapped).isNotNull();
        assertThat(snapped).isNotEqualTo(myroute.getStartCoordinates());
    }

    @Test
    public void snapToRoute_shouldReturnSameLocationWhenItMatchesNode() throws Exception {
        Route myroute = getRoute("greenpoint_around_the_block");
        Location cornerLocation = myroute.getGeometry().get(0);
        Location snapped = myroute.snapToRoute(cornerLocation);
        assertThat(myroute.getCurrentLeg()).isEqualTo(0);
        assertThat(snapped).isEqualTo(cornerLocation);
    }

    @Test
    public void snapToRoute_shouldSnapToBeginning() throws Exception {
        Route myroute = getRoute("greenpoint_around_the_block");
        Location snapped = myroute.snapToRoute(getLocation(40.660860, -73.989602));
        assertThat(snapped.getLatitude()).isEqualTo(myroute.getStartCoordinates().getLatitude());
        assertThat(snapped.getLongitude()).isEqualTo(myroute.getStartCoordinates().getLongitude());
    }

    @Test
    public void snapToRoute_shouldSnapToNextLeg() throws Exception {
        // these points are behind the new line
        Route myroute = getRoute("greenpoint_around_the_block");
        Location expected = myroute.getGeometry().get(1);
        Location snapToNextLeg1 = getLocation(40.659740, -73.987802);
        assertThat(myroute.snapToRoute(snapToNextLeg1)).isEqualsToByComparingFields(expected);
        myroute.rewind();
        Location snapToNextLeg2 = getLocation(40.659762, -73.987821);
        assertThat(myroute.snapToRoute(snapToNextLeg2)).isEqualsToByComparingFields(expected);
        myroute.rewind();
        Location snapToNextLeg3 = getLocation(40.659781, -73.987890);
        assertThat(myroute.snapToRoute(snapToNextLeg3)).isEqualsToByComparingFields(expected);
    }

    @Test
    public void snapToRoute_shouldAdvanceToNextLegButNotSnapToThatBeginning() throws Exception {
        Route myroute = getRoute("greenpoint_around_the_block");
        Location justAroundTheCorner1 = getLocation(40.659826, -73.987838);
        Location snappedTo1 = myroute.snapToRoute(justAroundTheCorner1);
        assertThat(snappedTo1).isNotEqualTo(route.getGeometry().get(1));
        assertThat(myroute.getCurrentLeg()).isEqualTo(1);
        myroute.rewind();
        Location justAroundTheCorner2 = getLocation(40.659847, -73.987835);
        Location snappedTo2 = myroute.snapToRoute(justAroundTheCorner2);
        assertThat(snappedTo2).isNotEqualTo(myroute.getGeometry().get(1));
        assertThat(myroute.getCurrentLeg()).isEqualTo(1);
    }

    @Test
    public void snapToRoute_shouldRealizeItsLost() throws Exception {
        Location lost;
        Route myroute = getRoute("greenpoint_around_the_block");
        lost = getLocation(40.662046, -73.987089);
        Location snapped = myroute.snapToRoute(lost);
        assertThat(snapped).isNull();
    }

    @Test
    public void snapToRoute_shouldRealizeLostTooFarFromRoute() throws Exception {
        Location lost;
        Route myroute = getRoute("greenpoint_around_the_block");
        lost = getLocation(40.658742, -73.987235);
        Location snapped = myroute.snapToRoute(lost);
        assertThat(snapped).isNull();
        assertThat(myroute.isLost()).isTrue();
    }

    @Test
    public void snapToRoute_shouldBeFinalDestination() throws Exception {
        Route myroute = getRoute("greenpoint_around_the_block");
        Location foundIt = getLocation(40.661434, -73.989030);
        Location snapped = myroute.snapToRoute(foundIt);
        ArrayList<Location> geometry = myroute.getGeometry();
        Location expected = geometry.get(geometry.size() - 1);
        assertThat(snapped).isEqualsToByComparingFields(expected);
    }

    @Test
    public void getCurrentRotationBearing_shouldBeSameAsInstruction() throws Exception {
        Route myroute = getRoute("greenpoint_around_the_block");
        assertThat(Math.round(myroute.getCurrentRotationBearing())).
                isEqualTo(myroute.getRouteInstructions().get(0).getRotationBearing());
    }

    @Test
    public void snapToRoute_shouldHandleSharpTurn() throws Exception {
        Route myroute = getRoute("sharp_turn");
        Location aroundSharpTurn = getLocation(40.687052, -73.976300);
        Location snapped = myroute.snapToRoute(aroundSharpTurn);
        // TODO ... handle this case
        //assertThat(myroute.getCurrentLeg()).isEqualTo(2);
    }

    @Test
    public void snapToRoute_shouldNotGoAgainstBearing() throws Exception {
        Route myroute = getRoute("greenpoint_around_the_block");
        Location correctedLocation = myroute.snapToRoute(getLocation(40.660835, -73.989436));
        assertThat(correctedLocation.getLatitude())
                .isEqualTo(myroute.getGeometry().get(0).getLatitude());
        assertThat(correctedLocation.getLongitude())
                .isEqualTo(myroute.getGeometry().get(0).getLongitude());
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
        Route myroute = getRoute("greenpoint_around_the_block");
        ArrayList<Instruction> instructions = myroute.getRouteInstructions();
        myroute.addSeenInstruction(instructions.get(0));
        Instruction instruction = myroute.getNextInstruction();
        assertThat(instruction).isEqualsToByComparingFields(myroute.getRouteInstructions().get(1));
    }

    @Test
    public void getClosestInstruction_shouldReturnNextRelevantClosest() throws Exception {
        Route myroute = getRoute("greenpoint_around_the_block");
        ArrayList<Instruction> instructions = myroute.getRouteInstructions();
        myroute.addSeenInstruction(instructions.get(0));
        Instruction instruction = myroute.getNextInstruction();
        assertThat(instruction).isEqualsToByComparingFields(myroute.getRouteInstructions().get(1));
    }

    @Test
    public void getClosestInstruction_shouldNotReturnDestination() throws Exception {
        Route myroute = getRoute("to_the_armory");
        ArrayList<Instruction> instructions = myroute.getRouteInstructions();
        myroute.addSeenInstruction(instructions.get(0));
        myroute.addSeenInstruction(instructions.get(1));
        Instruction instruction = myroute.getNextInstruction();
        Instruction i = instructions.get(instructions.size() - 1);
        assertThat(instruction.getFullInstruction()).isNotEqualTo(i.getFullInstruction());
    }

    @Test
    public void getRouteInstructions_shouldAttachCorrectLocation() throws Exception {
        Route myroute = getRoute("ny_to_vermount");
        ArrayList<Location> locations = myroute.getGeometry();
        ArrayList<Instruction> instructions = myroute.getRouteInstructions();
        for(Instruction instruction: instructions) {
            assertThat(instruction.getLocation().getLatitude())
                    .isEqualTo(locations.get(instruction.getPolygonIndex()).getLatitude());
            assertThat(instruction.getLocation().getLongitude())
                    .isEqualTo(locations.get(instruction.getPolygonIndex()).getLongitude());
        }
    }

    @Test
    public void getRouteInstructions_shouldPopulateLastInstruction() throws Exception {
        Route myroute = getRoute("last_instruction_at_last_point");
        ArrayList<Instruction> instructions = myroute.getRouteInstructions();
        assertThat(instructions.get(instructions.size() - 1).getHumanTurnInstruction())
                .isEqualTo("You have arrived");
    }

    @Test
    public void getRouteInstructions_shouldNotDuplicateLocations() throws Exception {
        Route myroute = getRoute("last_instruction_at_last_point");
        ArrayList<Instruction> instructions = myroute.getRouteInstructions();
        for (int i = 0; i < instructions.size() - 1; i++) {
            assertThat(instructions.get(i).getLocation())
                    .isNotEqualTo(instructions.get(i + 1).getLocation());
        }
    }

    @Test
    public void shouldHandleMaine() throws Exception {
        Route myroute = getRoute("maine");
        ArrayList<Location> locations = getLocationsFromFile("locations");
        for(Location location: locations) {
            assertThat(myroute.snapToRoute(location)).isNotNull();
        }
    }

    @Test
    public void getDistanceToNextInstruction_shouldBeEqualAtBeginning() throws Exception {
        Route myroute = getRoute("ace_hotel");
        Instruction beginning = myroute.getRouteInstructions().get(0);
        myroute.snapToRoute(beginning.getLocation());
        assertThat((double) myroute.getDistanceToNextInstruction())
                .isEqualTo(beginning.getDistance(), Offset.offset(1.0));
    }

    @Test
    public void getDistanceToNextInstruction_shouldbe78() throws Exception {
        Route myroute = getRoute("ace_hotel");
        myroute.getRouteInstructions();
        Location loc = getLocation(40.743814, -73.989035);
        myroute.snapToRoute(loc);
        assertThat((double) myroute.getDistanceToNextInstruction())
                .isEqualTo(78, Offset.offset(1.0));
    }

    @Test
    public void getRemainingDistanceToDestination_shouldBeFullDistance() throws Exception {
        Route myroute = getRoute("ace_hotel");
        ArrayList<Instruction> instructions = myroute.getRouteInstructions();
        myroute.snapToRoute(instructions.get(0).getLocation());
        assertThat((double) myroute.getRemainingDistanceToDestination())
                .isEqualTo((double) myroute.getTotalDistance(), Offset.offset(1.0));
    }

    @Test
    public void getRemainingDistanceToDestination_shouldBeZero() throws Exception {
        Route myroute = getRoute("ace_hotel");
        ArrayList<Instruction> instructions = myroute.getRouteInstructions();
        for (Instruction instruction : instructions) {
            myroute.snapToRoute(instruction.getLocation());
        }
        assertThat(myroute.getRemainingDistanceToDestination()).isEqualTo(0);
    }

    @Test
    public void getRemainingDistanceToDestination_shouldBeSyncUpWithTravelledDistance()
            throws Exception {
        Route myroute = getRoute("ace_hotel");
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
        Route myroute = getRoute("ace_hotel");
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
        Route myroute = getRoute("ace_hotel");
        ArrayList<Instruction> instructions = myroute.getRouteInstructions();
        instructions.get(0).setLiveDistanceToNext(4);
        ArrayList<Instruction> secondSetOfInstructions = myroute.getRouteInstructions();
        assertThat(secondSetOfInstructions.get(0).getLiveDistanceToNext()).isEqualTo(4);
    }

    @Test
    public void getRouteInstruction_shouldTallyUpDistances() throws Exception {
        Route myroute = getRoute("ace_hotel");
        ArrayList<Instruction> instructions = myroute.getRouteInstructions();
        int accumulatedDistance = 0;
        for (Instruction instruction : instructions) {
            accumulatedDistance += instruction.getDistance();
            assertThat(instruction.getLiveDistanceToNext()).isEqualTo(accumulatedDistance);
        }
    }

    @Test
    public void getCurrentInstruction_shouldReturnOneThatHasntyetbeencompleted() throws Exception {
        Route myroute = getRoute("ace_hotel");
        ArrayList<Instruction> instructions = myroute.getRouteInstructions();
        Location cornerOf26thAndBroadway = getLocation(40.743814, -73.989035);
        myroute.snapToRoute(cornerOf26thAndBroadway);
        assertThat(myroute.getCurrentInstruction()).isEqualTo(instructions.get(0));
        Location cornerOf26thAnd5thAve = getLocation(40.743434, -73.988123);
        myroute.snapToRoute(cornerOf26thAnd5thAve);
        assertThat(myroute.getCurrentInstruction()).isEqualTo(instructions.get(1));
        Location cornerOf26thAndMadison = getLocation(40.742790, -73.986547);
        myroute.snapToRoute(cornerOf26thAndMadison);
        assertThat(myroute.getCurrentInstruction()).isEqualTo(instructions.get(2));
    }

    @Test
    public void shouldCoverTotalDistance() throws Exception {
        Route myroute = getRoute("ace_hotel");
        ArrayList<Instruction> instructions = myroute.getRouteInstructions();
        for (Instruction instruction : instructions) {
            myroute.snapToRoute(instruction.getLocation());
        }
        assertThat(myroute.getTotalDistanceTravelled())
                .isEqualTo(myroute.getTotalDistance(), Offset.offset(1.0));
    }

    @Test
    public void shouldKnowDistanceTravelled() throws Exception {
        Route myroute = getRoute("ace_hotel");
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
        Route myroute = getRoute("ace_hotel");
        ArrayList<Instruction> instructions = myroute.getRouteInstructions();
        Location cornerOf26thAndBroadway = getLocation(40.743814, -73.989035);
        int[] before = new int[instructions.size()];
        for (Instruction instruction : instructions) {
            before[instructions.indexOf(instruction)] = instruction.getLiveDistanceToNext();
        }

        myroute.snapToRoute(cornerOf26thAndBroadway);
        for (Instruction instruction : instructions) {
            int expected = before[instructions.indexOf(instruction)] - (int) myroute.getTotalDistanceTravelled();
            assertThat(instruction.getLiveDistanceToNext()).isEqualTo(expected);
        }
    }

    private ArrayList<Location> getLocationsFromFile(String name) throws Exception {
        String fileName = getProperty("user.dir");
        File file = new File(fileName + "/src/test/fixtures/" + name + ".txt");
        ArrayList<Location> allLocations = new ArrayList<Location>();
        for(String locations: Files.readAllLines(file.toPath(), defaultCharset())) {
            String[] latLng = locations.split(",");
            Location location = new Location("test");
            location.setLatitude(Double.valueOf(latLng[0]));
            location.setLongitude(Double.valueOf(latLng[1]));
            allLocations.add(location);
        }
        return allLocations;
    }
}
