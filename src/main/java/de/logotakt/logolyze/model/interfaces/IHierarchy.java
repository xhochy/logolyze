package de.logotakt.logolyze.model.interfaces;

/**
 * This interface exports all the methods available for a 'hierarchy' inside the metadata tree from outside the model.
 * It also implements the Iterable interface so that you can iterate over all the HierarchyLevels inside this Hierarchy.
 */
public interface IHierarchy extends Iterable<IHierarchyLevel> {

    /**
     * Returns the name of this Hierarchy.
     * @return The name of this Hierarchy.
     */
    String getName();

    /**
     * Returns one HierarchyLevel inside this Hierarchy, to be exact: the one with the name given.
     * @param name The name of the HierarchyLevel that should be returned
     * @return The HierarchyLevel with the given name, or null if no such level exists
     */
    IHierarchyLevel getLevel(String name);
}
