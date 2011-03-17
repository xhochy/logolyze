package de.logotakt.logolyze.view.swing2d;

/**
 * Hold the State for a MeasureConfigurationDialog.
 * @author s_paulss
 */
public class MeasureConfigurationDialogState implements IState {
    // TODO reflect the recent changes to MeasureConfigurationView with measure display types.

    private String[] selectedMeasures;
    private String[] measureDisplayTypes;

    /**
     * Get what Measures were selected.
     * @return the selected measures.
     */
    public String[] getSelectedMeasures() {
        return selectedMeasures;
    }

    /**
     * Get what display types are used for the measures.
     *
     * The measure display type indices correspond to the measure indices.
     * @return The display types to be used.
     */
    public String[] getMeasureDisplayTypes() {
        return measureDisplayTypes;
    }

    /**
     * Set what Measures are selected.
     * @param selectedMeasures The selected Measures.
     */
    public void setSelectedMeasures(final String[] selectedMeasures) {
        this.selectedMeasures = new String[selectedMeasures.length];
        System.arraycopy(selectedMeasures, 0, this.selectedMeasures, 0, selectedMeasures.length);
    }

    /**
     * Set what MeasureDisplayTypes are to be used.
     * @param measureDisplayTypes The measure display types to use.
     */
    public void setMeasureDisplayTypes(final String[] measureDisplayTypes) {
        this.measureDisplayTypes = new String[measureDisplayTypes.length];
        System.arraycopy(measureDisplayTypes, 0, this.measureDisplayTypes, 0, measureDisplayTypes.length);
    }
}
