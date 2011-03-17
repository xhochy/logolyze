package de.logotakt.logolyze.model.olap;

import de.logotakt.logolyze.model.interfaces.IMeasure;
import de.logotakt.logolyze.model.interfaces.IMeasureSet;
import de.logotakt.logolyze.model.interfaces.MeasureClass;

/**
 *
 * Represents a numerical value or a string value, that will be used to
 * annotate a node or edge.
 *
 * This class is an abstract one, it will be implemented by StringMeasure
 * and DoubleMeasure.
 */
public abstract class Measure implements
		IMeasure {

	private MeasureSet set;

	@Override
	public abstract Double getNumber();

	@Override
	public IMeasureSet getSet() {
		return this.set;
	}

	@Override
	public abstract String getText();

	/**
	 * This sets the MeasureSet that this Measure belongs to. See MeasureSet for
	 * further documentation.
	 *
	 * It also automatically adjusts the MeasureSet's min/max values in case of a
	 * numerical measure.
	 *
	 * This is protected because it should only be accessed from the constructor of
	 * classes extending this class.
	 *
	 * @param set The MeasureSet that this Measure should belong to
	 */
	protected void setSet(final MeasureSet set) {
		this.set = set;

		// If we are numerical, ensure the min/max values are correct!
		if (this.set.getType().getMeasureClass() == MeasureClass.NumeralMeasure) {
			if ((this.set.getMin() == null) || (this.getNumber() < this.set.getMin())) {
				this.set.setMinMax(this.getNumber(), this.set.getMax());
			}
			if ((this.set.getMax() == null) || (this.getNumber() > this.set.getMax())) {
				this.set.setMinMax(this.set.getMin(), this.getNumber());
			}
		}
	}
}
