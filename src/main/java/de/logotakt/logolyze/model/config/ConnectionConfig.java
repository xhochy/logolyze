package de.logotakt.logolyze.model.config;

/**
 * This represents a stored database connection with its connection and initialization strings,
 * and a name for the user to remember the connection.
 */
public class ConnectionConfig {
	private String connectionString;
	private String initString;
	private String name;

	/**
	 * Creates a new instance of the ConnectionConfig class.
	 *
	 * @param cs The connection string of the new Connection
	 * @param is The initialization string of the new Connection
	 * @param name The name for the new connection.
	 */
	public ConnectionConfig(final String cs, final String is, final String name) {
		this.connectionString = cs;
		this.initString = is;
		this.name = name;
	}

	/**
	 * Sets the connection string to the given value.
	 *
	 * @param cs The new connection string.
	 */
	public void setConnectionString(final String cs) {
		this.connectionString = cs;
	}

	/**
	 * @return the connection string.
	 */
	public String getConnectionString() {
		return this.connectionString;
	}

	/**
	 * Sets the database initialization string to the given value.
	 *
	 * @param is The new initialization string.
	 */
	public void setInitString(final String is) {
		this.initString = is;
	}

	/**
	 * @return the connection string.
	 */
	public String getInitString() {
		return this.initString;
	}

	/**
	 * Sets the name for this connection to the given value.
	 *
	 * @param name The new name
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * @return the name.
	 */
	public String getName() {
		return name;
	}
}
