package de.logotakt.logolyze.view.swing2d;

/**
 * These are the available types a measure which is attached to an edge could be displayed in the graph. Not all these
 * type are applicable for all measure types. Most of them could only be used with numerical measures.
 */
public enum EdgeMeasureDisplayType {
    /**
     * Do not display this measure.
     */
    none,
    /**
     * Only the toString() ouput as a label.
     */
    text,

    /**
     * Different numerical values will lead to different colours.
     */
    colour,

    /**
     * Starts from a dotted (···) line via (---) to a straight line.
     */
    strokeStyle,

    /**
     * Higher values lead to a thicker line.
     */
    strokeWidth,

    /**
     * Measure is only to be displayed in the tooltips.
     */
    tooltip
}
