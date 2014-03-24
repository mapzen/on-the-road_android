package com.mapzen.osrm;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.ListIterator;

import static java.lang.System.*;
import static org.fest.assertions.api.Assertions.assertThat;

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
    public void hasCorrectTurnByTurnCordinatesInBrooklyn() throws Exception {
        ArrayList<double[]> points = new ArrayList<double[]>();
        points.add(new double[]{40.66071, -73.98933});
        points.add(new double[]{40.65982, -73.98784});
        points.add(new double[]{40.65925, -73.98843});
        points.add(new double[]{40.66325, -73.99504});
        points.add(new double[]{40.66732, -73.99117});
        points.add(new double[]{40.66631, -73.98909});
        Route brooklynRoute = getRoute("brooklyn");

        ListIterator<double[]> expectedPoints = points.listIterator();
        for(Instruction instruction: brooklynRoute.getRouteInstructions()) {
            double[] expectedPoint = expectedPoints.next();
            double[] instructionPoint = instruction.getPoint();

            // ceiling it as the percision of the double is not identical on the sixth digit
            assertThat(Math.ceil(instructionPoint[0])).isEqualTo(Math.ceil(expectedPoint[0]));
            assertThat(Math.ceil(instructionPoint[1])).isEqualTo(Math.ceil(expectedPoint[1]));
        }
    }

    @Test
    public void hasCorrectTurnByTurnHumanInstructionsInBrooklyn() throws Exception {
        ArrayList<String> points = new ArrayList<String>();
        points.add("Head on");
        points.add("Make a right on to");
        points.add("Make a right on to");
        points.add("Make a right on to");
        points.add("Make a right on to");
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
        double[] expected = {
            40.660708,
            -73.989332
        };
        assertThat(route.getStartCoordinates()).isEqualTo(expected);
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
        double[] stayOnRoute = {
                40.660250, -73.988105
        };
        double[] snapped = myroute.snapToRoute(stayOnRoute);
        assertThat(myroute.getCurrentLeg()).isEqualTo(0);
        assertThat(snapped).isNotNull();
        assertThat(snapped).isNotEqualTo(myroute.getStartCoordinates());
    }

    @Test
    public void snapToRoute_shouldSnapToBeginning() throws Exception {
        Route myroute = getRoute("greenpoint_around_the_block");
        double[] snapToBeginning = {40.661060, -73.990004};
        assertThat(myroute.snapToRoute(snapToBeginning)).isEqualTo(myroute.getStartCoordinates());
    }

    @Test
    public void snapToRoute_shouldSnapToNextLeg() throws Exception {
        // these points are behind the new line
        Route myroute = getRoute("greenpoint_around_the_block");
        double[] expected = {myroute.getGeometry().get(1)[0], myroute.getGeometry().get(1)[1]};
        double[] snapToNextLeg1 = {40.659740, -73.987802};
        assertThat(myroute.snapToRoute(snapToNextLeg1)).isEqualTo(expected);
        myroute.rewind();
        double[] snapToNextLeg2 = {40.659762, -73.987821};
        assertThat(myroute.snapToRoute(snapToNextLeg2)).isEqualTo(expected);
        myroute.rewind();
        double[] snapToNextLeg3 = {40.659781, -73.987890};
        assertThat(myroute.snapToRoute(snapToNextLeg3)).isEqualTo(expected);
    }

    @Test
    public void snapToRoute_shouldAdvanceToNextLegButNotSnapToThatBeginning() throws Exception {
        Route myroute = getRoute("greenpoint_around_the_block");
        double[] justAroundTheCorner1 = {40.659826, -73.987838};
        double[] snappedTo1 = myroute.snapToRoute(justAroundTheCorner1);
        out.println("snapped: " + snappedTo1[0] + ", " + snappedTo1[1]);
        assertThat(snappedTo1).isNotEqualTo(new double[] {route.getGeometry().get(1)[0], route.getGeometry().get(1)[1]});
        assertThat(myroute.getCurrentLeg()).isEqualTo(1);
        myroute.rewind();
        double[] justAroundTheCorner2 = {40.659847, -73.987835};
        double[] snappedTo2 = myroute.snapToRoute(justAroundTheCorner2);
        assertThat(snappedTo2).isNotEqualTo(new double[] {myroute.getGeometry().get(1)[0], myroute.getGeometry().get(1)[1]});
        assertThat(myroute.getCurrentLeg()).isEqualTo(1);
    }

    @Test
    public void snapToRoute_shouldFindFutureLegs() throws Exception {
        Route myroute = getRoute("greenpoint_around_the_block");
        double[] point = {40.660785, -73.987878};
        double[] snapped = myroute.snapToRoute(point);
        out.println("snapped: " + snapped[0] + ", " + snapped[1]);
        assertThat(snapped).isNotNull();
        assertThat(myroute.getCurrentLeg()).isEqualTo(4);
    }

    @Test
    public void snapToRoute_shouldRealizeItsLost() throws Exception {
        double[] lost;
        Route myroute = getRoute("greenpoint_around_the_block");
        lost = new double[] {40.662046, -73.987089};
        assertThat(myroute.snapToRoute(lost)).isNull();
        myroute.rewind();
        lost = new double[] {40.658749, -73.986102};
        //assertThat(myroute.snapToRoute(lost)).isNull();
    }

    @Test
    public void snapToRoute_shouldBeFinalDestination() throws Exception {
        Route myroute = getRoute("greenpoint_around_the_block");
        double[] foundIt = {
                40.661434, -73.989030
        };
        double[] snapped = myroute.snapToRoute(foundIt);
        ArrayList<double[]> geometry = myroute.getGeometry();
        double[] expected = {
                geometry.get(geometry.size()-1)[0],
                geometry.get(geometry.size()-1)[1]
        };
        assertThat(snapped).isEqualTo(expected);
    }

    @Test
    public void snapToRoute_shouldHandleSharpTurn() throws Exception {
        Route myroute = getRoute("sharp_turn");
        double[] aroundSharpTurn = {
                40.687052, -73.976300
        };
        double[] snapped = myroute.snapToRoute(aroundSharpTurn);
        // TODO ... handle this case
        //assertThat(myroute.getCurrentLeg()).isEqualTo(2);
    }
}
