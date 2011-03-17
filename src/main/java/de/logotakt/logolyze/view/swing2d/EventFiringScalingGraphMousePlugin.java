package de.logotakt.logolyze.view.swing2d;

import java.awt.event.MouseWheelEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;

/**
 * This class is a ScalingGraphMousePlugin that is augmented in a way that it fires
 * a ChangeEvent every time the zoom factor is adjusted, and that the current zoom
 * factor is retrievable.
 */
public class EventFiringScalingGraphMousePlugin extends ScalingGraphMousePlugin {

    private Double zoomFactor = 1.0;
    private List<ChangeListener> listener;

    /**
     * Create a new EventFiringScalingGraphMousePlugin. For documentation of the parameters, see
     * ScalingGraphMousePlugin documentation.
     * @param scaler see ScalingGraphMousePlugin documentation
     * @param modifiers see ScalingGraphMousePlugin documentation
     */
    public EventFiringScalingGraphMousePlugin(final ScalingControl scaler, final int modifiers) {
        super(scaler, modifiers);

        this.listener = new LinkedList<ChangeListener>();
    }

    /**
     * Create a new EventFiringScalingGraphMousePlugin. For documentation of the parameters, see
     * ScalingGraphMousePlugin documentation.
	 *
     * @param scaler see ScalingGraphMousePlugin documentation
     * @param modifiers see ScalingGraphMousePlugin documentation
     * @param in see ScalingGraphMousePlugin documentation
     * @param out see ScalingGraphMousePlugin documentation
     */
    public EventFiringScalingGraphMousePlugin(final ScalingControl scaler, final int modifiers, final float in,
            final float out) {
        super(scaler, modifiers, in, out);

        this.listener = new LinkedList<ChangeListener>();
    }

    /**
     * Adds a ChangeListener that will be notified if the zoom level changes.
     * @param l The ChangeListener to be added
     */
    public void addListener(final ChangeListener l) {
        this.listener.add(l);
    }

    /**
     * Removes a ChangeListener.
     * @param l The listener to be removed
     */
    public void removeListener(final ChangeListener l) {
        this.listener.remove(l);
    }

    @Override
    public void mouseWheelMoved(final MouseWheelEvent e) {
        ChangeEvent ce;

        super.mouseWheelMoved(e);

        if (e.getWheelRotation() > 0) { // > 0 is zooming in
            this.zoomFactor *= (e.getWheelRotation() * this.getIn());
        } else {
            this.zoomFactor *= (-1 * e.getWheelRotation() * this.getOut());
        }

        ce = new ChangeEvent(this);
        for (ChangeListener l : this.listener) {
            l.stateChanged(ce);
        }
    }

    /**
     * Returns the current zoom factor, with the factor at creation of this plugin.
     * being 1.0
     * @return The current zoom factor
     */
    public Double getZoom() {
        return this.zoomFactor;
    }

    /**
     * Set the zoom factor to use.
     * @param zoomLevel The new zoom factor.
     */
    public void setZoom(final Double zoomLevel) {
        this.zoomFactor = zoomLevel;
    }
}
