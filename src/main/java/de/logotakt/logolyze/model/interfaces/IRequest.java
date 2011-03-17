package de.logotakt.logolyze.model.interfaces;

/**
 * Represents a request to the model.
 *
 * A <code>Request</code> contains constraints, which have to be
 * met by the graphs to be returned. It also specifies what measures
 * should be attached to the graphs.
 * A new <code>Request</code>-object can be created through the
 * <code>ModelDataFactory</code>.
 */
public interface IRequest extends Iterable<IConstraint> {
	/**
	 * @return The Cube to which this Request applies.
	 */
	ICube getCube();

	/**
	 * Adds a new constraint to the request.
	 *
	 * @param constraint The constraint to be added.
	 */
	void addConstraint(IConstraint constraint);

	/**
	 * Removes a constraint from the request.
	 *
	 * @param constraint The constraint to be removed.
	 */
	void removeConstraint(IConstraint constraint);

	/**
	 * Adds the request for a measure.
	 *
	 * @param mt A <code>MeasureType</code>-object specifying
	 *           the requested measure.
	 */
	void addMeasureType(IMeasureType mt);

	/**
	 * Removes a request for a measure.
	 *
	 * @param mt A <code>MeasureType</code>-object specifying
	 *           the measure to be removed.
	 */
	void removeMeasureType(IMeasureType mt);
}
