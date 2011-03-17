package de.logotakt.logolyze.model.olap;


/**
 * This class implements one type of measure: A numerical measure,
 * based on Doubles. This of course can also be used to handle integer
 * measures, as long as they don't get too big...
 */
public class DoubleMeasure extends Measure {

	private final Double val;

	/**
	 * Creates a new Double Measure inside a certain set,
	 * with a certain value.
	 *
	 * @param set The set this Measure will belong to
	 * @param val The value of this measure
	 */
	DoubleMeasure(final MeasureSet set, final Double val) {
		this.val = val;
		this.setSet(set);
	}

	@Override
	public Double getNumber() {
		return this.val;
	}

	@Override
	public String getText() {
		return String.valueOf(this.val);
	}

}
