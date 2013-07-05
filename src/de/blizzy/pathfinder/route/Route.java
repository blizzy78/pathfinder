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
package de.blizzy.pathfinder.route;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class Route {
	public static void main(String... args) {
		Node frankfurt = new Node("Frankfurt"); //$NON-NLS-1$
		Node wuerzburg = new Node("Wuerzburg"); //$NON-NLS-1$
		Node ludwigshafen = new Node("Ludwigshafen"); //$NON-NLS-1$
		Node kaiserslautern = new Node("Kaiserslautern"); //$NON-NLS-1$
		Node heilbronn = new Node("Heilbronn"); //$NON-NLS-1$
		Node saarbruecken = new Node("Saarbruecken"); //$NON-NLS-1$
		Node karlsruhe = new Node("Karlsruhe"); //$NON-NLS-1$

		final Map<TwoNodes, Integer> distances = Maps.newHashMap();
		distances.put(new TwoNodes(frankfurt, kaiserslautern), 103);
		distances.put(new TwoNodes(frankfurt, wuerzburg), 116);
		distances.put(new TwoNodes(kaiserslautern, ludwigshafen), 53);
		distances.put(new TwoNodes(ludwigshafen, wuerzburg), 183);
		distances.put(new TwoNodes(kaiserslautern, saarbruecken), 70);
		distances.put(new TwoNodes(saarbruecken, karlsruhe), 145);
		distances.put(new TwoNodes(karlsruhe, heilbronn), 84);
		distances.put(new TwoNodes(heilbronn, wuerzburg), 102);

		final Map<Node, Integer> estimatedDistancesToTarget = Maps.newHashMap();
		estimatedDistancesToTarget.put(frankfurt, 96);
		estimatedDistancesToTarget.put(wuerzburg, 0);
		estimatedDistancesToTarget.put(kaiserslautern, 158);
		estimatedDistancesToTarget.put(ludwigshafen, 108);
		estimatedDistancesToTarget.put(saarbruecken, 222);
		estimatedDistancesToTarget.put(karlsruhe, 140);
		estimatedDistancesToTarget.put(heilbronn, 87);

		final Map<Node, Set<Node>> adjacentNodes = Maps.newHashMap();
		adjacentNodes.put(frankfurt, Sets.newHashSet(kaiserslautern, wuerzburg));
		adjacentNodes.put(kaiserslautern, Sets.newHashSet(frankfurt, ludwigshafen, saarbruecken));
		adjacentNodes.put(ludwigshafen, Sets.newHashSet(kaiserslautern, wuerzburg));
		adjacentNodes.put(wuerzburg, Sets.newHashSet(frankfurt, ludwigshafen, heilbronn));
		adjacentNodes.put(saarbruecken, Sets.newHashSet(kaiserslautern, karlsruhe));
		adjacentNodes.put(karlsruhe, Sets.newHashSet(saarbruecken, heilbronn));
		adjacentNodes.put(heilbronn, Sets.newHashSet(karlsruhe, wuerzburg));

		IAStarFunctions aStarFunctions = new IAStarFunctions() {
			@Override
			public int getEstimatedDistanceToTarget(Node node) {
				return estimatedDistancesToTarget.get(node);
			}

			@Override
			public int getDistance(Node node1, Node node2) {
				return distances.get(new TwoNodes(node1, node2));
			}

			@Override
			public Set<Node> getAdjacentNodes(Node node) {
				return adjacentNodes.get(node);
			}
		};

		AStar aStar = new AStar();
		List<Node> route = aStar.getShortestRoute(saarbruecken, wuerzburg, aStarFunctions);
		System.out.printf("%s", route).println(); //$NON-NLS-1$
	}
}
