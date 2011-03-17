package de.logotakt.logolyze.model.interfaces;

import java.util.Collection;

/**
 * Represents a single constraint of an OLAP request.
 * A constraint stands for a filtering operation on the OLAP cube, narrowing
 * the results to a set of values on a certain HierarchyLevel. Note that in
 * one Request, there may be at most one Constraint for each Hierarchy.
 */
public interface IConstraint {
	/**
	 * @return The Dimension to which this Request applies.
	 */
	IDimension getDimension();

	/**
	 * @return The Hierarchy on which this request filters.
	 */
	IHierarchy getHierarchy();

	/**
	 * @return The HierarchyLevel on which this request filters.
	 */
	IHierarchyLevel getHierarchyLevel();

	/**
	 * @return The values this Request filters for.
	 */
	Collection<IHierarchyLevelValue> getValues();
}
