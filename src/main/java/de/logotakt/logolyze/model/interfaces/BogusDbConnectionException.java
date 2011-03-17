package de.logotakt.logolyze.model.interfaces;

/**
 * This exception is thrown if at any time the database connection behaves in a way it is not supposed to, and the error
 * cannot be determined further.
 */
public class BogusDbConnectionException extends Exception {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = -2298792671287150077L;

    /**
     * Creates a new BogusDbConnectionException with a specified reason.
     * @param reason The reason for this exception
     */
    public BogusDbConnectionException(final String reason) {
        super(reason);
    }

    /**
     * Creates a new BogusDbConnectionException with a specified reason.
     * @param reason The reason for this exception
     * @param cause The cause for this exception
     */
    public BogusDbConnectionException(final String reason, final Throwable cause) {
        super(reason, cause);
    }
}
