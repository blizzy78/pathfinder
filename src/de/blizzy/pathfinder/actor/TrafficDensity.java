package de.blizzy.pathfinder.actor;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

class TrafficDensity {
	private World world;
	private Map<Area, Integer> roadSegmentMaxVehicles;
	private Map<Area, Integer> lastRoadSegmentVehicles = Collections.emptyMap();
	private Map<Area, Double> roadSegmentFill = new HashMap<>();

	TrafficDensity(World world) {
		this.world = world;
	}

	boolean calculate() {
		init();

		Map<Area, Integer> roadSegmentVehicles = new HashMap<>();
		for (Vehicle vehicle : world.getAllVehicles()) {
			Area roadSegment = getRoadSegment(vehicle);
			if (roadSegment != null) {
				Integer numVehicles = roadSegmentVehicles.get(roadSegment);
				roadSegmentVehicles.put(roadSegment, (numVehicles != null) ? (numVehicles + 1) : 1);
			}
		}
		boolean changed = !roadSegmentVehicles.equals(lastRoadSegmentVehicles);
		if (changed) {
			roadSegmentFill.clear();
			for (Area roadSegment : roadSegmentVehicles.keySet()) {
				int vehicles = roadSegmentVehicles.get(roadSegment);
				int maxVehicles = roadSegmentMaxVehicles.get(roadSegment);
				double fill = (double) vehicles / maxVehicles;
				roadSegmentFill.put(roadSegment, fill);
			}

			lastRoadSegmentVehicles = roadSegmentVehicles;
		}

		return changed;
	}

	Map<Area, Double> getRoadSegmentFill() {
		return roadSegmentFill;
	}

	private Area getRoadSegment(Vehicle vehicle) {
		for (Area roadSegment : roadSegmentMaxVehicles.keySet()) {
			if (roadSegment.contains(vehicle.getLocation())) {
				return roadSegment;
			}
		}
		return null;
	}

	private void init() {
		if (roadSegmentMaxVehicles == null) {
			roadSegmentMaxVehicles = new HashMap<>();
			for (Road road : world.getAllRoads()) {
				for (Area roadSegment : getRoadSegments(road)) {
					Rectangle area = roadSegment.getArea();
					roadSegmentMaxVehicles.put(roadSegment, area.width * area.height);
				}
			}
		}
	}

	private Set<Area> getRoadSegments(Road road) {
		Road[] roads = world.getAllRoads();
		Rectangle area = road.getArea().getArea();
		Set<Area> segments = new HashSet<>();

		// north-south
		if ((area.width == 2) && (area.height > 2)) {
			int endY = area.y + area.height;
			int startY = area.y - 1;
			for (int y = startY + 1; y < endY; y++) {
				for (Road r : roads) {
					if (!r.equals(road) && r.contains(new Point(area.x, y))) {
						if (startY >= 0) {
							Rectangle a = new Rectangle(area.x, startY, area.width, y - startY);
							segments.add(new Area(world, a));
						}
						startY = y + r.getArea().getArea().height;
						y = startY;
					}
				}
			}
		}

		// west-east
		if ((area.width > 2) && (area.height == 2)) {
			int endX = area.x + area.width;
			int startX = area.x - 1;
			for (int x = startX + 1; x < endX; x++) {
				for (Road r : roads) {
					if (!r.equals(road) && r.contains(new Point(x, area.y))) {
						if (startX >= 0) {
							Rectangle a = new Rectangle(startX, area.y, x - startX, area.height);
							segments.add(new Area(world, a));
						}
						startX = x + r.getArea().getArea().width;
						x = startX;
					}
				}
			}
		}

		return segments;
	}
}
