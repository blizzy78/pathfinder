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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.forms.FormColors;

public class TrafficDensityOverlay implements IActor {
	private static final long CALCULATION_INTERVAL = TimeUnit.MILLISECONDS.convert(2, TimeUnit.SECONDS);
	private static final RGB RED = new RGB(255, 0, 0);
	private static final RGB GREEN = new RGB(0, 255, 0);
	private static final int COLOR_STEPS = 6;
	private static final int ALPHA = 50;

	private AtomicBoolean mustRedraw = new AtomicBoolean(true);
	private long lastCalculation;
	private TrafficDensity trafficDensity;
	private ColorRegistry colorRegistry;
	private Color[] colors;

	public TrafficDensityOverlay(World world) {
		trafficDensity = world.getTrafficDensity();
		colorRegistry = world.getColorRegistry();

		world.add(this);
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean mustRedraw() {
		return mustRedraw.get();
	}

	@Override
	public boolean paint(GC gc, int pass) {
		initColors();

		Map<Area, Double> fill;
		synchronized (this) {
			fill = new HashMap<>(trafficDensity.getRoadSegmentFill());
		}
		int oldAlpha = gc.getAlpha();
		gc.setAlpha(ALPHA);
		for (Map.Entry<Area, Double> entry : fill.entrySet()) {
			int colorIdx = Math.min((int) Math.round(entry.getValue() * COLOR_STEPS), COLOR_STEPS - 1);
			if (colorIdx > 0) {
				gc.setBackground(colors[colorIdx]);
				gc.fillRectangle(entry.getKey().getDrawArea());
			}
		}
		gc.setAlpha(oldAlpha);
		mustRedraw.set(false);
		return false;
	}

	private void initColors() {
		if (colors == null) {
			colors = new Color[COLOR_STEPS];
			for (int i = 0; i < COLOR_STEPS; i++) {
				RGB rgb = FormColors.blend(RED, GREEN, Math.min((int) Math.round(i * (100d / COLOR_STEPS)), 100));
				colors[i] = colorRegistry.getColor(rgb);
			}
		}
	}

	@Override
	public void animate() {
		long now = System.currentTimeMillis();
		if ((now - lastCalculation) >= CALCULATION_INTERVAL) {
			synchronized (this) {
				if (trafficDensity.calculate()) {
					mustRedraw.set(true);
				}
			}
			lastCalculation = now;
		}
	}

	@Override
	public boolean canBlockRoad() {
		return false;
	}

	@Override
	public boolean contains(Point location) {
		// traffic density overlay covers the whole world
		return true;
	}
}
