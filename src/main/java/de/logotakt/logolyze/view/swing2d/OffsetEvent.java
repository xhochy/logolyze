package de.logotakt.logolyze.view.swing2d;

/**
 * This event is fired when an axis is moved to a new offset.
 */
public class OffsetEvent {
    private final double offset;

    /**
     * Creates a new OffsetEvent with a given offset.
     * @param newOffset The new offset of the axis that fires this event
     */
    public OffsetEvent(final double newOffset) {
        this.offset = newOffset;
    }

    /**
     * Returns the new offset.
     * @return the new offset
     */
    public double offset() {
        return this.offset;
    }
}
