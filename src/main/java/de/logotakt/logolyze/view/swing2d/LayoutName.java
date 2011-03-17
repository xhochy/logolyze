package de.logotakt.logolyze.view.swing2d;

/**
 * This enumeration identifies the different possible graph layouts. Please refer to JUNG's API documentation for
 * details.
 */
public enum LayoutName {
    /**
     * a self-organizing map layout algorithm, based on Meyer's self-organizing graph methods.
     */
    ISOMLayout,

    /**
     * a Fruchtermann-Reingold layout algorithm.
     */
    FRLayout,

    /**
     * A special, partially self-implemented algorithm that first builds a spanning tree and then layouts the sub-trees.
     * See SpanningTreeLayout documentation for details.
     */
    TreeLayout,

    /**
     * A simple layout that positions all vertices in a circle.
     */
    CircleLayout
}
