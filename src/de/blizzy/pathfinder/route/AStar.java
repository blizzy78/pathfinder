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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AStar {
	public AStar() {
	}

	public List<INode> getShortestRoute(INode origin, INode target, IAStarFunctions function) {
		final Map<INode, Double> distancesFromOriginToTarget = new HashMap<>();
		Map<INode, Double> distancesFromOriginToNode = new HashMap<>();
		Comparator<INode> openNodesComparator = new Comparator<INode>() {
			@Override
			public int compare(INode n1, INode n2) {
				Double d1 = distancesFromOriginToTarget.get(n1);
				if (d1 == null) {
					d1 = 0d;
				}
				Double d2 = distancesFromOriginToTarget.get(n2);
				if (d2 == null) {
					d2 = 0d;
				}
				return d1.compareTo(d2);
			}
		};
		List<INode> openNodes = new ArrayList<>();
		Set<INode> closedNodes = new HashSet<>();
		closedNodes.add(origin);
		Map<INode, INode> predecessors = new HashMap<>();

		distancesFromOriginToNode.put(origin, 0d);
		openNodes.add(origin);

		do {
			INode currentNode = openNodes.remove(0);
			distancesFromOriginToTarget.remove(currentNode);

			if (currentNode.equals(target)) {
				return toRoute(target, predecessors);
			}

			closedNodes.add(currentNode);

			expandNode(currentNode, function, distancesFromOriginToTarget, distancesFromOriginToNode,
					openNodes, openNodesComparator, closedNodes, predecessors);
		} while (!openNodes.isEmpty());

		return null;
	}

	private void expandNode(INode currentNode, IAStarFunctions function, Map<INode, Double> distancesFromOriginToTarget,
			Map<INode, Double> distancesFromOriginToNode, List<INode> openNodes, Comparator<INode> openNodesComparator,
			Set<INode> closedNodes, Map<INode, INode> predecessors) {

		for (INode successor : function.getAdjacentNodes(currentNode)) {
			if (closedNodes.contains(successor)) {
				continue;
			}

			double distanceFromCurrentToSuccessor = function.getDistance(currentNode, successor);
			double newDistanceFromOriginToSuccessor = distancesFromOriginToNode.get(currentNode) + distanceFromCurrentToSuccessor;

			boolean open = openNodes.contains(successor);
			if (open && (newDistanceFromOriginToSuccessor >= distancesFromOriginToNode.get(successor))) {
				continue;
			}

			predecessors.put(successor, currentNode);
			distancesFromOriginToNode.put(successor, newDistanceFromOriginToSuccessor);

			double estimatedDistanceFromSuccessorToTarget = function.getEstimatedDistanceToTarget(successor);
			double newDistanceFromOriginToTarget = newDistanceFromOriginToSuccessor + estimatedDistanceFromSuccessorToTarget;
			distancesFromOriginToTarget.put(successor, newDistanceFromOriginToTarget);
			if (!open) {
				openNodes.add(successor);
				Collections.sort(openNodes, openNodesComparator);
			}
		}
	}

	private List<INode> toRoute(INode target, Map<INode, INode> predecessors) {
		List<INode> route = new ArrayList<>();
		INode node = target;
		while (node != null) {
			route.add(node);
			node = predecessors.get(node);
		}

		Collections.reverse(route);
		return route;
	}
}
