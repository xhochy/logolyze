package de.logotakt.logolyze.view.swing2d;

/**
 * These are the available types a measure which is attached to a node could be displayed in the graph. Not all these
 * type are applicable for all measure types. Most of them could only be used with numerical measures.
 */
public enum NodeMeasureDisplayType {
    /**
     * Do not display this measure.
     */
    none,
    /**
     * Display as text.
     */
    text,
    /**
     * Display via the backgroud colour of a node.
     */
    colour,
    /**
     * Display via the size of a node.
     */
    size,
    /**
     * Display only in the tooltip.
     */
    tooltip
}
