package de.logotakt.logolyze.model.interfaces;

/**
 * This is the interface to a measure set. In a graph, all node/edge measures of the same key (i.e. that specify the
 * same type of values) are being combined in a set. With this set, it's possible to retrieve the maximum and minimum
 * values of the measures (of this kind) in the graph, if the measure has a numerical type.
 */
public interface IMeasureSet {

    /**
     * Returns the type of the measures within this set. See MeasureType documentation for details.
     *
     * @return The type of the measures in this set
     */
    IMeasureType getType();

    /**
     * Returns the maximum value of the measures in this set, if the measures are of a numerical type. If not, a
     * UnsupportedOperationException will be thrown.
     *
     * @return The maximum value of the measures in this set
     */
    Double getMax();

    /**
     * Returns the minimum value of the measures in this set, if the measures are of a numerical type. If not, a
     * UnsupportedOperationException will be thrown.
     *
     * @return The minimum value of the measures in this set
     */
    Double getMin();
}
