package de.logotakt.logolyze.model.olap;

import java.util.Iterator;

import de.logotakt.logolyze.model.interfaces.IConstraint;
import de.logotakt.logolyze.model.interfaces.RequestValidationFailedException;

/**
 * This validator performs the task of validating whether a request conforms to the rules of the metadata.
 */
public class MetadataValidator implements Validator {

    @Override
    public void validate(final Request r)
            throws RequestValidationFailedException {
        /*
         * This will fail if: - any constraint does not have a Value below a Level that usually has values
         */

        Iterator<IConstraint> cit = r.iterator();
        while (cit.hasNext()) {
            Constraint c;

            c = (Constraint) cit.next();

            if (c.getValues().isEmpty()
                    && (((Hierarchy) c.getHierarchy()).getGraphColumnValue() != null)) {
                throw new RequestValidationFailedException("HierarchyLevel "
                        + c.getHierarchyLevel().getName()
                        + " expects values, but none given.");
            }

            if (!(c.getValues().isEmpty())
                    && (((Hierarchy) c.getHierarchy()).getGraphColumnValue() == null)) {
                throw new RequestValidationFailedException("HierarchyLevel "
                        + c.getHierarchyLevel().getName()
                        + " expects no values, but there are some in the request.");
            }
        }
    }
}
