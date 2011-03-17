package de.logotakt.logolyze.model.olap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.logotakt.logolyze.model.interfaces.IHierarchy;
import de.logotakt.logolyze.model.interfaces.IHierarchyLevel;

/**
 * This class represents one hierarchy inside one of the dimensions of a cube described by the database's metadata.
 */
public class Hierarchy implements IHierarchy {

	private List<IHierarchyLevel> levels;
    private Map<String, IHierarchyLevel> levelMap;
    private final String name;
    private final String graphColumn;
    private final String graphColumnValue;

    @Override
    public IHierarchyLevel getLevel(final String name) {
        return this.levelMap.get(name);
    }

    @Override
    public String getName() {
        return this.name;
    }

    String getGraphColumn() {
        return this.graphColumn;
    }

    String getGraphColumnValue() {
        return this.graphColumnValue;
    }

    /**
     * Creates a new Hierarchy with a given name.
     * @param name The name of the new hierarchy
     */
    Hierarchy(final String name, final String graphColumn, final String graphColumnValue) {
        this.name = name;
        this.graphColumn = graphColumn;
        this.graphColumnValue = graphColumnValue;

        this.levelMap = new HashMap<String, IHierarchyLevel>();
        this.levels = new LinkedList<IHierarchyLevel>();
    }

    /**
     * Adds a hierarchy level to this hierarchy.
     * @param l The HierarchyLevel to be added
     */
    public void addLevel(final IHierarchyLevel l) {
        this.levelMap.put(l.getName(), l);
        this.levels.add(l);
    }

    @Override
    public Iterator<IHierarchyLevel> iterator() {
        return this.levels.iterator();
    }

    @Override
    public String toString() {
        return String.valueOf(name);
    }
}
