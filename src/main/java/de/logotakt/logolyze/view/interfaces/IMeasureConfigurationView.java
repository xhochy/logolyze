package de.logotakt.logolyze.view.interfaces;

import java.util.Collection;

import de.logotakt.logolyze.model.interfaces.IMeasureType;

/**
 * Interface to communicate with view components that handle measures.
 */
public interface IMeasureConfigurationView extends IErrorReporter {
    /**
     * Set the list of available measures.
     * @param m The available measures
     */
    void setMeasures(Collection<? extends IMeasureType> m);

    /**
     * Get all selected measures.
     * @return The selected measures.
     */
    Collection<? extends IMeasureType> getSelectedMeasures();

    /**
     * Set the selected Measures.
     * @param sels selected Measures to set.
     */
    void setSelectedMeasures(Collection<? extends IMeasureType> sels);
}
