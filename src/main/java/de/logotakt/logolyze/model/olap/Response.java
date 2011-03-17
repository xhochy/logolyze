package de.logotakt.logolyze.model.olap;

import java.util.Collection;

import de.logotakt.logolyze.model.interfaces.IOLAPGraph;
import de.logotakt.logolyze.model.interfaces.IResponse;

public class Response implements IResponse {
    private long requestDuration;
    private Collection<IOLAPGraph> graphs;

    public Response(Collection<IOLAPGraph> graphs, long requestDuration) {
        this.graphs = graphs;
        this.requestDuration = requestDuration;
    }

    public long getRequestDuration() {
        return requestDuration;
    }

    public Collection<IOLAPGraph> getGraphs() {
        return graphs;
    }
}
