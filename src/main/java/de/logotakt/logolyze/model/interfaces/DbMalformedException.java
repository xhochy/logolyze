package de.logotakt.logolyze.model.interfaces;

/**
 * This exception is thrown if the database we connected to is malformed in
 * any way - e.g. if a table, a relation, or an attribute that is supposed to
 * exist doesn't, etc.
 */
public class DbMalformedException extends Exception {
    private static final long serialVersionUID = 3597011365396604684L;

    /**
	 * Creates a new DbMalformedException.
	 * @param reason A guess at what went wrong.
	 */
	public DbMalformedException(final String reason) {
		super(reason);
	}

	/**
	 * Creates a new DbMalformedException, passing the causing exception along.
	 * @param reason A guess at what went wrong.
	 * @param cause The Exception that caused the problem.
	 */
	public DbMalformedException(final String reason, final Throwable cause) {
	    super(reason, cause);
	}
}
