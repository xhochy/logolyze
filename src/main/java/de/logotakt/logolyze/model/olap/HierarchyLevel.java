package de.logotakt.logolyze.model.olap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.logotakt.logolyze.model.interfaces.IHierarchyLevel;
import de.logotakt.logolyze.model.interfaces.IHierarchyLevelValue;

/**
 * This class represents a hierarchy level inside one of the hierarchies inside a dimension of a cube described by the
 * database's metadata.
 */
public class HierarchyLevel implements IHierarchyLevel {

    private HierarchyLevel child;
    private final HierarchyLevel parent;
    private final String name;
    private List<IHierarchyLevelValue> values;
    private Map<String, IHierarchyLevelValue> valueMap;

    @Override
    public IHierarchyLevel childLevel() {
        return this.child;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Collection<IHierarchyLevelValue> getValues() {
        return this.values;
    }

    @Override
    public IHierarchyLevel parentLevel() {
        return this.parent;
    }

    /**
     * This sets the child hierarchy level of this hierarchy level. Hierarchy levels form a list (rather than a 'real'
     * tree), so it really only allows for one child, instead of children.
     *
     * @param child The child to be set
     */
    public void setChild(final HierarchyLevel child) {
        this.child = child;
    }

    /**
     * Creates a new HierarchyLevel with given parent and name. If this HierarchyLevel comes directly below a
     * 'hierarchy' (i.e. is the root of the HierarchyLevel-list inside this hierarchy), the parent should be null
     *
     * @param name The name of this HierarchyLevel
     * @param parent The parent HierarchyLevel, or null if this is the root
     */
    HierarchyLevel(final String name, final HierarchyLevel parent) {
        this.name = name;
        this.parent = parent;

        this.values = new LinkedList<IHierarchyLevelValue>();
        this.valueMap = new HashMap<String, IHierarchyLevelValue>();
    }

    /**
     * This adds a HierarchyLevelValue to the list of hierarchy level values that come directly below this hierarchy
     * level.
     *
     * @param value The value to be added
     */
    public void addValue(final HierarchyLevelValue value) {
        this.values.add(value);
        this.valueMap.put(value.getValue(), value);
    }

    /**
     * Returns the HierarchyLevelValue below this HierarchyLevel with a specific string value.
     *
     * @param valueString The string value you want to get the HierarchyLevelValue object to
     * @return The HierarchyLevelValue object with the valueString value, or null if nonexistant
     */
    HierarchyLevelValue valueByString(final String valueString) {
        return (HierarchyLevelValue) this.valueMap.get(valueString);
    }

    @Override
    public Iterator<IHierarchyLevelValue> iterator() {
        return this.values.iterator();
    }

    @Override
    public String toString() {
        return String.valueOf(name);
    }
}
