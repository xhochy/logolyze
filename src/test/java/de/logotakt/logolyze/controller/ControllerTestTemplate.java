package de.logotakt.logolyze.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.logotakt.logolyze.model.config.ConfigManager;
import de.logotakt.logolyze.model.config.ConnectionConfig;
import de.logotakt.logolyze.model.interfaces.ILogolyzeModel;
import de.logotakt.logolyze.view.interfaces.EventArgs;
import de.logotakt.logolyze.view.interfaces.EventType;
import de.logotakt.logolyze.view.interfaces.IEventHandler;
import de.logotakt.logolyze.view.interfaces.ILogolyzeView;

/**
 * Provides methods used across all controller tests.
 */
public abstract class ControllerTestTemplate {

    private ConnectionConfig connectionConfig;
    private ConfigManager configManager;

    @Inject
    @Named("logolyze view")
    private ILogolyzeView view;
    @Inject
    @Named("listeners")
    private Map<EventType, Set<IEventHandler>> listeners;
    @Inject
    @Named("model")
    private ILogolyzeModel model;

    /**
     * Generic method to fire an event and call listeners.
     * @param caller The calling object.
     * @param type The type of event.
     * @param details The details supplied with this event.
     */
    protected void fireEvent(final Object caller, final EventType type, final Object details) {
        for (IEventHandler handler : listeners.get(type)) {
            handler.event(new EventArgs(caller, type, details));
        }
    }

    /**
     * Inject members before each test.
     * @throws IllegalAccessException Could not mock everything.
     * @throws NoSuchFieldException Could not mock everything.
     */
    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        Guice.createInjector(new ViewMockModule(), new ModelMockModule()).injectMembers(this);
        mockConfigModel();

        new Controller(view, model);
    }

    private void mockConfigModel() throws NoSuchFieldException, IllegalAccessException {
        connectionConfig = mock(ConnectionConfig.class);
        // ConnectionConfig
        when(connectionConfig.getConnectionString()).thenReturn(Constants.JDBC_TESTSTRING);
        when(connectionConfig.getName()).thenReturn(Constants.CONNECTION_SELECTED_NAME);

        // ConfigManager
        List<ConnectionConfig> configs = new ArrayList<ConnectionConfig>();
        configManager = mock(ConfigManager.class);
        configs.add(connectionConfig);
        try {
            when(configManager.loadLastConnections()).thenReturn(configs);
        } catch (IOException e) {
            // this will never happen, but leaving the catch block out will make eclipse a sad panda.
            e.printStackTrace();
        }

        // Mock into Singleton
        Field f = ConfigManager.class.getDeclaredField("instance");
        f.setAccessible(true);
        f.set(null, configManager);
    }

}
