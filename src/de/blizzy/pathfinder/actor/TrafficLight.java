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

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;

import de.blizzy.pathfinder.Direction;

public class TrafficLight implements IActor {
	private static final RGB RED_COLOR = new RGB(255, 0, 0);
	private static final RGB GREEN_COLOR = new RGB(0, 255, 0);

	private Point location;
	private ColorRegistry colorRegistry;
	private Area area;
	private AtomicBoolean northSouthAllowed = new AtomicBoolean(Math.random() < 0.5d);
	private int secondsBetweenSwitches = (int) (Math.random() * 5d) + 5;
	private int framesBetweenSwitches = secondsBetweenSwitches * World.ANIMATION_FRAMES_PER_SECOND;
	private int frames;
	private AtomicBoolean mustRedraw = new AtomicBoolean(true);

	public TrafficLight(World world, Point location) {
		if (!world.isRoadAt(location) || !world.isRoadAt(new Point(location.x + 1, location.y + 1))) {
			throw new IllegalArgumentException("traffic light must be placed on road"); //$NON-NLS-1$
		}

		this.location = location;
		colorRegistry = world.getColorRegistry();
		area = new Area(world, new Rectangle(location.x, location.y, 2, 2));

		world.add(this);
	}

	@Override
	public boolean mustRedraw() {
		return mustRedraw.get();
	}

	@Override
	public boolean paint(GC gc, int pass) {
		boolean northSouthAllowed = this.northSouthAllowed.get();
		gc.setBackground(colorRegistry.getColor(northSouthAllowed ? GREEN_COLOR : RED_COLOR));
		gc.fillRectangle(location.x * World.CELL_PIXEL_SIZE + location.x * World.CELL_SPACING,
				location.y * World.CELL_PIXEL_SIZE + location.y * World.CELL_SPACING,
				2, 2);
		gc.fillRectangle((location.x + 2) * World.CELL_PIXEL_SIZE + location.x * World.CELL_SPACING - 1,
				(location.y + 2) * World.CELL_PIXEL_SIZE + location.y * World.CELL_SPACING - 1,
				2, 2);
		gc.setBackground(colorRegistry.getColor(!northSouthAllowed ? GREEN_COLOR : RED_COLOR));
		gc.fillRectangle((location.x + 2) * World.CELL_PIXEL_SIZE + location.x * World.CELL_SPACING - 1,
				location.y * World.CELL_PIXEL_SIZE + location.y * World.CELL_SPACING,
				2, 2);
		gc.fillRectangle(location.x * World.CELL_PIXEL_SIZE + location.x * World.CELL_SPACING,
				(location.y + 2) * World.CELL_PIXEL_SIZE + location.y * World.CELL_SPACING - 1,
				2, 2);
		mustRedraw.set(false);
		return false;
	}

	@Override
	public void animate() {
		if (frames++ >= framesBetweenSwitches) {
			northSouthAllowed.set(!northSouthAllowed.get());
			frames = 0;
			mustRedraw.set(true);
		}
	}

	@Override
	public boolean canBlockRoad() {
		return false;
	}

	@Override
	public boolean contains(Point location) {
		return area.contains(location);
	}

	boolean isAllowed(Direction comingFrom) {
		boolean northSouthAllowed = this.northSouthAllowed.get();
		return (northSouthAllowed && ((comingFrom == Direction.NORTH) || (comingFrom == Direction.SOUTH))) ||
				(!northSouthAllowed && ((comingFrom == Direction.WEST) || (comingFrom == Direction.EAST)));
	}
}
