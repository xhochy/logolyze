package de.logotakt.logolyze.model.olap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.logotakt.logolyze.model.interfaces.DimensionType;
import de.logotakt.logolyze.model.interfaces.IDimension;
import de.logotakt.logolyze.model.interfaces.IHierarchy;

/**
 * This class represents one dimension inside one of the cubes described by the database's metadata.
 */
public class Dimension implements IDimension {

	private final String name;
	private final DimensionType type;
	private Map<String, IHierarchy> hierarchies;

	@Override
	public IHierarchy getHierarchy(final String name) {
		return this.hierarchies.get(name);
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public DimensionType getType() {
		return this.type;
	}

	@Override
	public Iterator<IHierarchy> iterator() {
		return this.hierarchies.values().iterator();
	}

	@Override
	public String toString() {
		return (this.type == DimensionType.iDimension ? "(I) " : "(T) ") + this.name;
	}

	/**
	 * Adds one hierarchy to this dimension.
	 * @param h The hierarchy to be added
	 */
	void addHierarchy(final IHierarchy h) {
		this.hierarchies.put(h.getName(), (Hierarchy) h);
	}

	/**
	 * Creates a new Dimension, with a given name and type.
	 * @param name The name for the new dimension
	 * @param type The new dimension's type
	 */
	Dimension(final String name, final DimensionType type) {
		this.name = name;
		this.type = type;

		this.hierarchies = new HashMap<String, IHierarchy>();
	}
}
