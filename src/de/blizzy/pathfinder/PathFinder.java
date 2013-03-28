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

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.blizzy.pathfinder.actor.Area;
import de.blizzy.pathfinder.actor.Building;
import de.blizzy.pathfinder.actor.Road;
import de.blizzy.pathfinder.actor.RoadBlock;
import de.blizzy.pathfinder.actor.TrafficDensityOverlay;
import de.blizzy.pathfinder.actor.TrafficLight;
import de.blizzy.pathfinder.actor.Vehicle;
import de.blizzy.pathfinder.actor.World;

public class PathFinder {
	private boolean running = true;

	public static void main(String... args) {
		new PathFinder().run();
	}

	private void run() {
		final Display display = Display.getDefault();

		Shell shell = new Shell(display);
		shell.setText("PathFinder"); //$NON-NLS-1$

		final World world = createControls(shell);
		shell.setLayout(GridLayoutFactory.swtDefaults().margins(10, 10).create());

		shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent e) {
				running = false;
				display.wake();
			}
		});
		shell.addControlListener(new ControlListener() {
			@Override
			public void controlResized(ControlEvent e) {
				world.forceCompleteRedraw();
			}

			@Override
			public void controlMoved(ControlEvent e) {
				world.forceCompleteRedraw();
			}
		});

		shell.pack();
		shell.open();

		while (running && !display.isDisposed()) {
			try {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			} catch (RuntimeException e) {
			}
		}

		shell.dispose();
		display.dispose();
	}

	private World createControls(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);

		final World world = createWorld(composite);
		GridData gd = new GridData();
		gd.widthHint = world.getWidthHint();
		gd.heightHint = world.getHeightHint();
		world.getControl().setLayoutData(gd);

		Button pauseButton = new Button(composite, SWT.TOGGLE);
		pauseButton.setText("Pause"); //$NON-NLS-1$

		composite.setLayout(GridLayoutFactory.swtDefaults().margins(0, 0).create());

		pauseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				world.setPaused(((Button) e.widget).getSelection());
			}
		});

		return world;
	}

	private World createWorld(Composite parent) {
		World world = new World(parent, 96, 96);

		// roads
		for (int i = 0; i < 11; i++) {
			if ((i % 2) == 0) {
				// north-south
				new Road(world, new Point(i * 9 + ((i > 4) ? 4 : 0), 0), (i == 4) ? 6 : 2, 96, Direction.SOUTH);
				// west-east
				new Road(world, new Point(0, i * 9 + ((i > 4) ? 4 : 0)), (i == 4) ? 6 : 2, 96, Direction.EAST);
			}
		}

		// buildings
		for (int x = 0; x < 10; x++) {
			for (int y = 0; y < 10; y++) {
				new Building(world, new Area(world, new Rectangle(
						x * 8 + ((x / 2) + 1) * 2 + ((x > 3) ? 4 : 0),
						y * 8 + ((y / 2) + 1) * 2 + ((y > 3) ? 4 : 0),
						8, 8)));
			}
		}

		// road blocks
		new RoadBlock(world, new Point(93, 77));

//		// vehicles
		for (int i = 1; i <= 500; i++) {
			new Vehicle(world, new Point(36, 40));
		}

		// traffic lights
		for (int x = 0; x < 6; x++) {
			for (int y = 0; y < 6; y++) {
				new TrafficLight(world, new Point(
						x * 18 + ((x > 2) ? 4 : 0),
						y * 18 + ((y > 2) ? 4 : 0)),
						(x == 2) ? 6 : 2,
						(y == 2) ? 6 : 2);
			}
		}

		new TrafficDensityOverlay(world);

		return world;
	}
}
