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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;

import de.blizzy.pathfinder.Direction;

public class Road implements IDrawable {
	private static final RGB LINE_COLOR = new RGB(150, 150, 150);

	private ColorRegistry colorRegistry;
	private boolean northSouth;
	private Area area;
	private Lane northLane;
	private Lane westLane;
	private Lane southLane;
	private Lane eastLane;

	public Road(World world, Point location, int length, Direction direction) {
		colorRegistry = world.getColorRegistry();
		northSouth = (direction == Direction.NORTH) || (direction == Direction.SOUTH);

		area = northSouth ?
				new Area(world, new Rectangle(location.x, location.y, 2, length)) :
				new Area(world, new Rectangle(location.x, location.y, length, 2));

		if (northSouth) {
			southLane = new Lane(world, new Point(location.x, location.y), length, Direction.SOUTH);
			northLane = new Lane(world, new Point(location.x + 1, location.y + length - 1), length, Direction.NORTH);
		} else {
			westLane = new Lane(world, new Point(location.x + length - 1, location.y), length, Direction.WEST);
			eastLane = new Lane(world, new Point(location.x, location.y + 1), length, Direction.EAST);
		}

		world.add(this);
	}

	@Override
	public boolean mustRedraw() {
		// roads no never change
		return false;
	}

	@Override
	public boolean paint(GC gc, int pass) {
		Rectangle drawArea = area.getDrawArea();
		gc.setBackground(colorRegistry.getColor(Lane.COLOR));
		gc.fillRectangle(drawArea.x + World.CELL_PIXEL_SIZE, drawArea.y + World.CELL_PIXEL_SIZE,
				northSouth ? 1 : drawArea.width - World.CELL_PIXEL_SIZE, !northSouth ? 1 : drawArea.height - World.CELL_PIXEL_SIZE);
		gc.setForeground(colorRegistry.getColor(LINE_COLOR));
		gc.setLineStyle(SWT.LINE_DASH);
		gc.drawLine(drawArea.x + World.CELL_PIXEL_SIZE, drawArea.y + World.CELL_PIXEL_SIZE,
				!northSouth ? drawArea.x + drawArea.width - World.CELL_PIXEL_SIZE - 1 : drawArea.x + World.CELL_PIXEL_SIZE,
				northSouth ? drawArea.y + drawArea.height - World.CELL_PIXEL_SIZE - 1 : drawArea.y + World.CELL_PIXEL_SIZE);
		return false;
	}

	@Override
	public boolean contains(Point location) {
		return area.contains(location);
	}

	boolean isRightSide(Point location, Direction headedTo) {
		if ((headedTo == null) || !contains(location)) {
			throw new IllegalArgumentException();
		}

		switch (headedTo) {
			case NORTH:
				if (northLane != null) {
					return northLane.contains(location);
				}
				break;
			case WEST:
				if (westLane != null) {
					return westLane.contains(location);
				}
				break;
			case SOUTH:
				if (southLane != null) {
					return southLane.contains(location);
				}
				break;
			case EAST:
				if (eastLane != null) {
					return eastLane.contains(location);
				}
				break;
		}
		return false;
	}

	Area getArea() {
		return area;
	}
}
