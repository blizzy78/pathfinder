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

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;

public class RouteOverlay implements IDrawable {
	private static final RGB COLOR = new RGB(255, 0, 0);

	private World world;
	private ColorRegistry colorRegistry;
	private Collection<Point> route;
	private AtomicBoolean mustRedraw = new AtomicBoolean(true);

	public RouteOverlay(World world) {
		this.world = world;
		colorRegistry = world.getColorRegistry();

		world.add(this);
	}

	@Override
	public void dispose() {
		// nothing to do
	}

	@Override
	public boolean mustRedraw() {
		return mustRedraw.get();
	}

	@Override
	public boolean paint(GC gc, int pass) {
		if (route != null) {
			Color color = colorRegistry.getColor(COLOR);
			gc.setBackground(color);
			for (Point location : route) {
				Area area = new Area(world, new Rectangle(location.x, location.y, 1, 1));
				Rectangle drawArea = area.getDrawArea();
				gc.fillRectangle(drawArea);
			}
			mustRedraw.set(false);
		}
		return false;
	}

	@Override
	public boolean contains(Point location) {
		return false;
	}

	public void setRoute(Collection<Point> route) {
		this.route = route;
		mustRedraw.set(true);
	}
}
