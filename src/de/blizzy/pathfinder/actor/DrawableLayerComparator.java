package de.blizzy.pathfinder.actor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class DrawableLayerComparator implements Comparator<IDrawable> {
	static final DrawableLayerComparator INSTANCE = new DrawableLayerComparator();

	private static final List<Class<? extends IDrawable>> classes = new ArrayList<>();

	static {
		classes.add(World.class);
		classes.add(Lane.class);
		classes.add(Road.class);
		classes.add(Building.class);
		classes.add(RoadBlock.class);
		classes.add(Vehicle.class);
		classes.add(TrafficLight.class);
		classes.add(TrafficDensityOverlay.class);
	}

	private DrawableLayerComparator() {}

	@Override
	public int compare(IDrawable d1, IDrawable d2) {
		int idx1 = classes.indexOf(d1.getClass());
		int idx2 = classes.indexOf(d2.getClass());
		return Integer.valueOf(idx1).compareTo(idx2);
	}
}
