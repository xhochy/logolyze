package de.logotakt.logolyze.model.interfaces;

/**
 * This connection will be thrown by the OLAPengine if it fails to connect
 * to the database it should connect to.
 */
public class DbConnectFailedException extends Exception {
    private static final long serialVersionUID = -1653694882129755596L;

    /**
	 * Creates a new DbConnectFailedException.
	 * @param reason The reason why the connection failed.
	 */
	public DbConnectFailedException(final String reason) {
		super(reason);
	}

	/**
         * Creates a new DbConnectionFailedException, passing the causing exception along.
         * @param reason The reason why the connection failed.
         * @param cause The Exception that caused the problem.
         */
        public DbConnectFailedException(final String reason, final Throwable cause) {
            super(reason, cause);
        }
}
