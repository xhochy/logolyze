package de.logotakt.logolyze.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.logotakt.logolyze.model.config.ConfigManager;
import de.logotakt.logolyze.model.config.ConnectionConfig;
import de.logotakt.logolyze.model.interfaces.DbMalformedException;
import de.logotakt.logolyze.model.interfaces.IDimension;
import de.logotakt.logolyze.model.interfaces.IHierarchy;
import de.logotakt.logolyze.model.interfaces.IHierarchyLevel;
import de.logotakt.logolyze.model.interfaces.IHierarchyLevelValue;
import de.logotakt.logolyze.model.interfaces.ILogolyzeModel;
import de.logotakt.logolyze.view.interfaces.EventType;
import de.logotakt.logolyze.view.interfaces.IHierarchyTreeView;
import de.logotakt.logolyze.view.interfaces.ILogolyzeView;
import de.logotakt.logolyze.view.interfaces.IMeasureConfigurationView;
import de.logotakt.logolyze.view.interfaces.IViewState;

/**
 * Tests the Controller class. In order to test everything properly, there needs to be an actual DbStructure mocked up
 * here. To help understanding, here is a sketch:
 *
 * <pre>
 * - TIMES
 * -- TIMES_WEEK
 * --- DAY
 * ---- 2010-05-12
 * ---- 2010-05-13
 * --- WEEK
 * ---- 2010-20
 * -- TIMES_WEEKDAY
 * --- WEEKDAY
 * ---- SUNDAY
 * - LOCATION
 * -- HUB
 * --- RAILPORT
 * ---- TOPOLOGY
 * </pre>
 */
public class ControllerTest extends ControllerTestTemplate {
    @Inject
    @Named("model")
    private ILogolyzeModel model;
    @Inject
    @Named("dimension TIMES")
    private IDimension dTim;
    @Inject
    @Named("hierarchy TIMES_WEEK")
    private IHierarchy hTimW;
    @Inject
    @Named("hierarchy TIMES_WEEKDAY")
    private IHierarchy hTimWd;
    @Inject
    @Named("hierarchylevel DAY")
    private IHierarchyLevel lDay;
    @Inject
    @Named("hierarchylevel WEEKDAY")
    private IHierarchyLevel lWd;
    @Inject
    @Named("hierarchylevel WEEK")
    private IHierarchyLevel lWeek;
    @Inject
    @Named("dimension LOCATION_TYPE")
    private IDimension dLoc;
    @Inject
    @Named("hierarchy HUB")
    private IHierarchy hHub;
    @Inject
    @Named("hierarchylevel RAIL")
    private IHierarchyLevel lRail;
    @Inject
    @Named("hlv 2010-05-12")
    private IHierarchyLevelValue v12;
    @Inject
    @Named("hlv 2010-05-13")
    private IHierarchyLevelValue v13;
    @Inject
    @Named("hlv 2010-20")
    private IHierarchyLevelValue v20;
    @Inject
    @Named("hlv SUNDAY")
    private IHierarchyLevelValue vSun;
    @Inject
    @Named("hlv TOPOLOGY")
    private IHierarchyLevelValue vTop;

    @Inject
    @Named("logolyze view")
    private ILogolyzeView view;

    @Mock
    private IHierarchyTreeView hierarchyTreeView;
    @Mock
    private ConnectionConfig connectionConfig;
    @Mock
    private ConfigManager configManager;
    @Mock
    private IMeasureConfigurationView measureConfView;

    private SecurityManager outerSecurityManager;

    /**
     * Setup a new Controller and a new mock for each test case. This is not really fast, but this is an easy way to
     * test with a fresh environment each time.
     * @throws NoSuchFieldException Reflection failed
     * @throws IllegalAccessException Reflection failed
     */
    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        Guice.createInjector(new ModelMockModule(), new ViewMockModule()).injectMembers(this);
        MockitoAnnotations.initMocks(this);
        mockConfigModel();

        new Controller(view, model);

        outerSecurityManager = System.getSecurityManager();
        SecurityManager noExitSecurityManager = new SecurityManager() {
            public void checkPermission(final Permission permission) {
                if (permission.getName().startsWith("exitVM")) {
                        throw new SecurityException("System.exit attempted and blocked.");
                }
            }
        };
        System.setSecurityManager(noExitSecurityManager);
    }

    /**
     * Resets the SecurityManager, so that exiting is allowed again.
     */
    @After
    public void cleanUp() {
        System.setSecurityManager(outerSecurityManager);
    }

    private void mockConfigModel() throws NoSuchFieldException, IllegalAccessException {
        // ConnectionConfig
        when(connectionConfig.getConnectionString()).thenReturn(Constants.JDBC_TESTSTRING);
        when(connectionConfig.getName()).thenReturn(Constants.CONNECTION_SELECTED_NAME);

        // ConfigManager
        List<ConnectionConfig> configs = new ArrayList<ConnectionConfig>();
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

    /**
     * Tests the reaction of the Controller to the viewStateSaved event.
     */
    @Test
    public void viewStateSaved() {
        when(view.getViewState()).thenReturn(new IViewState() {
        });
        fireEvent(view, EventType.viewStateSaved, Constants.VIEWSTATE_PATH);
        verify(view).getViewState();
        verify(view, never()).displayError(anyString());
    }

    /**
     * Tests the reaction of the Controller to the viewStateLoad event.
     */
    @Test
    public void viewStateLoad() {
	    when(model.isConnected()).thenReturn(true);
        fireEvent(view, EventType.viewStateLoad, Constants.VIEWSTATE_PATH);
        verify(view).setViewState(any(IViewState.class));
        verify(view, never()).displayError(anyString());
    }

    /**
     * Tests the reaction of the Controller to the cubeSelected event.
     */
    @Test
    public void cubeSelected() {
        fireEvent(view, EventType.cubeSelected, Constants.SELECTED_CUBE);
        verify(view, never()).displayError(anyString());
    }


    /**
     * Tests the reaction of the Controller to the treeLoad event.
     */
    @Test
    public void treeLoad() {
        // Push State
        cubeSelected();

        fireEvent(hierarchyTreeView, EventType.treeLoad, hierarchyTreeView);
        verify(hierarchyTreeView).clear();
        verify(hierarchyTreeView).addNode(null, dTim, false, false);
        verify(hierarchyTreeView).addNode(dTim, hTimW, false, false);
        verify(hierarchyTreeView).addNode(hTimW, lDay, false, false);
        verify(hierarchyTreeView).addNode(lDay, v12, true, false);
        verify(hierarchyTreeView).addNode(lDay, v13, true, false);
        verify(hierarchyTreeView).addNode(hTimW, lWeek, false, false);
        verify(hierarchyTreeView).addNode(lWeek, v20, true, false);
        verify(hierarchyTreeView).addNode(dTim, hTimWd, false, false);
        verify(hierarchyTreeView).addNode(hTimWd, lWd, false, false);
        verify(hierarchyTreeView).addNode(lWd, vSun, true, false);
        verify(hierarchyTreeView).addNode(null, dLoc, false, false);
        verify(hierarchyTreeView).addNode(dLoc, hHub, false, false);
        verify(hierarchyTreeView).addNode(hHub, lRail, true, false);
        verify(hierarchyTreeView, never()).addNode(lRail, vTop, true, false);
        verify(hierarchyTreeView, never()).displayError(anyString());
    }

    /**
     * Tests the reaction of the Controller to the treeNodeSelected event.
     */
    @Test
    public void treeNodeSelected() {
        // Push State
        cubeSelected();

        List<Object> selectedNodes = new ArrayList<Object>();
        selectedNodes.add(v12);
        when(hierarchyTreeView.getSelected()).thenReturn(selectedNodes);
        fireEvent(hierarchyTreeView, EventType.treeNodeSelected, hierarchyTreeView);
        verify(hierarchyTreeView, atLeastOnce()).getSelected();
        verify(hierarchyTreeView, never()).displayError(anyString());
    }

    /**
     * Tests the reaction of the Controller to the shutdownTriggered event.
     */
    @Test(expected = SecurityException.class)
    public void testShutdownTriggered() {
        fireEvent(null, EventType.shutdownTriggered, null);
    }

    /**
     * TODO Measure Tests.
     */
    @Test
    public void testMeasuresChanging() {
        // TODO use measures
        fireEvent(measureConfView, EventType.measuresChanging, measureConfView);
        // TODO Don't just do coverage, also do some assertions.
    }
}
