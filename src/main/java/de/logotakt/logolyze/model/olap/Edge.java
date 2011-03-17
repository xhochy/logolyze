package de.logotakt.logolyze.model.olap;

import java.util.HashMap;
import java.util.Map;

import de.logotakt.logolyze.model.interfaces.IEdge;
import de.logotakt.logolyze.model.interfaces.IMeasure;

/**
 * This class represents an edge inside a graph. Actually, it only annotates such an
 * edge, since all the graph logic is handled by JUNG.
 */
public class Edge implements IEdge {

	private Map<String, Measure> measures;

	/**
	 * Create an empty Edge.
	 */
	public Edge() {
	        measures = new HashMap<String, Measure>();
	}

	/**
	 * Adds a measure to this edge. The key of this measure inside the
	 * map is automatically determined by the name of the measure's type.
	 *
	 * @param m The measure to be added
	 */
	public void addMeasure(final Measure m) {
		this.measures.put(m.getSet().getType().getKey(), m);
	}

	@Override
	public Map<String, ? extends IMeasure> getMeasures() {
		return this.measures;
	}

}
