package de.logotakt.logolyze.controller;

import java.util.Collection;

import de.logotakt.logolyze.model.config.ConnectionConfig;
import de.logotakt.logolyze.view.interfaces.EventArgs;
import de.logotakt.logolyze.view.interfaces.IConnectionEditView;
import de.logotakt.logolyze.view.interfaces.IErrorReporter;
import de.logotakt.logolyze.view.interfaces.IEventHandler;

/**
 * This is the sub-controller for managing a IConnectionEditView. Its job is to handle editing a single database
 * connection. This controller handles the events dbConnectionCreated, dbConfigChanging, dbConfigChanged,
 * dbConfigRemoved.
 */
public class ConnectionEditController implements IEventHandler {
	// This controller's superior Controller.
	private Controller controller;

	/**
	 * Initializes a new instance of ConnectionEditController.
	 *
	 * @param controller This controller's superior controller.
	 */
	public ConnectionEditController(final Controller controller) {
		this.controller = controller;
	}

	/**
	 * Dispatches the events this class can handle. ConnectionEditController implements this from the IEventHandler
	 * interface. The ConnectionEditController class accepts the following events: dbConnectionCreated,
	 * connectionChanging, connectionChanged, connectionRemoved
	 * @param e The arguments for the current event.
	 */
	@Override
	public void event(final EventArgs e) {
		switch (e.getType()) {
		case dbConfigCreated:
			handleConfigCreated(e);
			break;
		case dbConfigChanging:
			handleConfigChanging(e);
			break;
		case dbConfigChanged:
			handleConfigChanged(e);
			break;
		default:
			throw new IllegalArgumentException("Wrong event was sent to ConnectionEditController.");
		}
	}

	/* Handler functions for the individual events. */

	// This event is fired when a new connection has been spefcified using a ConnectionEditView.
	// A new connection configuration is created here with its data read from the dialog.
	private void handleConfigCreated(final EventArgs e) {
		IConnectionEditView cev = (IConnectionEditView) e.getDetails();
		Collection<ConnectionConfig> lastConnections = controller.getLastConnections();

		String name = cev.getConnectionName();
		String cs = cev.getConnectionString();
		String is = cev.getInitializationString();

		if (!sanityCheckConnection(name, cs, cev)) {
			return;
		}

		ConnectionConfig newConnection = new ConnectionConfig(cs, is, name);

		lastConnections.add(newConnection);
		controller.pushLastConnections();
	}

	// The connection that is currently being edited. We need to save this, since
	// the connection's name might be changed and there is no other way to retrieve
	// the connection after that.
	private ConnectionConfig editingConnection = null;

	// This event is fired when the user opens the connection editing dialog.
	// All slots are filled with data from the connection that is being edited.
	private void handleConfigChanging(final EventArgs e) {
		IConnectionEditView cev = (IConnectionEditView) e.getCaller();
		String name = (String) e.getDetails();
		Collection<ConnectionConfig> lastConnections = controller.getLastConnections();

		for (ConnectionConfig cc : lastConnections) {
			if (cc.getName().equals(name)) {
				cev.setConnectionName(name);
				cev.setConnectionString(cc.getConnectionString());
				cev.setInitializationString(cc.getInitString());
				editingConnection = cc;
				return;
			}
		}

		// No connection was found
		throw new IllegalArgumentException("The config to be changed does not exist.");
	}

	// This event is fired then the user is done editing the connection and the dialog closed.
	// The current connection configuration is updated here to reflect the changes made.
	private void handleConfigChanged(final EventArgs e) {
		IConnectionEditView cev = (IConnectionEditView) e.getDetails();
		if (editingConnection == null) {
			throw new IllegalStateException("Event configChanged was thrown before event configChanging.");
		}

		String name = cev.getConnectionName();
		String cs = cev.getConnectionString();
		String is = cev.getInitializationString();

		if (!sanityCheckConnection(name, cs, cev)) {
			return;
		}

		editingConnection.setName(name);
		editingConnection.setConnectionString(cs);
		editingConnection.setInitString(is);

		controller.pushLastConnections();

		// Editing is done. Forget this connection.
		// This is done to be sure that dbConfigChanging will be called first next time.
		editingConnection = null;
	}

	/* Helpers for the handler functions. */

	// Checks that the given name and connection string are nonempty and that the name is unique amongst all
	// connection configurations.
	private boolean sanityCheckConnection(final String name, final String cs, final IErrorReporter er) {
		// No name
		if (name.equals("")) {
			er.displayError("There was no name given for the connection.");
			return false;
		}
		// Duplicate name
		for (ConnectionConfig cc : controller.getLastConnections()) {
			if (name.equals(cc.getName()) && cc != editingConnection) {
				er.displayError("This connection name is already used for a different connection.");
				return false;
			}
		}
		// No connection string
		if (cs.equals("")) {
			er.displayError("There was no connection string given.");
			return false;
		}

		return true;
	}

	/**
	 * Make this controller forget all its state.
	 */
	void clean() { }
}
