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
package de.blizzy.pathfinder;

import org.eclipse.swt.graphics.Point;

public enum Direction {
	NORTH, WEST, SOUTH, EAST;

	public static Direction getHeadedTo(Point origin, Point target) {
		int originX = origin.x;
		int originY = origin.y;
		int targetX = target.x;
		int targetY = target.y;
		if (originX == targetX) {
			if (originY > targetY) {
				return NORTH;
			} else if (originY < targetY) {
				return SOUTH;
			} else {
				throw new IllegalArgumentException("locations must be adjacent to one another"); //$NON-NLS-1$
			}
		} else if (originY == targetY) {
			if (originX > targetX) {
				return WEST;
			} else if (originX < targetX) {
				return EAST;
			} else {
				throw new IllegalArgumentException("locations must be adjacent to one another"); //$NON-NLS-1$
			}
		} else {
			throw new IllegalArgumentException("locations must be adjacent to one another"); //$NON-NLS-1$
		}
	}
}
