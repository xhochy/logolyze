package de.logotakt.logolyze.model.interfaces;

import java.util.Collection;

/**
 * Represents the metadata for one OLAP cube. A Cube contains a number of
 * Dimensions (which again split up, see there) and a set of MeasureTypes which
 * are available for the Nodes and Edges of the Graphs in this Cube. The
 * information in this class is obtained from the metadata tables.
 */
public interface ICube extends Iterable<IDimension> {
    /**
     * Returns the name of this Cube.
     * @return The name of this Cube.
     */
    String getName();

    /**
     * Retrieves the Dimension with the given name from this Cube.
     * @param name The name (i.e. identifying String) of the Dimension to get
     * @return The dimension of this Cube with the given name.
     */
    IDimension getDimension(String name);

    /**
     * Returns the MeasureTypes which are associated with the Nodes and Edges of
     * the Graphs in this Cube. The returned Collection is read-only.
     * @return The MeasureTypes of the Graphs in this Cube.
     */
    Collection<IMeasureType> getMeasureTypes();
    
    boolean isDirected();
}
