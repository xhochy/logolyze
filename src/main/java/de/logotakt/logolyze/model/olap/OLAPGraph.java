package de.logotakt.logolyze.model.olap;

import de.logotakt.logolyze.model.interfaces.IEdge;
import de.logotakt.logolyze.model.interfaces.INode;
import de.logotakt.logolyze.model.interfaces.IOLAPGraph;
import de.logotakt.logolyze.model.interfaces.IRequest;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

/**
 * This class represents a graph from the database. It is quite
 * lightweight since JUNG takes care of all the graph logic.
 */
public class OLAPGraph extends DirectedSparseMultigraph
   <INode, IEdge> implements IOLAPGraph {

	/**
	 * Serialization ID.
	 */
	private static final long serialVersionUID = 4063401387772413479L;

	private Request resultOf;

	/**
	 * This creates a new OLAPGraph, being the result of the request
	 * passed.
	 *
	 * @param r The request that would lead to only this graph being retrieved from the database.
	 */
	public OLAPGraph(final Request r) {
		this.resultOf = r;
	}

	/**
	 * NEVER EVER use this. This function is only here because of the
	 * completely braindead way JUNG tends to use it. You will become very, very
	 * unhappy if you create OLAPGraphs like this.
	 */
	public OLAPGraph() {
		this.resultOf = null;
	}

	@Override
	public IRequest getResultOf() {
		return this.resultOf;
	}

    @Override
    public void setResultOf(IRequest req) {
        this.resultOf = (Request) req;
    }
	
	

}
