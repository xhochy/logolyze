package de.logotakt.logolyze.view.swing2d;

/**
 * This is the interface to be implemented by any listener that wants to listen for changes in the scrolling of the
 * axis.
 */
public interface OffsetChangeListener {

    /**
     * This will be called when the axis this listener is registered to is moved.
     * @param e The OffsetEvent containing the new axis offset
     */
    void offsetChanged(OffsetEvent e);
}
