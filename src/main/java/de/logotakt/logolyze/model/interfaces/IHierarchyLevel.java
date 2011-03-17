package de.logotakt.logolyze.model.interfaces;

import java.util.Collection;

/**
 * This exports all the methods available for a HierarchyLevel outside of the model. It also extends the Iterable
 * interface so that you can iterate over all the HierarchyLevelValues inside a certain HierarchyLevel.
 */
public interface IHierarchyLevel extends Iterable<IHierarchyLevelValue> {

    /**
     * Returns the name of this HierarchyLevel.
     * @return The name of this HierarchyLevel
     */
    String getName();

    /**
     * Returns a collection of all the values inside this HierarchyLevel.
     * @return A collection of all the values inside this HierarchyLevel
     */
    Collection<IHierarchyLevelValue> getValues();

    /**
     * Returns the parent HierarchyLevel to this level, nur null if this is the root HierarchyLevel inside its
     * Hierarchy.
     * @return The parent hierarchy level, or null if root
     */
    IHierarchyLevel parentLevel();

    /**
     * Returns the child HierarchyLevel of this HierarchyLevel, or null if there are no HierarchyLevels 'further down'.
     * Note that this returns only one HierarchyLevel, rather than a list, since HierarchyLevels form a list rather than
     * a tree.
     * @return The child HierarchyLevel, or null if there are none
     */
    IHierarchyLevel childLevel();
}
