package de.logotakt.logolyze.view.interfaces;

/**
 * Enum of event types triggered by the view.
 */
public enum EventType {
    /**
     * A database connection configuration was created. Details: {@link IConnectionEditView}.
     */
    dbConfigCreated,

    /**
     * A database connection was selected. Details: {@link IConnectionListView}
     */
    dbConfigSelected,

    /**
     * A view for displaying a list of connections is being shown. Details: {@link IConnectionListView}
     */
    connectionListShowing,

    /**
     * Edit of a connection started. The controller gets the selected item from the caller and uses that to fill in the
     * details. Caller: {@link IConnectionEditView} Details: The name of the edited connection.
     */
    dbConfigChanging,

    /**
     * A database connection was changed. Details: {@link IConnectionEditView}
     */
    dbConfigChanged,

    /**
     * A database connection was removed. Details: {@link IConnectionListView}
     */
    dbConfigRemoved,

    /**
     * The model should be disconnected. Details: none
     */
    dbDisconnect,

    /**
     * The view state should be saved. Details: String (the path to save to)
     */
    viewStateSaved,

    /**
     * The state should be loaded. Details: String (the path to load from)
     */
    viewStateLoad,

    /**
     * A cube was selected. Details: String (name of the cube)
     */
    cubeSelected,

    /**
     * Showing an {@link AxisConfigurationView}. Details: {@link AxisConfigurationView}
     */
    axisConfigShowing,

    /**
     * The configuration of an axis changed. Details: {@link AxisConfigurationView}
     */
    axisConfigChanged,

    /**
     * The configuration of an axis is done and a new Request should be generated. Details:
     * {@link IAxisConfigurationView}
     */
    axisConfigDone,

    /**
     * A tree should be loaded into the view. Details: {@link IHierarchyTreeView}
     */
    treeLoad,

    /**
     * A tree node was selected. Details: {@link IHierarchyTreeView}
     */
    treeNodeSelected,

    /**
     * A shutdown of the application/view was requested. Detail: {@link ILogolyzeView}
     */
    shutdownTriggered,

    /**
     * Showing a {@link MeasureConfigurationView} Details: {@link IMeasureConfigurationView}.
     */
    measuresChanging,

    /**
     * The measures for the Graph have changed. Details: {@link IMeasureConfigurationView}
     */
    measuresChanged
}
