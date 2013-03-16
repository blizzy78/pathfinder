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
import org.eclipse.swt.graphics.Rectangle;

class DoubleBuffer {
	private Image buffer;
	private GC gc;
	private boolean dirty;

	DoubleBuffer(Device device, int width, int height) {
		buffer = new Image(device, width, height);
		gc = new GC(buffer);
	}

	GC getGC() {
		return gc;
	}

	void dispose() {
		gc.dispose();
		buffer.dispose();
	}

	void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	boolean isDirty() {
		return dirty;
	}

	void paint(GC gc, Rectangle damagedRegion) {
		gc.drawImage(buffer, damagedRegion.x, damagedRegion.y, damagedRegion.width, damagedRegion.height,
				damagedRegion.x, damagedRegion.y, damagedRegion.width, damagedRegion.height);
	}
}
