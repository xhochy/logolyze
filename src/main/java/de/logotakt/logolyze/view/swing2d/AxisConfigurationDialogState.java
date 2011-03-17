package de.logotakt.logolyze.view.swing2d;

/**
 * This class keeps the state of an {@link AxisConfigurationDialog}.
 */
public class AxisConfigurationDialogState implements IState {
    private String selectedDimension;
    private String selectedHierarchy;
    private String selectedHierarchyLevel;
    private String[] selectedHierarchyValues;

    /**
     * Get the selected dimension.
     * @return The selected dimension.
     */
    public String getSelectedDimension() {
        return selectedDimension;
    }

    /**
     * Set the selected dimension.
     * @param selectedDimension The selected dimension.
     */
    public void setSelectedDimension(final String selectedDimension) {
        this.selectedDimension = selectedDimension;
    }

    /**
     * Get the selected hierarchy.
     * @return The selected hierarchy.
     */
    public String getSelectedHierarchy() {
        return selectedHierarchy;
    }

    /**
     * Set the selected hierarchy.
     * @param selectedHierarchy The selected hierarchy.
     */
    public void setSelectedHierarchy(final String selectedHierarchy) {
        this.selectedHierarchy = selectedHierarchy;
    }

    /**
     * Get the selected hierarchy level.
     * @return The selected hierarchy level.
     */
    public String getSelectedHierarchyLevel() {
        return selectedHierarchyLevel;
    }

    /**
     * Set the selected hierarchy level.
     * @param selectedHierarchyLevel The selected hierarchy level.
     */
    public void setSelectedHierarchyLevel(final String selectedHierarchyLevel) {
        this.selectedHierarchyLevel = selectedHierarchyLevel;
    }

    /**
     * Get the selected hierarchy values.
     * @return The selected hierarchy values.
     */
    public String[] getSelectedHierarchyValues() {
        return selectedHierarchyValues;
    }

    /**
     * Set the selected hierarchy values.
     * @param selectedHierarchyValues The selected hierarchy values.
     */
    public void setSelectedHierarchyValues(final String[] selectedHierarchyValues) {
        this.selectedHierarchyValues = new String[selectedHierarchyValues.length];
        System.arraycopy(selectedHierarchyValues, 0, this.selectedHierarchyValues, 0, selectedHierarchyValues.length);
    }

}
