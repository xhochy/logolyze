package de.logotakt.logolyze.model.olap;

import de.logotakt.logolyze.model.interfaces.IMeasureSet;
import de.logotakt.logolyze.model.interfaces.IMeasureType;
import de.logotakt.logolyze.model.interfaces.MeasureClass;

/**
 * A MeasureSet is a group of Measures, that are of the same type. This
 * class is used mainly to store min/max values of numerical measures, so that
 * the view can retrieve these values without having to walk over all the
 * measures.
 */
public class MeasureSet implements
		IMeasureSet {

	private Double min;
	private Double max;
	private final MeasureType type;

	/**
	 * Constructs a new, empty MeasureSet of a given type.
	 *
	 * @param type The type the measures inside this set will be of
	 */
	MeasureSet(final MeasureType type) {
		this.type = type;
	}

	@Override
	public Double getMax() {
		if (this.type.getMeasureClass() != MeasureClass.NumeralMeasure) {
			throw new UnsupportedOperationException("Trying to get number of a non-numeral measure");
		}

		return this.max;
	}

	@Override
	public Double getMin() {
		if (this.type.getMeasureClass() != MeasureClass.NumeralMeasure) {
			throw new UnsupportedOperationException("Trying to get number of a non-numeral measure");
		}

		return this.min;
	}

	@Override
	public IMeasureType getType() {
		return this.type;
	}

	/**
	 * This function sets the minimum and maximum values of the (numerical) measures inside this
	 * set. This function is mainly used by Measure.setSet().
	 *
	 * @param min The mininum value
	 * @param max The maximum value
	 */
	void setMinMax(final Double min, final Double max) {
		if (this.type.getMeasureClass() != MeasureClass.NumeralMeasure) {
			throw new UnsupportedOperationException("Trying to get number of a non-numeral measure");
		}

		this.min = min;
		this.max = max;
	}
}
