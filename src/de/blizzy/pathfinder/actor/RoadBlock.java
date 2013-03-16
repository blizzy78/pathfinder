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

public class RoadBlock implements IActor {
	private static final RGB COLOR = new RGB(255, 0, 0);

	private Point location;
	private ColorRegistry colorRegistry;

	public RoadBlock(World world, Point location) {
		if (!world.isRoadAt(location)) {
			throw new IllegalArgumentException("road block must be placed on road"); //$NON-NLS-1$
		}

		this.location = location;
		colorRegistry = world.getColorRegistry();

		world.add(this);
	}

	@Override
	public boolean mustRedraw() {
		// road blocks do never change
		return false;
	}

	@Override
	public boolean paint(GC gc, int pass) {
		gc.setBackground(colorRegistry.getColor(COLOR));
		gc.fillRectangle(location.x * World.CELL_PIXEL_SIZE + location.x * World.CELL_SPACING + 1,
				location.y * World.CELL_PIXEL_SIZE + location.y * World.CELL_SPACING + 1,
				World.CELL_PIXEL_SIZE - 2, World.CELL_PIXEL_SIZE - 2);
		return false;
	}

	@Override
	public void animate() {
		// road blocks do not move
	}

	@Override
	public boolean canBlockRoad() {
		return true;
	}

	@Override
	public boolean contains(Point location) {
		return location.equals(this.location);
	}
}
