package de.logotakt.logolyze.model.olap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.logotakt.logolyze.model.interfaces.ICube;
import de.logotakt.logolyze.model.interfaces.IDbStructure;

/**
 * This class represents the whole structure of the database, read from the metadata. This mainly will contain one or
 * more Cubes.
 */
public class DbStructure implements IDbStructure {

    private Map<String, ICube> cubes;

    @Override
    public ICube getCube(final String name) {
        return this.cubes.get(name);
    }

    @Override
    public Iterator<ICube> iterator() {
        return this.cubes.values().iterator();
    }

    /**
     * Adds one cube to this DbStructure.
     * @param c The cube to be added
     */
    void addCube(final ICube c) {
        this.cubes.put(c.getName(), c);
    }

    /**
     * Creates a new DbStructure.
     */
    DbStructure() {
        this.cubes = new HashMap<String, ICube>();
    }

}
