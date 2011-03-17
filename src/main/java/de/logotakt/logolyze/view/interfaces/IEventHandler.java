package de.logotakt.logolyze.view.interfaces;

/**
 * Generic interface for event handling components.
 */
public interface IEventHandler {

    /**
     * Trigger an event. This function should handle it.
     * @param e Details of the triggered event.
     */
    void event(EventArgs e);
}
