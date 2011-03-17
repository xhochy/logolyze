package de.logotakt.logolyze.model.interfaces;

/**
 * A MeasureAssociation specifies whether a certain type of measures belongs to nodes or edges.
 */
public enum MeasureAssociation {
	/**
	 * The measure is to be attached to a node.
	 */
	nodeMeasure,

	/**
	 * The measure is to be attached to an edge.
	 */
	edgeMeasure
}
