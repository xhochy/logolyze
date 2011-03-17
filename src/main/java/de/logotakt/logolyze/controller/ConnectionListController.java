package de.logotakt.logolyze.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Iterator;

import de.logotakt.logolyze.model.config.ConnectionConfig;
import de.logotakt.logolyze.view.interfaces.EventArgs;
import de.logotakt.logolyze.view.interfaces.IConnectionListView;
import de.logotakt.logolyze.view.interfaces.IErrorReporter;
import de.logotakt.logolyze.view.interfaces.IEventHandler;

/**
 * This is the sub-controller for managing a IConnectionListView. Its job is to handle the list of database connections
 * available to Logolyze. This controller handles the events dbConnectionSelected, connectionListShowing,
 * connectionListShown.
 */
public class ConnectionListController implements IEventHandler {
	// This controller's superior Controller.
	private Controller controller;

	/**
	 * Initializes a new instance of ConnectionListController.
	 *
	 * @param controller This controller's superior controller.
	 */
	public ConnectionListController(final Controller controller) {
		this.controller = controller;
	}

	/**
	 * Dispatches the events this class can handle. ConnectionListController implements this from the IEventHandler
	 * interface. The ConnectionListController class accepts the following events: dbConnectionSelected,
	 * connectionListShowing, connectionListShown.
	 * @param e The arguments for the current event.
	 */
	@Override
	public void event(final EventArgs e) {
		switch (e.getType()) {
		case dbConfigSelected:
			handleConfigSelected(e);
			break;
		case dbConfigRemoved:
			handleConfigRemoved(e);
			break;
		case connectionListShowing:
			handleListShowing(e);
			break;
		default:
			throw new IllegalArgumentException("Wrong event was sent to ConnectionListController.");
		}
	}

	/* Handler functions for the individual events. */

	// This event is fired when a connection configuration from the list has been selected to be used for a
	// connection to the database.
	// The right connection is retrieved, and the superior controller is requested to establish the
	// database connection.
	private void handleConfigSelected(final EventArgs e) {
		// IConnectionListView clv = (IConnectionListView) e.getDetails();
		// String selectedName = clv.getSelectedConnection();
		String selectedName = (String) e.getDetails();
	        IErrorReporter er = (IErrorReporter) e.getCaller();
		Collection<ConnectionConfig> lastConnections = controller.getLastConnections();

		for (ConnectionConfig cc : lastConnections) {
			if (cc.getName().equals(selectedName)) {
				controller.tryDbConnection(cc, er);
				return;
			}
		}

		er.displayError("The selected configuration does not exist.");
	}

	// This event is fired when a connection configuration is to be removed from the list.
	// The connection is then duly removed.
	private void handleConfigRemoved(final EventArgs e) {
		IConnectionListView cev = (IConnectionListView) e.getCaller();
		String name = (String) e.getDetails();
		Collection<ConnectionConfig> lastConnections = controller.getLastConnections();
		Iterator<ConnectionConfig> iter = lastConnections.iterator();

		while (iter.hasNext()) {
			ConnectionConfig cc = iter.next();
			if (cc.getName().equals(name)) {
				iter.remove();
				controller.pushLastConnections();
				return;
			}
		}

		// No connection found
		cev.displayError("The connection to be removed does not exist");
	}

	// This event is fired when the connection list is shown.
	// The connection list is populated with the names of all available connections.
	private void handleListShowing(final EventArgs e) {
		IConnectionListView clv = (IConnectionListView) e.getDetails();
		Collection<ConnectionConfig> lastConnections = controller.getLastConnections();
		List<String> connectionNames = new ArrayList<String>(lastConnections.size());

		for (ConnectionConfig cc : lastConnections) {
			connectionNames.add(cc.getName());
		}

		clv.setConnectionNames(connectionNames);
	}

	/**
	 * Make this controller forget all its state.
	 */
	void clean() { }
}
