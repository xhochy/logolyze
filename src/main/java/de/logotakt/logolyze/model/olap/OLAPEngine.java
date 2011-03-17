package de.logotakt.logolyze.model.olap;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import org.apache.log4j.Logger;

import de.logotakt.logolyze.model.interfaces.BogusDbConnectionException;
import de.logotakt.logolyze.model.interfaces.DbConnectFailedException;
import de.logotakt.logolyze.model.interfaces.DbMalformedException;
import de.logotakt.logolyze.model.interfaces.IDbStructure;
import de.logotakt.logolyze.model.interfaces.ILogolyzeModel;
import de.logotakt.logolyze.model.interfaces.IModelDataFactory;
import de.logotakt.logolyze.model.interfaces.IOLAPGraph;
import de.logotakt.logolyze.model.interfaces.IRequest;
import de.logotakt.logolyze.model.interfaces.IResponse;
import de.logotakt.logolyze.model.interfaces.RequestValidationFailedException;

/**
 * This is the central class of the model, implementing the ILogolyzeModel interface and thus being the facade to all
 * the model functions.
 */
public class OLAPEngine implements ILogolyzeModel {
    private static final Logger logger = Logger.getLogger(OLAPEngine.class);
    private Connection dbConnection = null;
    private ModelDataFactory modelDataFactory = null;
    private DbStructure dbStructure = null;
    private DataAbstraction dataAbstraction = null;
    private boolean connected = false;

    @Override
    public void openDbConnection(final String cstring, final String initDb) throws DbConnectFailedException,
            BogusDbConnectionException, DbMalformedException {
        if (connected) {
            this.shutdown();
        }

        try {
            dbConnection = DriverManager.getConnection(cstring);
        } catch (SQLException e) {
            throw new DbConnectFailedException("connect failed: " + e.getMessage(), e);
        }

        if ((initDb != null) && (initDb.length() > 0)) {
            Statement initStmt = null;
            try {
                initStmt = dbConnection.createStatement();
                initStmt.execute(initDb);
            } catch (SQLException e) {
                throw new DbConnectFailedException("init failed: " + e.getMessage(), e);
            } finally {
                if (initStmt != null) {
                    try {
                        initStmt.close();
                    } catch (SQLException e) {
                        logger.error("Statement could not be closed.", e);
                    }
                }
            }
        }

        dbStructure = MetadataParser.parseMetadata(dbConnection);
        dataAbstraction = new DataAbstraction(dbConnection);
        modelDataFactory = new ModelDataFactory(dbStructure);
        connected = true;
    }

    @Override
    public IResponse handleRequest(final IRequest request) throws BogusDbConnectionException, DbMalformedException,
            RequestValidationFailedException {
        Request req = (Request) request;

        if (!connected) {
            throw new IllegalStateException("The model is not connected to a database");
        }

        long startTime = System.currentTimeMillis();
        Collection<IOLAPGraph> resultGraphs = this.dataAbstraction.loadGraphs(req);

        return new Response(resultGraphs, System.currentTimeMillis() - startTime);
    }

    @Override
    public IDbStructure getDbStructure() {
        if (!connected) {
            throw new IllegalStateException("The model is not connected to a database");
        }

        return this.dbStructure;
    }

    @Override
    public IModelDataFactory getDataFactory() {
        if (!connected) {
            throw new IllegalStateException("The model is not connected to a database");
        }

        return this.modelDataFactory;
    }

    @Override
    public void shutdown() {
        if (!connected) {
            return;
        }

        /*
         * Close the database connection. We cannot sensibly handle an exception for close(), so we drop it.
         */
        try {
            dbConnection.close();
        } catch (Exception e) {
            logger.error("Could not close the database connection", e);
        } finally {
            connected = false;
        }
    }

    @Override
    public boolean isConnected() {
        return connected;
    }
}
