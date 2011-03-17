package de.logotakt.logolyze.model.olap;

import java.util.Collection;

import de.logotakt.logolyze.model.interfaces.IConstraint;
import de.logotakt.logolyze.model.interfaces.IDimension;
import de.logotakt.logolyze.model.interfaces.IHierarchy;
import de.logotakt.logolyze.model.interfaces.IHierarchyLevel;
import de.logotakt.logolyze.model.interfaces.IHierarchyLevelValue;

/**
 * This class represents a contraint, i.e. a selection of the data inside the database. This is used to filter the
 * graphs returned in a request.
 */
public class Constraint implements IConstraint {

    private final IDimension dimension;
    private final IHierarchy hierarchy;
    private final IHierarchyLevel level;
    private final Collection<IHierarchyLevelValue> values;

    /**
     * Constructs a new constraint with the given parameters.
     * @param dimension The dimension that this constraint is in
     * @param hierarchy The hierarchy that this constraint is in
     * @param level The HierarchyLevel that this constraint is in
     * @param values The values that this constraint constrains to
     */
    Constraint(final IDimension dimension, final IHierarchy hierarchy, final IHierarchyLevel level,
            final Collection<IHierarchyLevelValue> values) {
        this.dimension = dimension;
        this.hierarchy = hierarchy;
        this.level = level;
        this.values = values;
    }

    @Override
    public IDimension getDimension() {
        return this.dimension;
    }

    @Override
    public IHierarchy getHierarchy() {
        return this.hierarchy;
    }

    @Override
    public IHierarchyLevel getHierarchyLevel() {
        return this.level;
    }

    @Override
    public Collection<IHierarchyLevelValue> getValues() {
        return this.values;
    }

}
