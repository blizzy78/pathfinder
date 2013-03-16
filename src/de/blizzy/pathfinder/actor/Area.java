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

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class Area {
	private Rectangle area;
	private Point bottomRight;
	private Rectangle drawArea;

	public Area(World world, Rectangle area) {
		if (!world.contains(new Point(area.x, area.y)) || !world.contains(new Point(area.x + area.width - 1, area.y + area.height - 1))) {
			throw new IllegalArgumentException("world does not contain one of area corners"); //$NON-NLS-1$
		}
		if (area.width < 1) {
			throw new IllegalArgumentException("width must be >= 1"); //$NON-NLS-1$
		}
		if (area.height < 1) {
			throw new IllegalArgumentException("height must be >= 1"); //$NON-NLS-1$
		}

		this.area = area;
		bottomRight = new Point(area.x + area.width - 1, area.y + area.height - 1);
		drawArea = new Rectangle(area.x * World.CELL_PIXEL_SIZE + area.x * World.CELL_SPACING,
				area.y * World.CELL_PIXEL_SIZE + area.y * World.CELL_SPACING,
				area.width * World.CELL_PIXEL_SIZE + area.width * World.CELL_SPACING - 1,
				area.height * World.CELL_PIXEL_SIZE + area.height * World.CELL_SPACING - 1);
	}

	Rectangle getArea() {
		return area;
	}

	Rectangle getDrawArea() {
		return drawArea;
	}

	boolean contains(Point location) {
		return (location.x >= area.x) && (location.y >= area.y) &&
				(location.x <= bottomRight.x) && (location.y <= bottomRight.y);
	}
}
