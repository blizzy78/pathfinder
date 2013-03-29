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

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import de.blizzy.pathfinder.PathFinderPlugin;

public class FollowVehicleOverlay implements IDrawable {
	private Vehicle vehicle;
	private Point lastLocation = new Point(-1, -1);
	private World world;
	private Image arrowNW;

	public FollowVehicleOverlay(World world) {
		this.world = world;

		world.add(this);
	}

	@Override
	public void dispose() {
		if (arrowNW != null) {
			arrowNW.dispose();
		}
	}

	@Override
	public boolean mustRedraw() {
		return (vehicle != null) ? vehicle.contains(lastLocation) : false;
	}

	@Override
	public boolean paint(GC gc, int pass) {
		initImages(gc);

		lastLocation = vehicle.getLocation();
		Area area = new Area(world, new Rectangle(lastLocation.x, lastLocation.y, 1, 1));
		Rectangle drawArea = area.getDrawArea();
		gc.drawImage(arrowNW, drawArea.x + World.CELL_PIXEL_SIZE - 2, drawArea.y + World.CELL_PIXEL_SIZE - 2);

		return false;
	}

	private void initImages(GC gc) {
		if (arrowNW == null) {
			Device device = gc.getDevice();
			arrowNW = PathFinderPlugin.getImageDescriptor("etc/icons/arrow_nw.png").createImage(device); //$NON-NLS-1$
		}
	}

	@Override
	public boolean contains(Point location) {
		return false;
	}

	public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}
}
