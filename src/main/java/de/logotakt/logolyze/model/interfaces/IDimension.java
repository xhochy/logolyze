package de.logotakt.logolyze.model.interfaces;

/**
 * This is the interface of the 'Dimension' hierarchy of the metadata tree. It exports anything that any class outside
 * the model should ever need of a dimension.
 *
 * It also implements the Iterable interface so that you can iterate over every
 * Hierarchy inside this dimension.
 */
public interface IDimension extends Iterable<IHierarchy> {

    /**
     * Returns the name of this dimension.
     *
     * @return The name of this dimension
     */
    String getName();

    /**
     * Returns the type (i.e. i/t dimension) of this dimension.
     *
     * @return The type of this dimension
     */
    DimensionType getType();

    /**
     * Returns a hierarchy inside this dimension, to be precise the one with this name.
     *
     * @param name The name of the hierarchy to be returned
     * @return A hierarchy with the given name, of null if there is no such hierarchy
     */
    IHierarchy getHierarchy(String name);
}
