package de.logotakt.logolyze.view;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.util.List;

import org.fest.swing.core.ComponentLookupScope;
import org.fest.swing.edt.FailOnThreadViolationRepaintManager;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.fixture.DialogFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.logotakt.logolyze.view.interfaces.EventArgs;
import de.logotakt.logolyze.view.interfaces.EventType;
import de.logotakt.logolyze.view.interfaces.IAxisConfigurationView;
import de.logotakt.logolyze.view.interfaces.IEventHandler;
import de.logotakt.logolyze.view.swing2d.AxisConfigurationDialog;

/**
 * Tests the functionality of an {@link AxisConfigurationDialog}.
 */
public class AxisConfigurationDialogTest {

    private static final int CHANGED_SIMPLE_CHANGED_EVENTS = 6;
    private static final String VALUES_LIST = "valuesList";
    private static final String LEVEL_COMBO_BOX = "levelComboBox";
    private static final String HIERARCHY_COMBO_BOX = "hierarchyComboBox";
    private static final String DIMENSION_COMBO_BOX = "dimensionComboBox";

    private DialogFixture dialog;
    private AxisConfigurationDialog axisDialog;

    @Inject
    @Named("dimensions")
    private List<String> dimensions;
    @Inject
    @Named("hierarchies TIMES")
    private List<String> hierarchies;
    @Inject
    @Named("levels TIMES")
    private List<String> hierarchyLevel;
    @Inject
    @Named("values TIMES")
    private List<String> hierarchyLevelValues;
    @Inject
    @Named("selected values TIMES")
    private List<String> selectedValuesTimes;
    @Inject
    @Named("controller")
    private IEventHandler controller;

    @BeforeClass
    public static void setUpOnce() {
        assumeTrue(!GraphicsEnvironment.isHeadless());

        FailOnThreadViolationRepaintManager.install();
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Guice.createInjector(new ViewTestModule()).injectMembers(this);
        setUpDialog();
        mockController();

        dialog = new DialogFixture(axisDialog);
        dialog.robot.settings().componentLookupScope(ComponentLookupScope.ALL);
    }

    private void setUpDialog() {
        axisDialog = GuiActionRunner.execute(new GuiQuery<AxisConfigurationDialog>() {
            @Override
            protected AxisConfigurationDialog executeInEDT() throws Throwable {
                return new AxisConfigurationDialog(null);
            }
        });
        // We don't want any modality in unit tests.
        axisDialog.setModal(false);
    }

    private void mockController() {
        // Bind Controller to all events the dialog fires.
        axisDialog.addEventListener(controller, EventType.axisConfigChanged);
        axisDialog.addEventListener(controller, EventType.axisConfigDone);
        axisDialog.addEventListener(controller, EventType.axisConfigShowing);
    }

    @After
    public void tearDown() {
        dialog.cleanUp();
    }

    @Test
    public void showing() {
        dialog.show();
        verify(controller).event(EventArgsEventMatcher.any(EventType.axisConfigShowing));
    }

    @Test
    public void changedSimple() {
        // Push state!
        showing();

        dialog.comboBox(DIMENSION_COMBO_BOX).selectItem(TestConstants.SELECTED_DIMENSION);
        assertEquals(TestConstants.SELECTED_DIMENSION, axisDialog.getSelectedDimension());

        dialog.comboBox(HIERARCHY_COMBO_BOX).selectItem(TestConstants.SELECTED_X_HIERARCHY);
        assertEquals(TestConstants.SELECTED_X_HIERARCHY, axisDialog.getSelectedHierarchy());

        dialog.comboBox(LEVEL_COMBO_BOX).selectItem(TestConstants.SELECTED_X_HIERARCHYLEVEL);
        assertEquals(TestConstants.SELECTED_X_HIERARCHYLEVEL, axisDialog.getSelectedHierarchyLevel());

        dialog.list(VALUES_LIST).selectItems(TestConstants.SELECTED_X_VALUE_1, TestConstants.SELECTED_X_VALUE_2,
                TestConstants.SELECTED_X_VALUE_3);
        dialog.robot.waitForIdle();
        assertThat(axisDialog.getSelectedValues()).containsOnly(TestConstants.SELECTED_X_VALUE_1,
                TestConstants.SELECTED_X_VALUE_2, TestConstants.SELECTED_X_VALUE_3);
        verify(controller, times(CHANGED_SIMPLE_CHANGED_EVENTS)).event(
                EventArgsEventMatcher.any(EventType.axisConfigChanged));
    }

    @Test
    public void changedComplicated() {
        // Push state!
        changedSimple();

        dialog.comboBox(DIMENSION_COMBO_BOX).selectItem(TestConstants.OTHER_DIMENSION);
        assertEquals(TestConstants.OTHER_DIMENSION, axisDialog.getSelectedDimension());
        assertNull(axisDialog.getSelectedHierarchy());
        assertNull(axisDialog.getSelectedHierarchyLevel());
        assertThat(axisDialog.getSelectedValues()).hasSize(0);
        verify(controller, times(CHANGED_SIMPLE_CHANGED_EVENTS + 1)).event(
                EventArgsEventMatcher.any(EventType.axisConfigChanged));

        dialog.comboBox(DIMENSION_COMBO_BOX).selectItem(TestConstants.SELECTED_DIMENSION);
        dialog.comboBox(HIERARCHY_COMBO_BOX).selectItem(TestConstants.SELECTED_X_HIERARCHY);
        dialog.comboBox(LEVEL_COMBO_BOX).selectItem(TestConstants.SELECTED_X_HIERARCHYLEVEL);
        assertEquals(TestConstants.SELECTED_X_HIERARCHYLEVEL, axisDialog.getSelectedHierarchyLevel());
        dialog.comboBox(HIERARCHY_COMBO_BOX).selectItem(TestConstants.SELECTED_Y_HIERARCHY);
        assertEquals(TestConstants.SELECTED_Y_HIERARCHY, axisDialog.getSelectedHierarchy());
        assertNull(axisDialog.getSelectedHierarchyLevel());
        assertThat(axisDialog.getSelectedValues()).hasSize(0);
        verify(controller, times(CHANGED_SIMPLE_CHANGED_EVENTS + 1 + 4)).event(
                EventArgsEventMatcher.any(EventType.axisConfigChanged));
    }

    @Test
    public void done() {
        // Push state
        changedSimple();

        dialog.button("updateButton").click();
        verify(controller).event(EventArgsEventMatcher.any(EventType.axisConfigDone));
    }

    @Test
    public void setSelection() {
        // Handle axisConfigShowing
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable {
                EventArgs args = (EventArgs) invocation.getArguments()[0];
                IAxisConfigurationView view = (IAxisConfigurationView) args.getDetails();
                view.setDimensions(dimensions);
                view.setHierarchies(hierarchies);
                view.setHierarchyLevels(hierarchyLevel);
                view.setValues(hierarchyLevelValues);
                view.setSelectedDimension(TestConstants.SELECTED_DIMENSION);
                view.setSelectedHierarchy(TestConstants.SELECTED_X_HIERARCHY);
                view.setSelectedHierarchyLevel(TestConstants.SELECTED_X_HIERARCHYLEVEL);
                view.setSelectedValues(selectedValuesTimes);
                return null;
            }
        }).when(controller).event(EventArgsEventMatcher.any(EventType.axisConfigShowing));

        dialog.show();
        verify(controller).event(EventArgsEventMatcher.any(EventType.axisConfigShowing));
        verify(controller, never()).event(EventArgsEventMatcher.any(EventType.axisConfigChanged));
    }

    @Test
    public void displayError() {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                axisDialog.displayError("Error!");
            }
        });
        dialog.optionPane().requireErrorMessage().requireMessage("Error!");
    }
}
