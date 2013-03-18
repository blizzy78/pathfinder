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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
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

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class World implements IActor {
	static final int ANIMATION_FRAMES_PER_SECOND = 100;
	static final int REDRAW_FRAMES_PER_SECOND = 60;
	static final int CELL_PIXEL_SIZE = 7;
	static final int CELL_SPACING = 1;

	private static final RGB COLOR = new RGB(100, 30, 0);

	private int width;
	private int height;
	private ColorRegistry colorRegistry;
	private Canvas canvas;
	private IActor[] actors = new IActor[0];
	private Road[] roads = new Road[0];
	private Timer timer = new Timer();
	private boolean initialPaint;
	private DoubleBuffer doubleBuffer;
	private Map<Point, Set<Road>> roadsAtCache = new HashMap<>();
	private Map<Point, Boolean> isRoadAtCache = new HashMap<>();
	private ListeningExecutorService taskExecutor = MoreExecutors.listeningDecorator(Executors.newScheduledThreadPool(
			8, new ThreadFactoryBuilder().setNameFormat("Task Executor (%d)").build())); //$NON-NLS-1$
	private boolean paused;

	public World(Composite parent, int width, int height) {
		this.width = width;
		this.height = height;

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
				dispose();
			}
		});

		doubleBuffer = new DoubleBuffer(parent.getDisplay(), getWidthHint(), getHeightHint());

		TimerTask animationTask = new TimerTask() {
			@Override
			public void run() {
				animateWorld();
			}
		};
		timer.scheduleAtFixedRate(animationTask, 0, Math.round(1000d / ANIMATION_FRAMES_PER_SECOND));

		TimerTask redrawTask = new TimerTask() {
			@Override
			public void run() {
				redrawWorld();
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

	private void dispose() {
		timer.cancel();
		try {
			taskExecutor.shutdown();
			taskExecutor.awaitTermination(30, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// ignore
		}
		doubleBuffer.dispose();
		colorRegistry.dispose();
	}

	@Override
	public boolean contains(Point point) {
		return (point.x >= 0) && (point.x < width) && (point.y >= 0) && (point.y < height);
	}

	private void paint(GC gc, Rectangle damagedRegion) {
		boolean redrawAll = !initialPaint || mustRedrawAllObjects();

		if (redrawAll) {
			paint(doubleBuffer.getGC(), 0);
			doubleBuffer.setDirty(true);
		}

		for (int pass = 0;; pass++) {
			if (!paintObjects(pass, redrawAll)) {
				break;
			}
		}

		if (doubleBuffer.isDirty()) {
			doubleBuffer.paint(gc, damagedRegion);
			doubleBuffer.setDirty(false);
		}
	}

	private boolean mustRedrawAllObjects() {
		for (IActor actor : actors) {
			if (actor.mustRedraw()) {
				return true;
			}
		}
		return false;
	}

	private boolean paintObjects(int pass, boolean force) {
		boolean needMorePasses = false;
		GC gc = doubleBuffer.getGC();
		for (IActor actor : actors) {
			if (force || actor.mustRedraw()) {
				if (actor.paint(gc, pass)) {
					needMorePasses = true;
				}
				doubleBuffer.setDirty(true);
			}
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

	void add(IActor actor) {
		List<IActor> newActors = new ArrayList<>(Arrays.asList(actors));
		newActors.add(actor);
		actors = newActors.toArray(new IActor[0]);

		if (actor instanceof Road) {
			List<Road> newRoads = new ArrayList<>(Arrays.asList(roads));
			newRoads.add((Road) actor);
			roads = newRoads.toArray(new Road[0]);
		}
	}

	ColorRegistry getColorRegistry() {
		return colorRegistry;
	}

	EnumSet<Direction> getPossibleDirectionsAt(Point location) {
		if (!contains(location)) {
			throw new IllegalArgumentException();
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
				isRoadAtCache.put(location, true);
			}
			return isRoad;
		}
	}

	@Override
	public void animate() {
		// world does not move
	}

	private void animateWorld() {
		if (!paused) {
			List<ListenableFuture<?>> futures = new ArrayList<>(Math.max(actors.length, 1));
			for (final IActor actor : actors) {
				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						actor.animate();
					}
				};
				ListenableFuture<?> future = taskExecutor.submit(runnable);
				futures.add(future);
			}

			try {
				Futures.allAsList(futures).get();
			} catch (InterruptedException e) {
				// ignore
			} catch (ExecutionException e) {
				throw new RuntimeException(e.getCause());
			}
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

	@Override
	public boolean canBlockRoad() {
		return false;
	}

	public void forceCompleteRedraw() {
		initialPaint = false;
	}

	public Control getControl() {
		return canvas;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}
}