package de.logotakt.logolyze.model.olap;

import de.logotakt.logolyze.model.interfaces.RequestValidationFailedException;

/**
 * This validator performs no validation.
 */
public class NOPValidator implements Validator {

    @Override
    public void validate(final Request r)
            throws RequestValidationFailedException {
    }
}
