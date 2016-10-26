# On The Road

Android client for the Valhalla routing service plus mobile nav tools

[![CircleCI](https://circleci.com/gh/mapzen/on-the-road_android.svg?style=svg&circle-token=654423209f8f63b35432f450450069ce44bb5729)](https://circleci.com/gh/mapzen/on-the-road_android)

This library has two main responsibilities, it acts as an Android client for [Mapzen Turn-by-Turn][2] (powered by [Valhalla][5]) and it handles client-side location correction, specifically snapping the client’s location along road.

We accept feature requests as suggestions or patches.

## Usage

#### The Router

To construct a Route object from two or more locations you obtain a Router object and specify your options.

```java
new ValhallaRouter()
	.setDriving() // Driving is default (setBiking, setWalking also available)
	.setLocation(new double[]{lat, lng}) // required
	.setLocation(new double[]{lat, lng}) // required
	.setZoomLevel(17) // defaults to 17
	.setCallback(new Callback() {
		@Override
		public void success(Route route) {
			// do stuff
		}

		@Override
		public void failure(int statusCode) {
			// do stuff
		}
	}).fetch(); // this executes the http call in a thread and calls methods on the callback
```

#### Route

When Route is found between your two locations your Callback::success method will be called with the Route instance as argument.
From the Route instance you can get attributes such as time and distance. Route also provides the route geometry and instructions.

```java
int totalDistance = route.getTotalDistance(); // see DistanceFormatter for options
int totalTime = route.getTotalTime();
List<Instructions> instructions = route.getRouteInstructions();
List<Node> nodes = route.getGeometry();
```

#### Instruction

Route has a Instruction collection which represents an actionable point along the route this will have
coordinates of the action that needs taken plus some other useful properties.

```java
// quick sample of available methods
Instruction instruction = route.getRouteInstructions().get(0);
instruction.getTurnInstruction();
instruction.getHumanTurnInstruction();
instruction.getDistance();
instruction.getDirection();
instruction.getBearing();

// or get a formatted instruction
instruction.getFullInstruction();
```

The best way to learn the api while documentation is scarce is to look at the tests. We aim to keep thorough coverage for
increased stability and self documentation.

#### Client side magic

Device GPS accuracy is almost never perfect, especially in cities. Being off by several feet becomes a very apparent problem in routing. Your user could be driving their car in a straight line down the street but your app could be displaying them 20 feet off the road. To combat this we mathematically manipulate the
location we receive from the location service and snap it to the road. You can use our project [Lost][3] if you would like an open source alternative to the `FusedLocationProvider`.

The best way to describe the problem is by looking at two illustrations:

This is what happens before correction, you'll have a road (- and \)  and your location the (x);

```
x    x
  x
             x
---------------- x
   x            \
        x     x  \
                  \    x
                   \  x
```

When you are showing a location on a map and you have high-lighted a route but the location consistently
fails to persist on the route it might be confusing for the user. Ideally the user will see the location
represented along the route. To accomplish this we fix the location by snapping it on the road with corrected
location (o).
```

x    x
  x
             x
o-oo-o--o-o--o-- x
   x            o
        x     x  o
                  o    x
                   o  x
```

This is done by finding the intersection of two beams, one the beginning of the route leg and the other the location with +90 or -90
bearing compared to the bearing of the route leg. The math formulas we use were translated from [Chris Veness scripts][4]
his examples are written in javascript and we translated the formulas we needed to java.

```java

// if this returns null it means we are probably
// off course and it's time to obtain new route from current position
double[] onTheRoad = route.snapToRoute(new double[] { lat, lng });

```

#### To test this and reply route scenarios we have built a simple web app that communicates with the library

see https://github.com/mapzen/on-the-road_web-tester


## Install

#### Download AAR

Download the [latest AAR][1].

#### Maven

Include dependency using Maven.

```xml
<dependency>
  <groupId>com.mapzen</groupId>
  <artifactId>on-the-road</artifactId>
  <version>1.1.1</version>
  <type>aar</type>
</dependency>
```

#### Gradle

Include dependency using Gradle.

```groovy
compile 'com.mapzen:on-the-road:1.1.1'
```

[1]: http://search.maven.org/remotecontent?filepath=com/mapzen/on-the-road/1.1.1/on-the-road-1.1.1.aar
[2]: https://mapzen.com/projects/valhalla/
[3]: https://github.com/mapzen/lost
[4]: http://www.movable-type.co.uk/scripts/latlong.html
[5]: https://github.com/valhalla
