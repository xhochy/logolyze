package de.logotakt.logolyze.model.interfaces;

/**
 * This exception is thrown by the request Validators if the validation fails
 * for some reason.
 */
public class RequestValidationFailedException extends Exception {
    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 6435798341229365996L;

    /**
     * Creates a new RequestValidationFailedException.
     * @param reason The reason why the validation failed.
     */
    public RequestValidationFailedException(final String reason) {
        super(reason);
    }
}
