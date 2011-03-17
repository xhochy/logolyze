package de.logotakt.logolyze.model.olap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.logotakt.logolyze.model.interfaces.ICube;
import de.logotakt.logolyze.model.interfaces.IDimension;
import de.logotakt.logolyze.model.interfaces.IMeasureType;

/**
 * This class represents a cube depicted in the metadata.
 */
public class Cube implements ICube {

    private Map<String, IDimension> dimensions;
    private List<IMeasureType> measures;
    private final String name;
    private final boolean directed;

    @Override
    public IDimension getDimension(final String name) {
        return this.dimensions.get(name);
    }

    @Override
    public Collection<IMeasureType> getMeasureTypes() {
        return this.measures;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Iterator<IDimension> iterator() {
        return this.dimensions.values().iterator();
    }

    /**
     * Adds a dimension to this cube.
     * 
     * @param d The dimension to be added
     */
    void addDimension(final IDimension d) {
        this.dimensions.put(d.getName(), (Dimension) d);
    }

    /**
     * Adds a MeasureType to this cube.
     * 
     * @param mt The MeasureType to be added
     */
    void addMeasure(final IMeasureType mt) {
        this.measures.add((MeasureType) mt);
    }

    /**
     * Constructs a new cube with a specific name.
     * 
     * @param name The name of the cube to be constructed
     */
    Cube(final String name, final boolean directed) {
        this.name = name;

        this.dimensions = new HashMap<String, IDimension>();
        this.measures = new LinkedList<IMeasureType>();
        this.directed = directed;
    }

    @Override
    public boolean isDirected() {
        return this.directed;
    }
}
