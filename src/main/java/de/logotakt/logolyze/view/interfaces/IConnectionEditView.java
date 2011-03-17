package de.logotakt.logolyze.view.interfaces;

/**
 * Interface to a view component to edit or create a database connection.
 */
public interface IConnectionEditView extends IErrorReporter {
    /**
     * Set the name of the current edited database connection.
     * @param name The name of the database connection
     */
    void setConnectionName(String name);

    /**
     * Set the connection string of the current edited database connection.
     * @param cs The connection string of the database connection
     */
    void setConnectionString(String cs);

    /**
     * Set the Initialization String containing additional information to set up a connection.
     * @param is The initialization String
     */
    void setInitializationString(String is);

    /**
     * Get the (new) name of the database connection.
     * @return The name of the database connection
     */
    String getConnectionName();

    /**
     * Get the (new/edited) connection string.
     * @return The connection string
     */
    String getConnectionString();

    /**
     * Get the (new/edited) initialization string.
     * @return The initialization string.
     */
    String getInitializationString();
}
