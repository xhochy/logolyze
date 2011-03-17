package de.logotakt.logolyze.model.interfaces;

/**
 * This is the interface to a MeasureType object. These objects specify which type a certain measure is of, and which
 * measures are available in a cube.
 */
public interface IMeasureType {

    /**
     * Returns the class this measure type is in. See MeasureClass documentation for details.
     *
     * @return The measure class this measure type is in
     */
    MeasureClass getMeasureClass();

    /**
     * Returns the key of this measure type, i.e. the name of this measure within the metadata.
     *
     * @return The key of this measure type
     */
    String getKey();

    /**
     * Returns the measure association of this measure type. See MeasureAssociation documentation for details.
     *
     * @return The measure associaiton of this measure type.
     */
    MeasureAssociation getAssoc();
}
