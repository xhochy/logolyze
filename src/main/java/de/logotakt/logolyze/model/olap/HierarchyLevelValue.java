package de.logotakt.logolyze.model.olap;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.logotakt.logolyze.model.interfaces.IHierarchyLevel;
import de.logotakt.logolyze.model.interfaces.IHierarchyLevelValue;

/**
 * This class represents a hierarchy level value, i.e. a value that is valid
 * for one of the hierarchy levels.
 *
 * Please note that these Values form a tree: days are children of months, which
 * in turn are children of quarters, which are children of years, ...
 */
public class HierarchyLevelValue implements
		IHierarchyLevelValue {

	private List<HierarchyLevelValue> children;
	private final String value;
	private final HierarchyLevel level;
	private final HierarchyLevelValue parent;

	@Override
	public Collection<? extends IHierarchyLevelValue> childValues() {
		return this.children;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public IHierarchyLevel getLevel() {
		return this.level;
	}

	@Override
	public IHierarchyLevelValue parentValue() {
		return this.parent;
	}

	/**
	 * Adds a child to the list of children in this HierarchyLevelValue.
	 *
	 * @param ch The child to be added.
	 */
	public void addChild(final HierarchyLevelValue ch) {
		this.children.add(ch);
	}

	/**
	 * Constructs a new HierarchyLevelValue.
	 *
	 * @param val The actual value
	 * @param parent The parent HierarchyLevelValue - i.e. "2009" for "November"
	 * @param lvl The HierarchyLevel that this HierarchyLevelValue belongs into
	 */
	HierarchyLevelValue(final String val, final HierarchyLevelValue parent, final HierarchyLevel lvl) {
		this.value = val;
		this.parent = parent;
		this.level = lvl;

		this.children = new LinkedList<HierarchyLevelValue>();
	}

	    @Override
	    public String toString() {
	        return String.valueOf(value);
	    }

}
