package de.logotakt.logolyze.model.interfaces;

import java.util.Collection;

/**
 * Enables the controller to create model-specific objects.
 */
public interface IModelDataFactory {
    /**
     * Creates a new object that implements the <code>Request</code>-interface.
     * @param c The <code>Cube</code> this request will operate on.
     * @return A new request object
     */
    IRequest makeRequest(ICube c);

    /**
     * Creates a constraint for use with a <code>Request</code>.
     * @param d The <code>Dimension</code> to constrain on.
     * @param h The <code>Hierarchy</code> to constrain on.
     * @param hl The <code>HierarchyLevel</code> to constrain on.
     * @param values The values that should be allowed by the constraint.
     * @return A matching <code>Constraint</code>-object.
     */
    IConstraint makeConstraint(IDimension d, IHierarchy h, IHierarchyLevel hl, Collection<IHierarchyLevelValue> values);
}
