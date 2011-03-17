package de.logotakt.logolyze.view;

import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;

import de.logotakt.logolyze.view.interfaces.EventArgs;
import de.logotakt.logolyze.view.interfaces.EventType;

/**
 * Matcher that could filter by EventType.
 */
public class EventArgsEventMatcher extends ArgumentMatcher<EventArgs> {

    private EventType type;

    /**
     * Match any EventArgs against the supplied EventType.
     * @param type The EventType to match against
     * @return A matcher that matches an EventArgs against its EventType.
     */
    public static EventArgs any(final EventType type) {
        return Matchers.argThat(new EventArgsEventMatcher(type));
    }

    /**
     * Instantiate an EventArgsEventMatcher to match against the supplied type.
     * @param type The EventType to match against
     */
    public EventArgsEventMatcher(final EventType type) {
        this.type = type;
    }

    @Override
    public boolean matches(final Object argument) {
        EventArgs args = (EventArgs) argument;
        return args.getType() == type;
    }

}
