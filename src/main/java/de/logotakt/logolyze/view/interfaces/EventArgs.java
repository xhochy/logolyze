package de.logotakt.logolyze.view.interfaces;

/**
 * Capsulates all needed data for event handling.
 */
public class EventArgs {
    private Object caller;

    private EventType type;

    private Object details;

    /**
     * Create a new instance with needed data.
     * @param c Reference to the event trigger
     * @param t Type of the event
     * @param d Details for handling this Event
     */
    public EventArgs(final Object c, final EventType t, final Object d) {
        caller = c;
        type = t;
        details = d;
    }

    /**
     * Getter for the Details.
     * @return details of an event
     */
    public Object getDetails() {
        return details;
    }

    /**
     * Getter for Caller.
     * @return trigger of the event
     */
    public Object getCaller() {
        return caller;
    }

    /**
     * Getter for type.
     * @return Type of the event
     */
    public EventType getType() {
        return type;
    }

}
