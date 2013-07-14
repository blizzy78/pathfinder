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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;

import de.blizzy.pathfinder.Direction;

public class Vehicle implements IActor {
	private static enum Mode {
		DRIVE, PARK;
	}

	private static final RGB COLOR = new RGB(255, 255, 0);
	private static final RGB PARK_COLOR = new RGB(255, 150, 0);
	private static final double CHANGE_DIRECTION_CHANCE = 0.05d;
	private static final double PARK_CHANCE = 0.0005d;
	private static final long PARK_DURATION = 8 * 1000;

	private World world;
	private Point location;
	private ColorRegistry colorRegistry;
	private Direction headedTo = Direction.NORTH;
	private Mode mode = Mode.DRIVE;
	private int frames;
	private AtomicBoolean mustRedraw = new AtomicBoolean(true);
	private long parkedTime;
	private int framesPerStep = (int) (Math.random() * 4) + 2;

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
	public void dispose() {
	}

	@Override
	public boolean mustRedraw() {
		return mustRedraw.get();
	}

	@Override
	public boolean paint(GC gc, int pass) {
		Point location;
		Mode mode;
		synchronized (this) {
			location = this.location;
			mode = this.mode;
		}
		gc.setBackground(colorRegistry.getColor(mode == Mode.PARK ? PARK_COLOR : COLOR));
		gc.fillRectangle(location.x * World.CELL_PIXEL_SIZE + location.x * World.CELL_SPACING + 1,
				location.y * World.CELL_PIXEL_SIZE + location.y * World.CELL_SPACING + 1,
				World.CELL_PIXEL_SIZE - 2, World.CELL_PIXEL_SIZE - 2);
		mustRedraw.set(false);
		return false;
	}

	@Override
	public void animate() {
		if (++frames >= framesPerStep) {
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
			boolean rightMostSide = true;
			Set<Road> roads = world.getRoadsAt(location);
			for (Road road : roads) {
				if (!road.isOutermostSide(location, headedTo)) {
					rightMostSide = false;
					break;
				}
			}
			if (rightMostSide) {
				mode = Mode.PARK;
				parkedTime = System.currentTimeMillis();
				mustRedraw.set(true);
			}
			return;
		}

		Map<Direction, Point> blockedDirections = TrafficUtil.getPossibleDirections(location, headedTo, true, true, world);
		boolean couldGoInSameDirection = blockedDirections.containsKey(headedTo);
		Map<Direction, Point> possibleDirections = TrafficUtil.getPossibleDirections(location, headedTo, true, false, world);
		boolean canGoInSameDirection = possibleDirections.containsKey(headedTo);
		boolean changeDirection = Math.random() < CHANGE_DIRECTION_CHANCE;
		Point newLocation = null;

		// try to drive around a parking vehicle
		if (couldGoInSameDirection && !canGoInSameDirection && !changeDirection &&
			world.isParkingVehicleAt(blockedDirections.get(headedTo))) {

			Direction comingFrom;
			switch (headedTo) {
				case NORTH:
					newLocation = new Point(location.x - 1, location.y);
					comingFrom = Direction.EAST;
					break;
				case WEST:
					newLocation = new Point(location.x, location.y + 1);
					comingFrom = Direction.NORTH;
					break;
				case SOUTH:
					newLocation = new Point(location.x + 1, location.y);
					comingFrom = Direction.WEST;
					break;
				case EAST:
					newLocation = new Point(location.x, location.y - 1);
					comingFrom = Direction.SOUTH;
					break;
				default:
					throw new IllegalStateException();
			}
			if (!TrafficUtil.isRoad(newLocation, world) ||
				!TrafficUtil.isRightSideOfRoad(newLocation, headedTo, world) ||
				world.isRoadBlockedAt(newLocation, comingFrom, location)) {

				newLocation = null;
			}
		}

		// change direction if necessary or per random chance
		if ((newLocation == null) && !possibleDirections.isEmpty() &&
			(!canGoInSameDirection || changeDirection)) {

			List<Direction> directions = new ArrayList<>(possibleDirections.keySet());
			if (changeDirection && canGoInSameDirection && (directions.size() > 1)) {
				directions.remove(headedTo);
			}
			Collections.shuffle(directions);
			headedTo = directions.get(0);
			newLocation = possibleDirections.get(headedTo);
		}

		// default behavior: move into desired direction
		if (newLocation == null) {
			newLocation = possibleDirections.get(headedTo);
		}

		if (newLocation != null) {
			location = newLocation;
			mustRedraw.set(true);
		}
	}

	private void park() {
		if ((System.currentTimeMillis() - parkedTime) >= PARK_DURATION) {
			mode = Mode.DRIVE;
			mustRedraw.set(true);
		}
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

	public boolean isParking() {
		return mode == Mode.PARK;
	}
}
