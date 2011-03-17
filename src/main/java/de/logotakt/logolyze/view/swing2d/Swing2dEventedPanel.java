package de.logotakt.logolyze.view.swing2d;

import java.awt.EventQueue;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import de.logotakt.logolyze.view.interfaces.EventArgs;
import de.logotakt.logolyze.view.interfaces.EventType;
import de.logotakt.logolyze.view.interfaces.IErrorReporter;
import de.logotakt.logolyze.view.interfaces.IEventHandler;

/**
 * The Swing2dEventedPanel is the basis for JPanel subclasses that do event handling.
 */
public class Swing2dEventedPanel extends JPanel implements IErrorReporter {
    private static final long serialVersionUID = 701924364695709086L;
    private Map<EventType, Set<IEventHandler>> listeners;
    private Integer eventsSuspended = 0;

    protected Map<EventType, Set<IEventHandler>> getListeners() {
        return listeners;
    }

    /**
     * Create a new Instance.
     */
    public Swing2dEventedPanel() {
        super();
        listeners = new HashMap<EventType, Set<IEventHandler>>();
    }

    /**
     * Added a listener to a given {@link EventType}.
     * @param l The lister for this event.
     * @param event The {@link EventType} this listener should be called on.
     */
    public void addEventListener(final IEventHandler l, final EventType event) {
        if (listeners.containsKey(event)) {
            listeners.get(event).add(l);
        } else {
            throw new IllegalArgumentException(event.toString() + " is not triggered by this module.");
        }
    }

    /**
     * Remove a listener from an event.
     * @param l The listener that should be removed.
     * @param event The {@link EventType} this listener was called on.
     */
    public void removeEventListener(final IEventHandler l, final EventType event) {
        if (listeners.containsKey(event)) {
            listeners.get(event).remove(l);
        } else {
            throw new IllegalArgumentException(event.toString() + " is not triggered by this module.");
        }

    }

    /**
     * Trigger an event to all listeners.
     * @param event The triggered event type.
     */
    synchronized void fireEvent(final EventType event, final Object details) {

        // If events are suspended, ignore them.
        if (eventsSuspended > 0) {
            return;
        }

        forceEvent(event, details);
    }

    synchronized void forceEvent(final EventType event, final Object details) {
        EventArgs e = new EventArgs(this, event, details);
        for (IEventHandler handler : listeners.get(event)) {
            handler.event(e);
        }
    }

    /**
     * Suspend all events that may be fired by fireEvent.
     */
    protected synchronized void suspendEvents() {
            eventsSuspended++;
    }

    /**
     * Resume event handling. This method ensures that first pending waiting events are discard.
     */
    protected void resumeEvents() {
        Runnable resume = new Runnable() {
            @Override
            public void run() {
                synchronized (Swing2dEventedPanel.this) {
                    eventsSuspended--;
                }
            }
        };
        // Discards all pending events
        EventQueue.invokeLater(resume);
    }

    /**
     * Display an error for the User.
     * @param err The error to display.
     */
    public void displayError(final String err) {
        JOptionPane.showMessageDialog(this, err, err, JOptionPane.ERROR_MESSAGE);
    }
}
