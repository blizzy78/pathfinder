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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

class AStar {
	AStar() {
	}

	List<Node> getShortestRoute(Node origin, Node target, IAStarFunctions function) {
		final Map<Node, Integer> distancesFromOriginToTarget = Maps.newHashMap();
		Map<Node, Integer> distancesFromOriginToNode = Maps.newHashMap();
		Comparator<Node> openNodesComparator = new Comparator<Node>() {
			@Override
			public int compare(Node n1, Node n2) {
				Integer d1 = distancesFromOriginToTarget.get(n1);
				if (d1 == null) {
					d1 = 0;
				}
				Integer d2 = distancesFromOriginToTarget.get(n2);
				if (d2 == null) {
					d2 = 0;
				}
				return d1.compareTo(d2);
			}
		};
		SortedSet<Node> openNodes = new TreeSet<>(openNodesComparator);
		Set<Node> closedNodes = Sets.newHashSet(origin);
		Map<Node, Node> predecessors = Maps.newHashMap();

		distancesFromOriginToNode.put(origin, 0);
		openNodes.add(origin);

		do {
			Node currentNode = openNodes.first();
			openNodes.remove(currentNode);
			distancesFromOriginToTarget.remove(currentNode);

			if (currentNode.equals(target)) {
				return toRoute(target, predecessors);
			}

			closedNodes.add(currentNode);

			expandNode(currentNode, function, distancesFromOriginToTarget, distancesFromOriginToNode,
					openNodes, closedNodes, predecessors);
		} while (!openNodes.isEmpty());

		return null;
	}

	private void expandNode(Node currentNode, IAStarFunctions function, Map<Node, Integer> distancesFromOriginToTarget,
			Map<Node, Integer> distancesFromOriginToNode, Set<Node> openNodes, Set<Node> closedNodes, Map<Node, Node> predecessors) {

		for (Node successor : function.getAdjacentNodes(currentNode)) {
			if (closedNodes.contains(successor)) {
				continue;
			}

			int distanceFromCurrentToSuccessor = function.getDistance(currentNode, successor);
			int newDistanceFromOriginToSuccessor = distancesFromOriginToNode.get(currentNode) + distanceFromCurrentToSuccessor;

			boolean open = openNodes.contains(successor);
			if (open && (newDistanceFromOriginToSuccessor >= distancesFromOriginToNode.get(successor))) {
				continue;
			}

			predecessors.put(successor, currentNode);
			distancesFromOriginToNode.put(successor, newDistanceFromOriginToSuccessor);

			int estimatedDistanceFromSuccessorToTarget = function.getEstimatedDistanceToTarget(successor);
			int newDistanceFromOriginToTarget = newDistanceFromOriginToSuccessor + estimatedDistanceFromSuccessorToTarget;
			distancesFromOriginToTarget.put(successor, newDistanceFromOriginToTarget);
			if (!open) {
				openNodes.add(successor);
			}
		}
	}

	private List<Node> toRoute(Node target, Map<Node, Node> predecessors) {
		List<Node> route = Lists.newArrayList();
		Node node = target;
		while (node != null) {
			route.add(node);
			node = predecessors.get(node);
		}

		Collections.reverse(route);
		return route;
	}
}
