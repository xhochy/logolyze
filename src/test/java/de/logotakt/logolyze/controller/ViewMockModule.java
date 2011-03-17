package de.logotakt.logolyze.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import de.logotakt.logolyze.view.interfaces.EventType;
import de.logotakt.logolyze.view.interfaces.IAxisConfigurationView;
import de.logotakt.logolyze.view.interfaces.IConnectionEditView;
import de.logotakt.logolyze.view.interfaces.IConnectionListView;
import de.logotakt.logolyze.view.interfaces.IEventHandler;
import de.logotakt.logolyze.view.interfaces.ILogolyzeView;

/**
 * Module that creates instances of mocks of the view.
 */
public class ViewMockModule extends AbstractModule {

    private final Map<EventType, Set<IEventHandler>> listeners = new HashMap<EventType, Set<IEventHandler>>();

    @Override
    protected void configure() {
        ILogolyzeView view = mock(ILogolyzeView.class);
        for (EventType type : EventType.values()) {
            listeners.put(type, new HashSet<IEventHandler>());
        }
        bind(new TypeLiteral<Map<EventType, Set<IEventHandler>>>() {
        }).annotatedWith(Names.named("listeners")).toInstance(listeners);
        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                listeners.get((EventType) arguments[1]).add((IEventHandler) arguments[0]);
                return null;
            }
        }).when(view).addEventListener(any(IEventHandler.class), any(EventType.class));
        bind(ILogolyzeView.class).annotatedWith(Names.named("logolyze view")).toInstance(view);

        IConnectionEditView connEditView = mock(IConnectionEditView.class);
        bind(IConnectionEditView.class).annotatedWith(Names.named("connection edit view")).toInstance(connEditView);
        IConnectionListView connListView = mock(IConnectionListView.class);
        bind(IConnectionListView.class).annotatedWith(Names.named("connection list view")).toInstance(connListView);
        IAxisConfigurationView axisConfigView = mock(IAxisConfigurationView.class);
        bind(IAxisConfigurationView.class).annotatedWith(Names.named("axis configuration view")).toInstance(
                axisConfigView);
    }

}
