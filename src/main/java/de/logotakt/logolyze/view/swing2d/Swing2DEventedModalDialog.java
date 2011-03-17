package de.logotakt.logolyze.view.swing2d;

import java.awt.EventQueue;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import de.logotakt.logolyze.view.interfaces.EventArgs;
import de.logotakt.logolyze.view.interfaces.EventType;
import de.logotakt.logolyze.view.interfaces.IErrorReporter;
import de.logotakt.logolyze.view.interfaces.IEventHandler;

/**
 * Generic superclass for dialogs of the Swing2D view that use events.
 */
public class Swing2DEventedModalDialog extends JDialog implements IErrorReporter {

    private static final long serialVersionUID = -5564043729867190362L;

    private Map<EventType, Set<IEventHandler>> listeners;

    // TODO Use a semaphore.
    private Integer eventsSuspended = 0;

    private boolean errorDisplayed;

    protected Map<EventType, Set<IEventHandler>> getListeners() {
        return listeners;
    }

    /**
     * Creates a modal dialog that is associated with a {@link JFrame}.
     * @param owner The JFrame that is blocked while this dialog is shown.
     * @param title The title this dialog has while it is shown.
     */
    public Swing2DEventedModalDialog(final JFrame owner, final String title) {
        super(owner, title, true);
        listeners = new HashMap<EventType, Set<IEventHandler>>();
    }

    /**
     * Creates a modal dialog that is associated with a {@link JDialog}.
     * @param owner The JDialog that is blocked while this dialog is shown.
     * @param title The title this dialog has while it is shown.
     */
    public Swing2DEventedModalDialog(final JDialog owner, final String title) {
        super(owner, title, true);
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

    /**
     * Force an event to be triggered. This method should not be called via EventHandlers.
     * @param event The triggered event
     * @param details The submitted details
     */
    void forceEvent(final EventType event, final Object details) {
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
                synchronized (Swing2DEventedModalDialog.this) {
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
        errorDisplayed = true;
        JOptionPane.showMessageDialog(this, err, err, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Has there been a call to displayError?
     * @return True, if there was a call to displayError.
     */
    protected boolean hadError() {
        return errorDisplayed;
    }

    /**
     * Reset the monitoring of calls to displayError.
     */
    protected void resetErrorState() {
        errorDisplayed = false;
    }

}
