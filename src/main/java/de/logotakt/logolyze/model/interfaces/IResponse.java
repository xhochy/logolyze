package de.logotakt.logolyze.model.interfaces;

import java.util.Collection;

public interface IResponse {
	Collection<IOLAPGraph> getGraphs();
	long getRequestDuration();
}
