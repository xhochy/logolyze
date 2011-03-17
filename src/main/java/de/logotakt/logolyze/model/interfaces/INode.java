package de.logotakt.logolyze.model.interfaces;

import java.util.Map;

/**
 * Interface to the Logolyze-specific aspects of a node.
 *
 * Gives access to a label and the measures attached to
 * the node.
 */
public interface INode {
	/**
	 * Return the label of the node.
	 *
	 * @return The label of the node
	 */
	String getLabel();

	/**
	 * Return the measures attached to the node.
	 *
	 * @return A map from measure-name to value.
	 */
	Map<String, ? extends IMeasure> getMeasures();
}
