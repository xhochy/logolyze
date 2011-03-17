package de.logotakt.logolyze.view;

import de.logotakt.logolyze.model.interfaces.IEdge;
import de.logotakt.logolyze.model.interfaces.INode;
import de.logotakt.logolyze.model.interfaces.IOLAPGraph;
import de.logotakt.logolyze.model.interfaces.IRequest;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

/**
 * Simple class that implements the {@link IOLAPGraph} interface so that spies on a JUNG
 * {@link DirectedSparseMultigraph} could be created.
 */
@SuppressWarnings("serial")
public class MockedGraph extends DirectedSparseMultigraph<INode, IEdge> implements IOLAPGraph {
    private IRequest request;

    /**
     * Set the request that will be returned from getResultOf.
     * @param req The request that belongs to this Graph
     */
    public void setRequest(final IRequest req) {
        request = req;
    }

    @Override
    public IRequest getResultOf() {
        return request;
    }

    @Override
    public void setResultOf(IRequest req) {
        this.request = req;
    }

}
