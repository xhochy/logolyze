package de.logotakt.logolyze.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import de.logotakt.logolyze.model.config.ConfigManager;
import de.logotakt.logolyze.model.config.ConnectionConfig;
import de.logotakt.logolyze.model.interfaces.BogusDbConnectionException;
import de.logotakt.logolyze.model.interfaces.DbConnectFailedException;
import de.logotakt.logolyze.model.interfaces.DbMalformedException;
import de.logotakt.logolyze.model.interfaces.RequestValidationFailedException;
import de.logotakt.logolyze.model.interfaces.IConstraint;
import de.logotakt.logolyze.model.interfaces.ICube;
import de.logotakt.logolyze.model.interfaces.IDbStructure;
import de.logotakt.logolyze.model.interfaces.ILogolyzeModel;
import de.logotakt.logolyze.model.interfaces.IMeasureType;
import de.logotakt.logolyze.model.interfaces.IRequest;
import de.logotakt.logolyze.model.interfaces.IResponse;
import de.logotakt.logolyze.view.interfaces.EventArgs;
import de.logotakt.logolyze.view.interfaces.EventType;
import de.logotakt.logolyze.view.interfaces.IErrorReporter;
import de.logotakt.logolyze.view.interfaces.IEventHandler;
import de.logotakt.logolyze.view.interfaces.ILogolyzeView;
import de.logotakt.logolyze.view.interfaces.IViewState;

/**
 * This class is the controller component of the Logolyze program. Its main purposes are to initialize the whole system,
 * wire up the events and handle some of them itself.
 */
public class Controller implements IEventHandler {
    private static final Logger logger = Logger.getLogger(Controller.class);

    /* References to model and view */
    private ILogolyzeView myView;
    private ILogolyzeModel myModel;

    /* References to the sub-controllers */
    private ConnectionListController connListContr;
    private ConnectionEditController connEditContr;
    private AxisConfigurationController axisConfigContr;
    private HierarchyTreeController hierarchyTreeContr;
    private MeasureConfigurationController measureConfigContr;

    /* Other state held by the Controller. */

    // The list of last connections.
    private Collection<ConnectionConfig> lastConnections;
    // All constraints currently active, stored by the view from which they were added.
    private Map<Object, Collection<IConstraint>> constraintsByView;
    // All measures which are to be displayed.
    private Collection<IMeasureType> measures;
    // The cube which is currently active.
    private ICube cube;

    // While loading state, a lot of spurious graphs are requested from the view. This flag
    // disables them.
    private boolean suspendGraphs = false;

    /**
     * Constructs a new instance of the Controller class. This method takes an already initialized view and model and
     * initializes the event handling. Then, the saved database connections are pushed into the view.
     * @param v The ILogolyzeView this controller should use
     * @param m The LogolyzeModel this controller should use
     */
    public Controller(final ILogolyzeView v, final ILogolyzeModel m) {
        logger.debug("Controller starting up");

        myModel = m;
        myView = v;

        try {
            lastConnections = ConfigManager.getInstance().loadLastConnections();
        } catch (IOException ex) {
            myView.displayError("The list of last connections could not be loaded " + "for the following reason:\n"
                    + ex.getMessage());
            logger.error("Could not load last connections.", ex);

            lastConnections = new ArrayList<ConnectionConfig>();
        }

        constraintsByView = new HashMap<Object, Collection<IConstraint>>();
        measures = new ArrayList<IMeasureType>();
        cube = null;

        makeSubcontrollers();
        setupEvents();
        pushLastConnections();

        myView.initialize();

        logger.debug("Logolyze is initialized.");
    }

    /* Initializes all the subcontrollers. */
    private void makeSubcontrollers() {
        connListContr = new ConnectionListController(this);
        connEditContr = new ConnectionEditController(this);
        axisConfigContr = new AxisConfigurationController(this);
        hierarchyTreeContr = new HierarchyTreeController(this);
        measureConfigContr = new MeasureConfigurationController(this);
    }

    /* Hook up the view events. */
    private void setupEvents() {
        logger.debug("Subscribing to view events.");
        myView.addEventListener(connEditContr, EventType.dbConfigCreated);
        myView.addEventListener(connListContr, EventType.dbConfigSelected);
        myView.addEventListener(connListContr, EventType.connectionListShowing);
        // myView.addEventListener(connListContr, EventType.connectionListShown);
        myView.addEventListener(connEditContr, EventType.dbConfigChanging);
        myView.addEventListener(connEditContr, EventType.dbConfigChanged);
        myView.addEventListener(connListContr, EventType.dbConfigRemoved);
        myView.addEventListener(this, EventType.viewStateSaved);
        myView.addEventListener(this, EventType.viewStateLoad);
        myView.addEventListener(this, EventType.cubeSelected);
        myView.addEventListener(this, EventType.dbDisconnect);
        myView.addEventListener(axisConfigContr, EventType.axisConfigShowing);
        myView.addEventListener(axisConfigContr, EventType.axisConfigChanged);
        myView.addEventListener(axisConfigContr, EventType.axisConfigDone);
        myView.addEventListener(measureConfigContr, EventType.measuresChanging);
        myView.addEventListener(measureConfigContr, EventType.measuresChanged);
        myView.addEventListener(hierarchyTreeContr, EventType.treeLoad);
        myView.addEventListener(hierarchyTreeContr, EventType.treeNodeSelected);
        myView.addEventListener(this, EventType.shutdownTriggered);
    }

    /* Makes the view display the list of last connections. */
    void pushLastConnections() {
        List<String> connectionNames = new ArrayList<String>(lastConnections.size());

        for (ConnectionConfig cfg : lastConnections) {
            connectionNames.add(cfg.getName());
        }

        myView.setConnectionList(connectionNames);
    }

    /**
     * Dispatches the events this class can handle. Controller implements this from the IEventHandler interface. The
     * Controller class accepts the following events: viewStateSave, viewStateLoad, cubeSelected, dbDisconnect
     * shutdownTriggered.
     * @param e The arguments for the current event.
     */
    @Override
    public void event(final EventArgs e) {
        switch (e.getType()) {
        case viewStateSaved:
            handleStateSave(e);
            break;
        case viewStateLoad:
            handleStateLoad(e);
            break;
        case cubeSelected:
            handleCubeSelected(e);
            break;
        case dbDisconnect:
            handleDisconnect();
            break;
        case shutdownTriggered:
            handleShutdown(e);
            break;
        default:
            throw new IllegalArgumentException("Wrong event was sent to Controller.");
        }
    }

    /* Handler functions for the individual events. */

    // This event is fired when the view's state is to be saved.
    // The state is requested from the view and given to the ConfigManager to be stored.
    private void handleStateSave(final EventArgs e) {
        logger.debug("StateSave event received.");

        String path = (String) e.getDetails();
        IErrorReporter er = (IErrorReporter) e.getCaller();

        try {
            ConfigManager.getInstance().saveViewConfig(path, myView.getViewState());
        } catch (IOException ex) {
            er.displayError("The View state could not be saved because of the following reason:\n" + ex.getMessage());
            logger.warn("Could not save view state.", ex);
            logger.warn("\tPath: " + path);
        }
    }

    // This event is fired when the view's state is to be loaded.
    private void handleStateLoad(final EventArgs e) {
        logger.debug("StateLoad event received");

        String path = (String) e.getDetails();
        IErrorReporter er = (IErrorReporter) e.getCaller();

        if (!myModel.isConnected()) {
            er.displayError("Cannot load the view state without a database connection.");
        } else {
            try {
                suspendGraphs = true;
                myView.setViewState((IViewState) ConfigManager.getInstance().loadViewConfig(path));
                suspendGraphs = false;

                displayNewGraphs(er);
            } catch (IOException ex) {
                er.displayError("The View state could not be loaded because of the following reason:\n"
                        + ex.getMessage());
                logger.warn("Could not load view state:", ex);
                logger.warn("\tPath: " + path);
            }
        }
    }

    // This event is fired when a new cube is selected to work on.
    // Here, cube is set to the new cube and the view is told to reinitialize, since
    // a lot of data in the view is now invalid.
    private void handleCubeSelected(final EventArgs e) {
        logger.debug("CubeSelected event received");

        String cubeName = (String) e.getDetails();
        IDbStructure struc = myModel.getDbStructure();

        // When selecting a new cube, almost all state becomes invalid.
        this.clean();

        for (ICube c : (Iterable<ICube>) struc) {
            if (c.getName().equals(cubeName)) {
                logger.debug("Cube found, refreshing view.");
                this.cube = c;
                // Refresh the view to force the interface
                // to be reloaded.
                myView.getFresh();
                return;
            }
        }

        logger.error("Selected cube " + cubeName + " not found");
    }

    // This event is fired when the application shuts down.
    // Only the last connections are saved to their standard location.
    private void handleShutdown(final EventArgs e) {
        logger.debug("Shutdown event received");

        myModel.shutdown();
        myView.shutdown();
        try {
            ConfigManager.getInstance().saveLastConnections(lastConnections);
        } catch (IOException ex) {
            // The ConfigManager has already done a dump to stderr.
            // We can't do much more here except exit even earlier!
            logger.error("Could not save last connections.");
        }

        logger.debug("Logolyze is exiting.");
        System.exit(0);
    }

    /**
     * Makes the controller forget all its state and the state of all its subcontrollers. Note: This does not mean a
     * complete shutdown. The Controller must be usable afterwards.
     */
    void clean() {
        logger.debug("Controller cleaning itself.");

        connListContr.clean();
        connEditContr.clean();
        axisConfigContr.clean();
        hierarchyTreeContr.clean();
        measureConfigContr.clean();

        this.cube = null;
        this.constraintsByView = new HashMap<Object, Collection<IConstraint>>();
        this.measures = new ArrayList<IMeasureType>();
    }

    // This event is fired when a disconnection from the database has been requested
    // or ocurred unexpectedly.
    // All Controllers and the View are told to clear their current state in order to avoid
    // inconsistencies. Then, a new connection may be established.
    private void handleDisconnect() {
        logger.debug("Disconnect event received");

        if (!myModel.isConnected()) {
            return;
        }

        myModel.shutdown();
        myView.setConnected(false);

        clean();
    }

    /*
     * Methods provided for the sub-controllers. Note that these are package-internal.
     */

    // Returns the LogolyzeModel the controller talks to.
    ILogolyzeModel getModel() {
        return myModel;
    }

    // Returns the collection of last connections.
    Collection<ConnectionConfig> getLastConnections() {
        return lastConnections;
    }

    // Returns the collection of constraints which were added in the given view.
    Collection<IConstraint> getConstraintsForView(final Object view) {
        return constraintsByView.get(view);
    }

    // Returns the selected cube
    ICube getSelectedCube() {
        return cube;
    }

    // Stores a new collection of constraints for a given view. If the collection is null,
    // the entry is removed.
    void setConstraintsForView(final Object view, final Collection<IConstraint> constraints) {
        if (constraints == null) {
            constraintsByView.remove(view);
        } else {
            constraintsByView.put(view, constraints);
        }
    }

    // Returns the selected measures
    Collection<IMeasureType> getMeasures() {
        return Collections.unmodifiableCollection(measures);
    }

    // Sets the selected measures.
    void setMeasures(final Collection<? extends IMeasureType> newMeasures) {
        measures = new ArrayList<IMeasureType>(newMeasures);
    }

    // Adds a specific MeasureType to the selected measures.
    void addMeasureType(final IMeasureType type) {
        measures.add(type);
    }

    // Removes a specific MeasureType from the selected measures.
    void removeMeasureType(final IMeasureType type) {
        measures.remove(type);
    }

    // Tries to establish a database connection with the given connection configuration.
    // The argument er is needed because the main view might not actually be focused
    // and/or behind a modal dialog.
    void tryDbConnection(final ConnectionConfig cc, final IErrorReporter er) {
        logger.debug("Trying database connection.");

        try {
            if (myModel.isConnected()) {
                logger.debug("Shutting down old connection.");
                myModel.shutdown();
                myView.setConnected(false);
            }

            String initString = cc.getInitString();
            if (initString == null || initString.trim().equals("")) {
                initString = null;
            }

            long startTime = System.currentTimeMillis();
            myModel.openDbConnection(cc.getConnectionString(), initString);
            myView.setResponseTime("Connect took " + ((System.currentTimeMillis() - startTime) / 1000.0) + " seconds.");

            this.clean();

            List<String> cubeNames = new ArrayList<String>();
            for (ICube c : (Iterable<ICube>) myModel.getDbStructure()) {
                logger.debug("Adding cube " + c.getName());
                cubeNames.add(c.getName());
            }

            myView.setConnected(true);
            myView.setCubesList(cubeNames);

            logger.debug("Database connection set up successfully.");

        } catch (DbConnectFailedException ex) {
            er.displayError("Database connection failed:\n" + ex.getMessage());
            logger.error("Database connection failed", ex);
        } catch (BogusDbConnectionException ex) {
            handleDisconnect();
            er.displayError("Error in database connection:\n" + ex.getMessage());
            logger.error("Error in database connection:", ex);
        } catch (DbMalformedException ex) {
            er.displayError("Database metadata could not be parsed:\n" + ex.getMessage());
            logger.error("Database metadata could not be parsed", ex);
        }
    }

    // Executes a request for a new set of graphs with the model, using the constraints collected from
    // the different views and the selected measures. The resulting set of graphs is passed on to the view
    // to display.
    void displayNewGraphs(final IErrorReporter er) {
        if (suspendGraphs) {
            logger.debug("graph request dropped");
            return;
        }

        logger.debug("Requesting new graphs.");

        IRequest req = getModel().getDataFactory().makeRequest(cube);

        // Add Constraints
        for (Collection<IConstraint> cs : constraintsByView.values()) {
            for (IConstraint c : cs) {
                logger.debug("Adding constraint " + c);
                req.addConstraint(c);
            }
        }

        // Add measure types
        for (IMeasureType m : measures) {
            logger.debug("Adding measure type " + m);
            req.addMeasureType(m);
        }

        try {
            // Request the new Graphs and push them in.
            logger.debug("Sending request to model");
            IResponse response = myModel.handleRequest(req);

            logger.debug("Sending graphs to view");
            myView.setGraphs(response.getGraphs());
            myView.setResponseTime("Last request took " + (response.getRequestDuration() / 1000.0) + " seconds.");

        } catch (BogusDbConnectionException ex) {
            er.displayError("Database connection problem:\n" + ex.getMessage());
            logger.error("Database connection problem:", ex);
        } catch (DbMalformedException ex) {
            er.displayError("The Database was not in the expected format:\n" + ex.getMessage());
            logger.error("The Database was not in the expected format:", ex);
        } catch (RequestValidationFailedException ex) {
            er.displayError("The Request validation failed:\n" + ex.getMessage() + "\n"
                    + "This is probably because settings in the hierarchy tree and on the axes "
                    + "conflict with each other.");
            logger.error("The Request validation failed:\n" + ex.getMessage() + "\n"
                    + "This is probably because settings in the hierarchy tree and on the axes "
                    + "conflict with each other.", ex);
        }
    }
}
