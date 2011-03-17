package de.logotakt.logolyze.model.olap;

import java.util.HashMap;
import java.util.Map;

import de.logotakt.logolyze.model.interfaces.IMeasure;
import de.logotakt.logolyze.model.interfaces.INode;

/**
 * This class represents a node inside a graph. It actually only annotates these
 * nodes, since JUNG handles all the graph related logic.
 */
public class Node implements INode {

	private final String label;
	private Map<String, Measure> measures;

	/**
	 * Creates a new Node object with the given label.
	 *
	 * @param label The label the new node should carry.
	 */
	Node(final String label) {
		this.label = label;

		this.measures = new HashMap<String, Measure>();
	}

	/**
	 * This adds a measure to this node, and automatically sets the key for this
	 * measure to the name of the measure's type.
	 *
	 * @param m The measure to be added
	 */
	public void addMeasure(final Measure m) {
		this.measures.put(m.getSet().getType().getKey(), m);
	}

	@Override
	public String getLabel() {
		return this.label;
	}

	@Override
	public Map<String, ? extends IMeasure> getMeasures() {
		return this.measures;
	}
}
