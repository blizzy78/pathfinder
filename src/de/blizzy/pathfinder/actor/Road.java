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

public class Road implements IActor {
	private static final int WIDTH = 2;
	private static final RGB COLOR = new RGB(60, 60, 60);
	private static final RGB LINE_COLOR = new RGB(150, 150, 150);

	private Area area;
	private ColorRegistry colorRegistry;

	public Road(World world, Area area) {
		if ((area.getArea().width != WIDTH) && (area.getArea().height != WIDTH)) {
			throw new IllegalArgumentException("road width must be exactly " + WIDTH); //$NON-NLS-1$
		}

		this.area = area;
		colorRegistry = world.getColorRegistry();

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
		boolean horizontal = area.getArea().height == 2;

		if (pass == 0) {
			gc.setBackground(colorRegistry.getColor(COLOR));
			gc.fillRectangle(drawArea);
			return true;
		} else {
			gc.setForeground(colorRegistry.getColor(LINE_COLOR));
			gc.setLineStyle(SWT.LINE_DASH);
			gc.drawLine(drawArea.x + World.CELL_PIXEL_SIZE, drawArea.y + World.CELL_PIXEL_SIZE,
					horizontal ? drawArea.x + drawArea.width - World.CELL_PIXEL_SIZE - 1 : drawArea.x + World.CELL_PIXEL_SIZE,
					!horizontal ? drawArea.y + drawArea.height - World.CELL_PIXEL_SIZE - 1 : drawArea.y + World.CELL_PIXEL_SIZE);
			return false;
		}
	}

	@Override
	public boolean contains(Point location) {
		return area.contains(location);
	}

	@Override
	public boolean canBlockRoad() {
		return false;
	}

	@Override
	public void animate() {
		// roads do not move
	}

	boolean isRightSide(Point location, Direction headedTo) {
		if (!contains(location)) {
			throw new IllegalArgumentException();
		}

		Rectangle area = this.area.getArea();
		switch (headedTo) {
			case NORTH:
				return location.x == area.x + 1;
			case WEST:
				return location.y == area.y;
			case SOUTH:
				return location.x == area.x;
			case EAST:
				return location.y == area.y + 1;
		}
		throw new IllegalArgumentException();
	}

	Area getArea() {
		return area;
	}
}
