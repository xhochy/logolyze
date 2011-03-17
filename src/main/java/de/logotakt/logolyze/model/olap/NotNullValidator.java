package de.logotakt.logolyze.model.olap;

import de.logotakt.logolyze.model.interfaces.IConstraint;
import de.logotakt.logolyze.model.interfaces.RequestValidationFailedException;

/**
 * This validator performs no validation.
 */
public class NotNullValidator implements Validator {

    @Override
    public void validate(final Request r)
            throws RequestValidationFailedException {

	    if (r.getCube() == null) {
		    throw new RequestValidationFailedException(
				    "The request contains no cube");
	    }

	    for (IConstraint c : (Iterable<IConstraint>) r) {
		    if (c.getDimension() == null) {
			    throw new RequestValidationFailedException(
					    "The constraint refers to no Dimension");
		    }
		    if (c.getHierarchy() == null) {
			    throw new RequestValidationFailedException(
					    "The constraint refers to no Hierarchy");
		    }
		    if (c.getHierarchyLevel() == null) {
			    throw new RequestValidationFailedException(
					    "The constraint refers to no HierarchyLevel");
		    }
	    }
    }
}
