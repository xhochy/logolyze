package de.logotakt.logolyze.model.interfaces;

import java.util.Collection;

/**
 * The main interface to the model of Logolyze. The whole model is hidden behind this interface, the controller
 * interacts only with this interface and the objects returned by it.
 */
public interface ILogolyzeModel {
    /**
     * Opens a new database connection, as specified by the connection-string.
     *
     * @param cstring A model-specific string specifying a database connection.
     * @param initDb A model-specific string describing how to initialize the database session. <code>null</code> if not
     *        needed.
     * @throws DbConnectFailedException If the connection failed.
     * @throws DbMalformedException If the database is malformed.
     * @throws BogusDbConnectionException If the database connection behaves in unexpected ways.
     */
    void openDbConnection(String cstring, String initDb)
            throws DbConnectFailedException, BogusDbConnectionException,
            DbMalformedException;

    /**
     * Handles a request for a set of graphs.
     *
     * @param request An object specifying the request
     * @return A collection of <code>OLAPGraph</code>-objects which satisfy the request.
     * @throws DbMalformedException In case the database structure does not comply with our
     *         expectations.
     * @throws BogusDbConnectionException In case the database connection behaves oddly.
     * @throws RequestValidationFailedException This exception is thrown if any of the validators could not validate the
     *         request
     */
    IResponse handleRequest(IRequest request)
            throws BogusDbConnectionException, DbMalformedException,
            RequestValidationFailedException;

    /**
     * Returns the <code>DbStructure</code>-object that represents the structure of the currently connected database.
     *
     * @return A <code>DbStructure</code>-object, if a valid database connection exists. <code>null</code> otherwise.
     */
    IDbStructure getDbStructure();

    /**
     * Returns the <code>ModelDataFactory</code> for this model.
     *
     * @return An object implementing the <code>ModelDataFactory</code>- interface
     */
    IModelDataFactory getDataFactory();

    /**
     * Terminates the model.
     */
    void shutdown();

    /**
     * Determines whether the model is currently connected.
     *
     * @return true If connected
     *         false otherwise
     */
    boolean isConnected();
}
