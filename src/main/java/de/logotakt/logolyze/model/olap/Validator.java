package de.logotakt.logolyze.model.olap;

import de.logotakt.logolyze.model.interfaces.RequestValidationFailedException;

/**
 * This is the interface that every Request-Validator has to implement.
 */
public interface Validator {
    /**
     * This function will be passed the Request, and it should return whether this validator considers this request to
     * be valid or not.
     *
     * @param r The request to be validated.
     * @throws RequestValidationFailedException This exception is thrown with an error message if this validators fails
     *         to validate the request
     */
    void validate(Request r) throws RequestValidationFailedException;
}
