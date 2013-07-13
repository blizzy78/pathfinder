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
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import de.blizzy.pathfinder.Direction;
import de.blizzy.pathfinder.route.AStar;
import de.blizzy.pathfinder.route.IAStarFunctions;
import de.blizzy.pathfinder.route.INode;
import de.blizzy.pathfinder.route.RouteNode;

public class World implements IDrawable {
	static final int ANIMATION_FRAMES_PER_SECOND = 100;
	static final int REDRAW_FRAMES_PER_SECOND = 60;
	static final int CELL_PIXEL_SIZE = 7;
	static final int CELL_SPACING = 1;

	private static final RGB COLOR = new RGB(100, 30, 0);

	private int width;
	private int height;
	private TrafficDensity trafficDensity;
	private ColorRegistry colorRegistry;
	private Canvas canvas;
	private IDrawable[] drawables = new IDrawable[0];
	private IActor[] actors = new IActor[0];
	private Road[] roads = new Road[0];
	private Vehicle[] vehicles = new Vehicle[0];
	private Timer timer = new Timer();
	private boolean initialPaint;
	private DoubleBuffer doubleBuffer;
	private Map<Point, Set<Road>> roadsAtCache = new HashMap<>();
	private Map<Point, Boolean> isRoadAtCache = new HashMap<>();
	private AtomicBoolean paused = new AtomicBoolean();
	private List<IClickListener> clickListeners = new ArrayList<>();

	public World(Composite parent, int width, int height) {
		this.width = width;
		this.height = height;

		trafficDensity = new TrafficDensity(this);
		colorRegistry = new ColorRegistry(parent.getDisplay());

		canvas = new Canvas(parent, SWT.BORDER | SWT.NO_BACKGROUND);
		canvas.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				paint(e.gc, new Rectangle(e.x, e.y, e.width, e.height));
				initialPaint = true;
			}
		});
		canvas.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				handleDispose();
			}
		});
		canvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				Point location = new Point(e.x / (CELL_PIXEL_SIZE + CELL_SPACING), e.y / (CELL_PIXEL_SIZE + CELL_SPACING));
				fireClick(location);
			}
		});

		doubleBuffer = new DoubleBuffer(parent.getDisplay(), getWidthHint(), getHeightHint());

		TimerTask animationTask = new TimerTask() {
			@Override
			public void run() {
				if (!paused.get()) {
					animateWorld();
				}
			}
		};
		timer.scheduleAtFixedRate(animationTask, 0, Math.round(1000d / ANIMATION_FRAMES_PER_SECOND));

		TimerTask redrawTask = new TimerTask() {
			@Override
			public void run() {
				if (!paused.get()) {
					redrawWorld();
				}
			}
		};
		timer.scheduleAtFixedRate(redrawTask, 0, Math.round(1000d / REDRAW_FRAMES_PER_SECOND));
	}

	public int getWidthHint() {
		return width * CELL_PIXEL_SIZE + (width - 1) * CELL_SPACING;
	}

	public int getHeightHint() {
		return height * CELL_PIXEL_SIZE + (height - 1) * CELL_SPACING;
	}

	@Override
	public void dispose() {
		canvas.dispose();
	}

	private void handleDispose() {
		timer.cancel();
		for (IDrawable drawable : drawables) {
			drawable.dispose();
		}
		doubleBuffer.dispose();
		colorRegistry.dispose();
	}

	@Override
	public boolean contains(Point point) {
		return (point.x >= 0) && (point.x < width) && (point.y >= 0) && (point.y < height);
	}

	private void paint(GC gc, Rectangle damagedRegion) {
		if (!initialPaint || mustRedrawAllObjects()) {
			paint(doubleBuffer.getGC(), 0);
			doubleBuffer.setDirty(true);

			for (int pass = 0;; pass++) {
				if (!paintObjects(pass)) {
					break;
				}
			}

			if (doubleBuffer.isDirty()) {
				doubleBuffer.paint(gc, damagedRegion);
				doubleBuffer.setDirty(false);
			}
		}
	}

	private boolean mustRedrawAllObjects() {
		for (IDrawable drawable : drawables) {
			if (drawable.mustRedraw()) {
				return true;
			}
		}
		return false;
	}

	private boolean paintObjects(int pass) {
		boolean needMorePasses = false;
		GC gc = doubleBuffer.getGC();
		for (IDrawable drawable : drawables) {
			if (drawable.paint(gc, pass)) {
				needMorePasses = true;
			}
			doubleBuffer.setDirty(true);
		}
		return needMorePasses;
	}

	@Override
	public boolean paint(GC gc, int pass) {
		gc.setBackground(colorRegistry.getColor(COLOR));
		gc.fillRectangle(0, 0, width * CELL_PIXEL_SIZE + (width - 1) * CELL_SPACING, height * CELL_PIXEL_SIZE + (height - 1) * CELL_SPACING);
		return false;
	}

	@Override
	public boolean mustRedraw() {
		// world does never change
		return false;
	}

	void add(IDrawable drawable) {
		List<IDrawable> newDrawables = new ArrayList<>(Arrays.asList(drawables));
		newDrawables.add(drawable);
		Collections.sort(newDrawables, DrawableLayerComparator.INSTANCE);
		drawables = newDrawables.toArray(new IDrawable[0]);

		if (drawable instanceof IActor) {
			List<IActor> newActors = new ArrayList<>(Arrays.asList(actors));
			newActors.add((IActor) drawable);
			Collections.sort(newActors, DrawableLayerComparator.INSTANCE);
			actors = newActors.toArray(new IActor[0]);
		}

		if (drawable instanceof Road) {
			List<Road> newRoads = new ArrayList<>(Arrays.asList(roads));
			newRoads.add((Road) drawable);
			roads = newRoads.toArray(new Road[0]);
		}

		if (drawable instanceof Vehicle) {
			List<Vehicle> newVehicles = new ArrayList<>(Arrays.asList(vehicles));
			newVehicles.add((Vehicle) drawable);
			vehicles = newVehicles.toArray(new Vehicle[0]);
		}
	}

	ColorRegistry getColorRegistry() {
		return colorRegistry;
	}

	public EnumSet<Direction> getPossibleDirectionsAt(Point location) {
		if (!contains(location)) {
			throw new IllegalArgumentException("location not within world: " + location); //$NON-NLS-1$
		}

		if (!isRoadAt(location)) {
			throw new IllegalArgumentException("location must be on a road"); //$NON-NLS-1$
		}

		EnumSet<Direction> directions = EnumSet.noneOf(Direction.class);
		if ((location.y > 0) && isRoadAt(new Point(location.x, location.y - 1))) {
			directions.add(Direction.NORTH);
		}
		if ((location.x > 0) && isRoadAt(new Point(location.x - 1, location.y))) {
			directions.add(Direction.WEST);
		}
		if ((location.y < (height - 1)) && isRoadAt(new Point(location.x, location.y + 1))) {
			directions.add(Direction.SOUTH);
		}
		if ((location.x < (width - 1)) && isRoadAt(new Point(location.x + 1, location.y))) {
			directions.add(Direction.EAST);
		}
		return directions;
	}

	Set<Road> getRoadsAt(Point location) {
		synchronized (roadsAtCache) {
			Set<Road> roads = roadsAtCache.get(location);
			if (roads == null) {
				roads = new HashSet<>(2);
				for (Road road : this.roads) {
					if (road.contains(location)) {
						roads.add(road);
					}
				}
				roadsAtCache.put(location, roads);
			}
			return roads;
		}
	}

	Road[] getAllRoads() {
		return roads;
	}

	Vehicle[] getAllVehicles() {
		return vehicles;
	}

	boolean isRoadAt(Point location) {
		synchronized (isRoadAtCache) {
			Boolean isRoad = isRoadAtCache.get(location);
			if (isRoad == null) {
				isRoad = false;
				for (Road road : roads) {
					if (road.contains(location)) {
						isRoad = true;
						break;
					}
				}
				isRoadAtCache.put(location, isRoad);
			}
			return isRoad;
		}
	}

	private void animateWorld() {
		for (IActor actor : actors) {
			actor.animate();
		}
	}

	private void redrawWorld() {
		if (!canvas.isDisposed()) {
			Display display = canvas.getDisplay();
			if (!display.isDisposed()) {
				display.syncExec(new Runnable() {
					@Override
					public void run() {
						if (!canvas.isDisposed()) {
							canvas.redraw();
						}
					}
				});
			}
		}
	}

	int getWidth() {
		return width;
	}

	int getHeight() {
		return height;
	}

	boolean isRoadBlockedAt(Point location, Direction comingFrom, Point currentLocation) {
		for (IActor actor : actors) {
			if (actor instanceof TrafficLight) {
				TrafficLight trafficLight = (TrafficLight) actor;
				if (!trafficLight.contains(currentLocation)) {
					if (trafficLight.contains(location) && !trafficLight.isAllowed(comingFrom)) {
						return true;
					}
				}
			} else if (actor.canBlockRoad() && actor.contains(location)) {
				return true;
			}
		}
		return false;
	}

	boolean isParkingVehicleAt(Point location) {
		for (Vehicle vehicle : vehicles) {
			if (vehicle.contains(location) && vehicle.isParking()) {
				return true;
			}
		}
		return false;
	}

	public void forceCompleteRedraw() {
		initialPaint = false;
	}

	public Control getControl() {
		return canvas;
	}

	public void setPaused(boolean paused) {
		this.paused.set(paused);
	}

	public void addClickListener(IClickListener listener) {
		synchronized (clickListeners) {
			clickListeners.add(listener);
		}
	}

	public void removeClickListener(IClickListener listener) {
		synchronized (clickListeners) {
			clickListeners.remove(listener);
		}
	}

	private void fireClick(Point location) {
		List<IClickListener> listeners;
		synchronized (clickListeners) {
			listeners = new ArrayList<>(clickListeners);
		}
		ClickEvent event = new ClickEvent(this, location);
		for (IClickListener listener : listeners) {
			listener.click(event);
		}
	}

	TrafficDensity getTrafficDensity() {
		return trafficDensity;
	}

	public List<Point> getShortestRoute(Point originLocation, final Point targetLocation) {
		RouteNode origin = new RouteNode(originLocation);
		RouteNode target = new RouteNode(targetLocation);
		IAStarFunctions aStarFunctions = new IAStarFunctions() {
			@Override
			public double getEstimatedDistanceToTarget(INode node) {
				RouteNode routeNode = (RouteNode) node;
				Point location = routeNode.getLocation();
				int x = Math.abs(targetLocation.x - location.x);
				int y = Math.abs(targetLocation.y - location.y);
				return Math.sqrt(Math.pow(x, 2d) + Math.pow(y, 2d));
			}

			@Override
			public double getDistance(INode node1, INode node2) {
				return 1d;
			}

			@Override
			public Set<INode> getAdjacentNodes(INode node) {
				RouteNode routeNode = (RouteNode) node;
				final Point location = routeNode.getLocation();
				EnumSet<Direction> directions = getPossibleDirectionsAt(location);
				Function<Direction, INode> function = new Function<Direction, INode>() {
					@Override
					public INode apply(Direction direction) {
						switch (direction) {
							case NORTH:
								return new RouteNode(new Point(location.x, location.y - 1));
							case SOUTH:
								return new RouteNode(new Point(location.x, location.y + 1));
							case EAST:
								return new RouteNode(new Point(location.x + 1, location.y));
							case WEST:
								return new RouteNode(new Point(location.x - 1, location.y));
							default:
								throw new IllegalArgumentException("unknown direction: " + direction); //$NON-NLS-1$
						}
					}
				};
				return new HashSet<>(Lists.transform(new ArrayList<>(directions), function));
			}
		};
		List<INode> route = new AStar().getShortestRoute(origin, target, aStarFunctions);
		if (route != null) {
			Function<INode, Point> function = new Function<INode, Point>() {
				@Override
				public Point apply(INode node) {
					return ((RouteNode) node).getLocation();
				}
			};
			return new ArrayList<>(Lists.transform(route, function));
		} else {
			return null;
		}
	}

	public void redraw() {
		initialPaint = true;
		canvas.redraw();
	}
}
