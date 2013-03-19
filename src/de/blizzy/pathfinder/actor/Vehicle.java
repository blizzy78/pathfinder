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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;

public class Vehicle implements IActor {
	private static enum Mode {
		DRIVE, PARK;
	}

	private static final RGB COLOR = new RGB(255, 255, 0);
	private static final RGB PARK_COLOR = new RGB(255, 150, 0);
	private static final int FRAMES_PER_STEP = 1;
	private static final double CHANGE_DIRECTION_CHANCE = 0.05d;
	private static final double PARK_CHANCE = 0.0005d;
	private static final long PARK_DURATION = 5 * 1000;

	private World world;
	private Point location;
	private ColorRegistry colorRegistry;
	private Direction headedTo = Direction.NORTH;
	private Mode mode = Mode.DRIVE;
	private int frames;
	private boolean mustRedraw = true;
	private long parkedTime;

	public Vehicle(World world, Point location) {
		if (!world.isRoadAt(location)) {
			throw new IllegalArgumentException("vehicle must be placed on road"); //$NON-NLS-1$
		}

		this.world = world;
		this.location = location;
		colorRegistry = world.getColorRegistry();

		world.add(this);
	}

	@Override
	public boolean mustRedraw() {
		return mustRedraw;
	}

	@Override
	public boolean paint(GC gc, int pass) {
		Point location;
		synchronized (this) {
			location = this.location;
		}
		gc.setBackground(colorRegistry.getColor(mode == Mode.PARK ? PARK_COLOR : COLOR));
		gc.fillRectangle(location.x * World.CELL_PIXEL_SIZE + location.x * World.CELL_SPACING + 1,
				location.y * World.CELL_PIXEL_SIZE + location.y * World.CELL_SPACING + 1,
				World.CELL_PIXEL_SIZE - 2, World.CELL_PIXEL_SIZE - 2);
		mustRedraw = false;
		return false;
	}

	@Override
	public void animate() {
		if (frames++ >= FRAMES_PER_STEP) {
			synchronized (this) {
				switch (mode) {
					case DRIVE:
						drive();
						break;
					case PARK:
						park();
						break;
				}
			}
			frames = 0;
		}
	}

	private void drive() {
		if (Math.random() < PARK_CHANCE) {
			mode = Mode.PARK;
			parkedTime = System.currentTimeMillis();
			mustRedraw = true;
			return;
		}

		Map<Direction, Point> possibleDirections = getPossibleDirections();
		if (!possibleDirections.isEmpty()) {
			boolean canGoInSameDirection = possibleDirections.containsKey(headedTo);
			boolean changeDirection = Math.random() < CHANGE_DIRECTION_CHANCE;
			if (!canGoInSameDirection || changeDirection) {
				List<Direction> directions = new ArrayList<>(possibleDirections.keySet());
				if (changeDirection && canGoInSameDirection && (directions.size() > 1)) {
					directions.remove(headedTo);
				}
				Collections.shuffle(directions);
				headedTo = directions.get(0);
			}

			Point newLocation = possibleDirections.get(headedTo);
			location = newLocation;
			mustRedraw = true;
		}
	}

	private void park() {
		if ((System.currentTimeMillis() - parkedTime) >= PARK_DURATION) {
			mode = Mode.DRIVE;
			mustRedraw = true;
		}
	}

	private Map<Direction, Point> getPossibleDirections() {
		Map<Direction, Point> directions = new HashMap<>(4);
		Point newLocation;
		if (headedTo != Direction.SOUTH) {
			newLocation = new Point(location.x, location.y - 1);
			if (isRoad(newLocation) &&
				isRightSideOfRoad(newLocation, Direction.NORTH) &&
				!world.isRoadBlockedAt(newLocation, Direction.SOUTH, location)) {

				directions.put(Direction.NORTH, newLocation);
			}
		}
		if (headedTo != Direction.EAST) {
			newLocation = new Point(location.x - 1, location.y);
			if (isRoad(newLocation) &&
				isRightSideOfRoad(newLocation, Direction.WEST) &&
				!world.isRoadBlockedAt(newLocation, Direction.EAST, location)) {

				directions.put(Direction.WEST, newLocation);
			}
		}
		if (headedTo != Direction.NORTH) {
			newLocation = new Point(location.x, location.y + 1);
			if (isRoad(newLocation) &&
				isRightSideOfRoad(newLocation, Direction.SOUTH) &&
				!world.isRoadBlockedAt(newLocation, Direction.NORTH, location)) {

				directions.put(Direction.SOUTH, newLocation);
			}
		}
		if (headedTo != Direction.WEST) {
			newLocation = new Point(location.x + 1, location.y);
			if (isRoad(newLocation) &&
				isRightSideOfRoad(newLocation, Direction.EAST) &&
				!world.isRoadBlockedAt(newLocation, Direction.WEST, location)) {

				directions.put(Direction.EAST, newLocation);
			}
		}
		return directions;
	}

	private boolean isRoad(Point location) {
		return world.contains(location) && world.isRoadAt(location);
	}

	private boolean isRightSideOfRoad(Point location, Direction headedTo) {
		if (!isRoad(location)) {
			return false;
		}
		for (Road road : world.getRoadsAt(location)) {
			if (road.isRightSide(location, headedTo)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean canBlockRoad() {
		return true;
	}

	@Override
	public boolean contains(Point location) {
		return location.equals(this.location);
	}

	Point getLocation() {
		return location;
	}
}
