package de.logotakt.logolyze.controller;

import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.logotakt.logolyze.model.interfaces.ILogolyzeModel;
import de.logotakt.logolyze.view.interfaces.EventType;
import de.logotakt.logolyze.view.interfaces.IConnectionEditView;
import de.logotakt.logolyze.view.interfaces.IConnectionListView;
import de.logotakt.logolyze.view.interfaces.ILogolyzeView;

/**
 * Tests the controller on the db*-Events.
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class ControllerDbEventTest extends ControllerTestTemplate {

    @Inject
    @Named("connection edit view")
    private IConnectionEditView connEditView;
    @Inject
    @Named("connection list view")
    private IConnectionListView connListView;
    @Inject
    @Named("logolyze view")
    private ILogolyzeView view;
    @Inject
    @Named("model")
    private ILogolyzeModel model;

    /**
     * Tests the reaction of the Controller to the dbConnectionCreated event.
     */
    @Test
    public void dbConfigCreated() {
        when(connEditView.getConnectionString()).thenReturn(Constants.JDBC_TESTSTRING);
        when(connEditView.getConnectionName()).thenReturn(Constants.CONNECTION_CREATED_NAME);
        fireEvent(connEditView, EventType.dbConfigCreated, connEditView);
        verify(connEditView).getConnectionString();
        verify(connEditView).getConnectionName();
        verify(connEditView, never()).displayError(anyString());
    }

    /**
     * Tests the reaction of the Controller to the dbConnectionCreated event.
     */
    @Test
    public void dbConfigCreatedDuplicateName() {
        dbConfigCreated();

        when(connEditView.getConnectionString()).thenReturn(Constants.JDBC_TESTSTRING);
        when(connEditView.getConnectionName()).thenReturn(Constants.CONNECTION_CREATED_NAME);
        fireEvent(connEditView, EventType.dbConfigCreated, connEditView);
        verify(connEditView).displayError(anyString());
    }

    /**
     * Tests the reaction of the Controller to the dbConnectionCreated event.
     */
    @Test
    public void dbConfigCreatedNoName() {
        when(connEditView.getConnectionString()).thenReturn(Constants.JDBC_TESTSTRING);
        when(connEditView.getConnectionName()).thenReturn("");
        fireEvent(connEditView, EventType.dbConfigCreated, connEditView);
        verify(connEditView).displayError(anyString());
    }

    /**
     * Tests the reaction of the Controller to the dbConnectionCreated event.
     */
    @Test
    public void dbConfigCreatedNoConnectionString() {
        when(connEditView.getConnectionString()).thenReturn("");
        when(connEditView.getConnectionName()).thenReturn(Constants.CONNECTION_CREATED_NAME);
        fireEvent(connEditView, EventType.dbConfigCreated, connEditView);
        verify(connEditView).displayError(anyString());
    }

    /**
     * Tests the reaction of the Controller to the dbConnectionSelected event.
     */
    @Test
    public void dbConfigSelected() {
        when(connListView.getSelectedConnection()).thenReturn(Constants.CONNECTION_SELECTED_NAME);
        fireEvent(connListView, EventType.dbConfigSelected, Constants.CONNECTION_SELECTED_NAME);
        // Not needed if the String is pushed directly
        // verify(connListView).getSelectedConnection();
        verify(connListView, never()).displayError(anyString());
    }

    /**
     * Tests the reaction of the Controller to the dbConnectionSelected event.
     */
    @Test
    public void dbConfigSelectedNonExist() {
        when(connListView.getSelectedConnection()).thenReturn(Constants.CONNECTION_SELECTED_NAME + "dummy");
        fireEvent(connListView, EventType.dbConfigSelected, Constants.CONNECTION_SELECTED_NAME + "dummy");
        // Not needed if the String is pushed directly
        // verify(connListView).getSelectedConnection();
        verify(connListView).displayError(anyString());
    }

    /**
     * Tests the reaction of the Controller to the connectionListShowing event.
     */
    @Test
    public void connectionListShowing() {
        fireEvent(view, EventType.connectionListShowing, connListView);

        verify(connListView).setConnectionNames(anyCollectionOf(String.class));
        verify(connListView, never()).displayError(anyString());
    }

    /**
     * Tests the reaction of the Controller to the dbConfigChanging event.
     */
    @Test
    public void dbConfigChanging() {
        fireEvent(connEditView, EventType.dbConfigChanging, Constants.CONNECTION_SELECTED_NAME);
        verify(connEditView).setConnectionString(anyString());
        // Maybe we want to have this called too?
        // assertTrue(view.getConnEditView().isSetNameCalled());
        verify(connEditView, never()).displayError(anyString());
    }

    /**
     * Tests the reaction of the Controller to the dbConfigChanged event.
     */
    @Test
    public void dbConfigChanged() {
        // Initialize the controller with some state.
        dbConfigChanging();

        when(connEditView.getConnectionString()).thenReturn(Constants.JDBC_TESTSTRING);
        when(connEditView.getConnectionName()).thenReturn(Constants.CONNECTION_SELECTED_NAME);
        fireEvent(connEditView, EventType.dbConfigChanged, connEditView);
        verify(connEditView).getConnectionString();
        verify(connEditView).getConnectionName();
        verify(connEditView, never()).displayError(anyString());
    }

    /**
     * Tests the reaction of the Controller to the dbConfigRemoved event.
     */
    @Test
    public void dbConfigRemoved() {
        fireEvent(connListView, EventType.dbConfigRemoved, Constants.CONNECTION_SELECTED_NAME);
        verify(connListView, never()).displayError(anyString());
    }

    /**
     * Tests the reaction of the Controller to the dbConfigRemoved event.
     */
    @Test
    public void dbConfigRemovedNonExist() {
        fireEvent(connListView, EventType.dbConfigRemoved, Constants.CONNECTION_SELECTED_NAME + "dummy");
        verify(connListView).displayError(anyString());
    }

    /**
     * Test the reaction of the Controller to the dbDisconnect event.
     */
    @Test
    public void dbDisconnect() {
        fireEvent(null, EventType.dbDisconnect, view);
        dbConfigSelected();
        fireEvent(null, EventType.dbDisconnect, view);
        verify(view).setConnected(true);
        verify(view).setConnected(false);
        assert !model.isConnected();
    }
}
