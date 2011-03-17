package de.logotakt.logolyze.model.olap;

import java.util.LinkedList;

import de.logotakt.logolyze.model.interfaces.IConstraint;
import de.logotakt.logolyze.model.interfaces.IHierarchyLevel;
import de.logotakt.logolyze.model.interfaces.RequestValidationFailedException;

/**
 * This validator tests whether a request contains multiple constraints in the
 * same HierarchyLevel. This is usually a logical error by the user.
 */
public class ConflictingConstraintsValidator implements Validator {

    @Override
    public void validate(final Request r) throws RequestValidationFailedException {
        LinkedList<IHierarchyLevel> hierarchyLevelList = new LinkedList<IHierarchyLevel>();

        for (IConstraint ic : (Iterable<IConstraint>) r) {
            Constraint c = (Constraint) ic;

            if (hierarchyLevelList.contains(c.getHierarchyLevel())) {
                throw new RequestValidationFailedException(
                        "This Request containts multiple constraints the same HierarchyLevel: "
                        + ic.getHierarchyLevel().getName());
            }

            hierarchyLevelList.add(c.getHierarchyLevel());
        }
    }
}
