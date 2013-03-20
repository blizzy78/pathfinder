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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class ActorLayerComparator implements Comparator<IActor> {
	static final ActorLayerComparator INSTANCE = new ActorLayerComparator();

	private static final List<Class<? extends IActor>> classes = new ArrayList<>();

	static {
		classes.add(World.class);
		classes.add(Road.class);
		classes.add(Building.class);
		classes.add(RoadBlock.class);
		classes.add(Vehicle.class);
		classes.add(TrafficLight.class);
		classes.add(TrafficDensityOverlay.class);
	}

	private ActorLayerComparator() {}

	@Override
	public int compare(IActor a1, IActor a2) {
		int idx1 = classes.indexOf(a1.getClass());
		int idx2 = classes.indexOf(a2.getClass());
		return Integer.valueOf(idx1).compareTo(idx2);
	}
}
