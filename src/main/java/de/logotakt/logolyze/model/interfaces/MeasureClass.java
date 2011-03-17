package de.logotakt.logolyze.model.interfaces;

/**
 * A MeasureClass specifies how a certain type of measures can be interpreted:
 * As a number, or just as text.
 */
public enum MeasureClass {
	/**
	 * The measure is numeral.
	 */
	NumeralMeasure,

	/**
	 * The measure is opaque text.
	 */
	OtherMeasure
}
