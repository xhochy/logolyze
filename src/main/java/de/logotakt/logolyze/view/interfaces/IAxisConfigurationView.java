package de.logotakt.logolyze.view.interfaces;

import java.util.List;

/**
 * Interface to communicate with view components that configure axis details.
 */
public interface IAxisConfigurationView extends IErrorReporter {
    /**
     * Set the list of available dimensions.
     * @param dim Available dimensions.
     */
    void setDimensions(List<String> dim);

    /**
     * Get the selected dimension.
     * @return The selected dimension
     */
    String getSelectedDimension();

    /**
     * Sets the selected dimension.
     * @param dimension Selected dimension
     */
    void setSelectedDimension(String dimension);

    /**
     * Set all available hierarchies.
     * @param h Available hierarchies
     */
    void setHierarchies(List<String> h);

    /**
     * Get the selected hierarchy.
     * @return The selected hierarchy.
     */
    String getSelectedHierarchy();

    /**
     * Set the selected hierarchy.
     * @param hierarchy Selected hierarchy
     */
    void setSelectedHierarchy(String hierarchy);

    /**
     * Set the available hierarchy levels.
     * @param l The available hierarchy levels.
     */
    void setHierarchyLevels(List<String> l);

    /**
     * Get the selected hierarchy level.
     * @return The selected hierarchy level
     */
    String getSelectedHierarchyLevel();

    /**
     * Sets the selected HierarchyLevel.
     * @param hierarchyLevel Selected hierarchy level
     */
    void setSelectedHierarchyLevel(final String hierarchyLevel);

    /**
     * Set the list of available hierarchy values.
     * @param v The available hierarchy values
     */
    void setValues(List<String> v);

    /**
     * Get the selected values.
     * @return The selected values.
     */
    List<String> getSelectedValues();

    /**
     * Set the selected values.
     * @param v The selected values
     */
    void setSelectedValues(List<String> v);
}
