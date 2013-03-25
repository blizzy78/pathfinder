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
	private int lanesPerSide;
	private Lane[] northLanes;
	private Lane[] westLanes;
	private Lane[] southLanes;
	private Lane[] eastLanes;

	public Road(World world, Point location, int lanes, int length, Direction direction) {
		if (lanes < 2) {
			throw new IllegalArgumentException("number of lanes must be >= 2"); //$NON-NLS-1$
		}
		lanesPerSide = lanes / 2;
		if ((lanesPerSide * 2) != lanes) {
			throw new IllegalArgumentException("number of lanes must be a factor of 2"); //$NON-NLS-1$
		}

		colorRegistry = world.getColorRegistry();
		northSouth = (direction == Direction.NORTH) || (direction == Direction.SOUTH);

		area = northSouth ?
				new Area(world, new Rectangle(location.x, location.y, lanes, length)) :
				new Area(world, new Rectangle(location.x, location.y, length, lanes));

		if (northSouth) {
			southLanes = new Lane[lanesPerSide];
			northLanes = new Lane[lanesPerSide];
		} else {
			westLanes = new Lane[lanesPerSide];
			eastLanes = new Lane[lanesPerSide];
		}

		//  x   x+1  x+2  x+3  x+4  x+5
		//  S   S    S    N    N    N

		for (int i = 0; i < lanesPerSide; i++) {
			if (northSouth) {
				northLanes[i] = new Lane(world, new Point(location.x + lanesPerSide + i, location.y + length - 1), length, Direction.NORTH);
				southLanes[i] = new Lane(world, new Point(location.x + lanesPerSide - i - 1, location.y), length, Direction.SOUTH);
			} else {
				westLanes[i] = new Lane(world, new Point(location.x + length - 1, location.y + lanesPerSide - i - 1), length, Direction.WEST);
				eastLanes[i] = new Lane(world, new Point(location.x, location.y + lanesPerSide + i), length, Direction.EAST);
			}
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
		if (pass == 0) {
			gc.setBackground(colorRegistry.getColor(Lane.COLOR));
			gc.fillRectangle(drawArea.x, drawArea.y, drawArea.width, drawArea.height);
			return true;
		} else {
			gc.setForeground(colorRegistry.getColor(LINE_COLOR));
			for (int i = 1; i < (lanesPerSide * 2); i++) {
				gc.setLineStyle((i == lanesPerSide) ? SWT.LINE_DASH : SWT.LINE_DOT);
				if (northSouth) {
					gc.drawLine(drawArea.x + World.CELL_PIXEL_SIZE * i + World.CELL_SPACING * (i - 1),
							drawArea.y + World.CELL_PIXEL_SIZE,
							drawArea.x + World.CELL_PIXEL_SIZE * i + World.CELL_SPACING * (i - 1),
							drawArea.y + drawArea.height - World.CELL_PIXEL_SIZE - 1);
				} else {
					gc.drawLine(drawArea.x + World.CELL_PIXEL_SIZE,
							drawArea.y + World.CELL_PIXEL_SIZE * i + World.CELL_SPACING * (i - 1),
							drawArea.x + drawArea.width - World.CELL_PIXEL_SIZE - 1,
							drawArea.y + World.CELL_PIXEL_SIZE * i + World.CELL_SPACING * (i - 1));
				}
			}
			return false;
		}
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
				if (northLanes != null) {
					return contains(northLanes, location);
				}
				break;
			case WEST:
				if (westLanes != null) {
					return contains(westLanes, location);
				}
				break;
			case SOUTH:
				if (southLanes != null) {
					return contains(southLanes, location);
				}
				break;
			case EAST:
				if (eastLanes != null) {
					return contains(eastLanes, location);
				}
				break;
		}
		return false;
	}

	boolean isRightMostSide(Point location, Direction headedTo) {
		if ((headedTo == null) || !contains(location)) {
			throw new IllegalArgumentException();
		}

		int outermostIdx = lanesPerSide - 1;
		switch (headedTo) {
			case NORTH:
				if ((northLanes != null) && northLanes[outermostIdx].contains(location)) {
					return true;
				}
				break;
			case WEST:
				if ((westLanes != null) && westLanes[outermostIdx].contains(location)) {
					return true;
				}
				break;
			case SOUTH:
				if ((southLanes != null) && southLanes[outermostIdx].contains(location)) {
					return true;
				}
				break;
			case EAST:
				if ((eastLanes != null) && eastLanes[outermostIdx].contains(location)) {
					return true;
				}
				break;
		}
		return false;
	}

	private boolean contains(Lane[] lanes, Point location) {
		if (lanes != null) {
			for (int i = 0; i < lanes.length; i++) {
				if (lanes[i].contains(location)) {
					return true;
				}
			}
		}
		return false;
	}

	Area getArea() {
		return area;
	}
}
