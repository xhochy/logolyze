package de.logotakt.logolyze.model.olap;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.logotakt.logolyze.model.interfaces.IConstraint;
import de.logotakt.logolyze.model.interfaces.ICube;
import de.logotakt.logolyze.model.interfaces.IDimension;
import de.logotakt.logolyze.model.interfaces.IHierarchy;
import de.logotakt.logolyze.model.interfaces.IHierarchyLevel;
import de.logotakt.logolyze.model.interfaces.IHierarchyLevelValue;
import de.logotakt.logolyze.model.interfaces.IModelDataFactory;
import de.logotakt.logolyze.model.interfaces.IRequest;

/**
 * This is the factory that the controller can use to construct the various classes it needs to communicate with the
 * model.
 */
public class ModelDataFactory implements IModelDataFactory {

    private List<Validator> validators;

    /**
     * Creates a new ModelDataFactory for the given DbStructure.
     * @param dbStructure The DbStructure to use as a basis for objects created by the factory.
     */
    public ModelDataFactory(final DbStructure dbStructure) {
        this.validators = new LinkedList<Validator>();

        this.validators.add(new MetadataValidator());
        this.validators.add(new ColumnDuplicationValidator());
        this.validators.add(new NotNullValidator());
        this.validators.add(new ConflictingConstraintsValidator());
    }

    @Override
    public IRequest makeRequest(final ICube c) {
        return new Request((Cube) c, validators);
    }

    @Override
    public IConstraint makeConstraint(final IDimension d, final IHierarchy h, final IHierarchyLevel l,
            final Collection<IHierarchyLevelValue> values) {
        return new Constraint(d, h, l, values);
    }

}
