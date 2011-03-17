package de.logotakt.logolyze.model.interfaces;

/**
 * This is the interface of any measure objects. Measures are any values that nodes or edges can be annotated with.
 */
public interface IMeasure {

    /**
     * Returns the MeasuresSet this measure is part of. See MeasureSet documentation for details.
     *
     * @return The MeasureSet this measure is part of.
     */
    IMeasureSet getSet();

    /**
     * Returns a string representation of this measure, resp. its value. All measures have a string representation.
     *
     * @return The string representation of this measure
     */
    String getText();

    /**
     * Returns the numerical interpretation of this measure. Not all measures (or, precisely: not all measure types) can
     * be interpreted as a nuber. If this one can't, it will throw an UnsupportedOperationException. You can find out
     * whether this is supported or not via the Measures set, and its type.
     *
     * @return The numerical interpretation of this measure's value
     */
    Double getNumber();
}
