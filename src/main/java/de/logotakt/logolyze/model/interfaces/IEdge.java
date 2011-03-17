package de.logotakt.logolyze.model.interfaces;

import java.util.Map;

/**
 * This interface exports all functions that are accessible for
 * a given edge of a graph.
 */
public interface IEdge {

    /**
     * Returns a map that contains all the measures associated with this
     * edge as values, and the name of their type as key.
     *
     * @return A map containing all this edge's measures
     */
    Map<String, ? extends IMeasure> getMeasures();
}
