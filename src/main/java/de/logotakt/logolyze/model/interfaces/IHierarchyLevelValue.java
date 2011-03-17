package de.logotakt.logolyze.model.interfaces;

import java.util.Collection;

/**
 * This is the interface to a HierarchyLevelValue object, i.e. an object representing a possible value inside a
 * HierarchyLevel.
 */
public interface IHierarchyLevelValue {
    /**
     * Returns the hierarchy level value represented by this object.
     *
     * @return The current hierarchy level value
     */
    String getValue();

    /**
     * Returns the child values of the current hierarchy level value.
     *
     * @return A collection of all the child values of the current value.
     */
    Collection<? extends IHierarchyLevelValue> childValues();

    /**
     * Returns the <code>HierarchyLevel</code> object this <code>HierarchyLevelValue</code> is valid for.
     *
     * @return The <code>HierarchyLevel</code> for which the current value is valid.
     */
    IHierarchyLevel getLevel();

    /**
     * Returns the parent value of the current HierarchyLevelValue or null, if it is the root of its HierarchyLevelValue
     * tree.
     *
     * @return The <code>HierarchyLevelValue</code> that is the parent of the current value, or null.
     */
    IHierarchyLevelValue parentValue();
}
