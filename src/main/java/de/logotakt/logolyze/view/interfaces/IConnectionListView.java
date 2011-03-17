package de.logotakt.logolyze.view.interfaces;

import java.util.Collection;

/**
 * Interface, so that the controller could specify the list of available
 * database connections and get the selected database connection.
 */
public interface IConnectionListView extends IErrorReporter {
    /**
     * Update the list of available database connections.
     * @param names The available database connections
     */
    void setConnectionNames(Collection<String> names);

    /**
     * Get the name of the selected database connection.
     * @return The selected database connection
     */
    String getSelectedConnection();
}
