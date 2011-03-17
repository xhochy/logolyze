package de.logotakt.logolyze.model.olap;


/**
 * This class implements one type of measure: A string measure.
 */
public class StringMeasure extends Measure {

	private String val;

	@Override
	public Double getNumber() {
		throw new UnsupportedOperationException("Trying to get number of a non-numeral measure");
	}

	@Override
	public String getText() {
		return this.val;
	}

	/**
	 * Creates a new string measure inside a given set, with a given value.
	 *
	 * @param set The set this measure should belong to
	 * @param val The value of this measure
	 */
	StringMeasure(final MeasureSet set, final String val) {
		this.val = val;
		this.setSet(set);
	}

}
