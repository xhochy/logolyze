package de.logotakt.logolyze.model.interfaces;

import edu.uci.ics.jung.graph.Graph;

/**
 * Interface to the Logolyze-specific aspects of a graph.
 */
public interface IOLAPGraph extends Graph<INode, IEdge> {
	/**
	 * Returns a matching request to a graph.
	 *
	 * @return A <code>Request</code>-object that describes
	 *         the current graph.
	 */
	IRequest getResultOf();
	
	void setResultOf(IRequest req);
}
