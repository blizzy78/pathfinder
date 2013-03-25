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

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;

import de.blizzy.pathfinder.Direction;

class Lane implements IDrawable {
	static final RGB COLOR = new RGB(60, 60, 60);

	private ColorRegistry colorRegistry;
	private Area area;

	Lane(World world, Point location, int length, Direction direction) {
		if (!world.contains(location)) {
			throw new IllegalArgumentException("world does not contain lane starting point"); //$NON-NLS-1$
		}

		Point endLocation;
		switch (direction) {
			case NORTH:
				endLocation = new Point(location.x, location.y - length + 1);
				break;
			case WEST:
				endLocation = new Point(location.x - length + 1, location.y);
				break;
			case EAST:
				endLocation = new Point(location.x + length - 1, location.y);
				break;
			case SOUTH:
				endLocation = new Point(location.x, location.y + length - 1);
				break;
			default:
				throw new IllegalArgumentException();
		}
		if (!world.contains(endLocation)) {
			throw new IllegalArgumentException("world does not contain lane ending point"); //$NON-NLS-1$
		}

		colorRegistry = world.getColorRegistry();
		int width = (direction == Direction.NORTH) || (direction == Direction.SOUTH) ? 1 : length;
		int height = (direction == Direction.WEST) || (direction == Direction.EAST) ? 1 : length;
		area = new Area(world, new Rectangle(Math.min(location.x, endLocation.x), Math.min(location.y, endLocation.y),
				width, height));

		world.add(this);
	}

	@Override
	public boolean mustRedraw() {
		return false;
	}

	@Override
	public boolean paint(GC gc, int pass) {
		gc.setBackground(colorRegistry.getColor(COLOR));
		gc.fillRectangle(area.getDrawArea());
		return false;
	}

	@Override
	public boolean contains(Point location) {
		return area.contains(location);
	}
}
