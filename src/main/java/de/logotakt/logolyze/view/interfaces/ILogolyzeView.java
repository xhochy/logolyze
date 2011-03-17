package de.logotakt.logolyze.view.interfaces;

import java.util.Collection;
import java.util.List;

import de.logotakt.logolyze.model.interfaces.IOLAPGraph;

/**
 * Main interaction interface of the view. Used to register events and push specific data.
 */
public interface ILogolyzeView extends IErrorReporter {
    /**
     * Added a listener to a given {@link EventType}.
     * @param l The lister for this event.
     * @param event The {@link EventType} this listener should be called on.
     */
    void addEventListener(IEventHandler l, EventType event);

    /**
     * Remove a listener from an event.
     * @param l The listener that should be removed.
     * @param event The {@link EventType} this listener was called on.
     */
    void removeEventListener(IEventHandler l, EventType event);

    /**
     * Display these new graphs in the view.
     * @param graphs The graphs that should now be shown.
     */
    void setGraphs(Collection<? extends IOLAPGraph> graphs);

    /**
     * Update the list of known database connections.
     * @param names The names of all known database connections.
     */
    void setConnectionList(List<String> names);

    /**
     * Load a saved {@link ViewState} back into the view.
     * @param state The loaded state.
     */
    void setViewState(IViewState state);

    /**
     * Get the current {@link ViewState} for serialization.
     * @return The current state of the view.
     */
    IViewState getViewState();

    /**
     * Update the list of available cubes.
     * @param names The available cubes
     */
    void setCubesList(List<String> names);

    /**
     * Update the connected-state of the view.
     * @param connected Whether or not Logolyze is connected to a database;
     */
    void setConnected(boolean connected);

    /**
     * Further initialization which could not have been done in the constructor.
     */
    void initialize();

    /**
     * Perform a cleanup and do other tasks that need to be done before destruction.
     */
    void shutdown();

    /**
     * 
     * @param time
     */
    void setResponseTime(String time);

    /**
     * Gets the view in a fresh state.
     */
    void getFresh();
}
