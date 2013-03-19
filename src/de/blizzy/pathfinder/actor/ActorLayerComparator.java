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
	}

	private ActorLayerComparator() {}

	@Override
	public int compare(IActor a1, IActor a2) {
		int idx1 = classes.indexOf(a1.getClass());
		int idx2 = classes.indexOf(a2.getClass());
		return Integer.valueOf(idx1).compareTo(idx2);
	}
}
