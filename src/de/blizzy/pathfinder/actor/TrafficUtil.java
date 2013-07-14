/*
Copyright (C) 2013 Maik Schreiber

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to
deal in the Software without restriction, including without limitation the
rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
sell copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package de.blizzy.pathfinder.actor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Point;

import de.blizzy.pathfinder.Direction;

final class TrafficUtil {
	private TrafficUtil() {
		// nothing to do
	}

	static Map<Direction, Point> getPossibleDirections(Point location, Direction headedTo,
			boolean checkTrafficLights, boolean checkRoadBlocks, boolean checkVehicles, boolean blocked, World world) {

		Map<Direction, Point> directions = new HashMap<>(4);
		Point newLocation;
		if (headedTo != Direction.SOUTH) {
			newLocation = new Point(location.x, location.y - 1);
			if (isRoad(newLocation, world) &&
				isRightSideOfRoad(newLocation, Direction.NORTH, world) &&
				(world.isRoadBlockedAt(newLocation, Direction.SOUTH, location,
						checkTrafficLights, checkRoadBlocks, checkVehicles) == blocked)) {

				directions.put(Direction.NORTH, newLocation);
			}
		}
		if (headedTo != Direction.EAST) {
			newLocation = new Point(location.x - 1, location.y);
			if (isRoad(newLocation, world) &&
				isRightSideOfRoad(newLocation, Direction.WEST, world) &&
				(world.isRoadBlockedAt(newLocation, Direction.EAST, location,
						checkTrafficLights, checkRoadBlocks, checkVehicles) == blocked)) {

				directions.put(Direction.WEST, newLocation);
			}
		}
		if (headedTo != Direction.NORTH) {
			newLocation = new Point(location.x, location.y + 1);
			if (isRoad(newLocation, world) &&
				isRightSideOfRoad(newLocation, Direction.SOUTH, world) &&
				(world.isRoadBlockedAt(newLocation, Direction.NORTH, location,
						checkTrafficLights, checkRoadBlocks, checkVehicles) == blocked)) {

				directions.put(Direction.SOUTH, newLocation);
			}
		}
		if (headedTo != Direction.WEST) {
			newLocation = new Point(location.x + 1, location.y);
			if (isRoad(newLocation, world) &&
				isRightSideOfRoad(newLocation, Direction.EAST, world) &&
				(world.isRoadBlockedAt(newLocation, Direction.WEST, location,
						checkTrafficLights, checkRoadBlocks, checkVehicles) == blocked)) {

				directions.put(Direction.EAST, newLocation);
			}
		}
		return directions;
	}

	static boolean isRoad(Point location, World world) {
		return world.contains(location) && world.isRoadAt(location);
	}

	static boolean isRightSideOfRoad(Point location, Direction headedTo, World world) {
		for (Road road : world.getRoadsAt(location)) {
			if (road.isRightSide(location, headedTo)) {
				return true;
			}
		}
		return false;
	}
}
