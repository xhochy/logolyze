package de.logotakt.logolyze.view;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;
import static org.fest.swing.fixture.Containers.showInFrame;
import static org.junit.Assume.assumeTrue;

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.util.List;

import javax.swing.JMenuItem;

import org.fest.swing.core.GenericTypeMatcher;
import org.fest.swing.edt.FailOnThreadViolationRepaintManager;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.edt.GuiTask;
import org.fest.swing.finder.WindowFinder;
import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.logotakt.logolyze.model.interfaces.IOLAPGraph;
import de.logotakt.logolyze.view.interfaces.IAxisConfigurationView;
import de.logotakt.logolyze.view.swing2d.Axis;
import de.logotakt.logolyze.view.swing2d.DisplayOptions;
import de.logotakt.logolyze.view.swing2d.GraphGrid2D;
import de.logotakt.logolyze.view.swing2d.LayoutName;

/**
 * Test the functionality of a {@link GraphGrid2D}.
 */
public class GraphGrid2DTest {

    @Inject
    @Named("graphs")
    private List<IOLAPGraph> graphs;

    private FrameFixture frame;
    private GraphGrid2D grid;
    private IAxisConfigurationView xacDialog;
    private IAxisConfigurationView yacDialog;

    @Inject
    @Named("dimensions")
    private List<String> dimensions;
    @Inject
    @Named("hierarchies TIMES")
    private List<String> hierarchies;
    @Inject
    @Named("levels TIMES")
    private List<String> hierarchyLevelsTimes;
    @Inject
    @Named("levels TIMES_WEEKDAY")
    private List<String> hierarchyLevelsTimesWeekday;
    @Inject
    @Named("values TIMES")
    private List<String> valuesTimes;
    @Inject
    @Named("values TIMES_WEEKDAY")
    private List<String> valuesTimesWeekday;
    @Inject
    @Named("selected values TIMES")
    private List<String> selectedValuesTimes;
    @Inject
    @Named("selected values TIMES_WEEKDAY")
    private List<String> selectedValuesTimesWeekday;

    // TODO this should be injectable or movable to ViewTestModule?
    private DisplayOptions options;

    @BeforeClass
    public static void setUpOnce() {
        assumeTrue(!GraphicsEnvironment.isHeadless());

        FailOnThreadViolationRepaintManager.install();
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Guice.createInjector(new ViewTestModule()).injectMembers(this);

        options = DisplayOptions.getDefault();

        grid = GuiActionRunner.execute(new GuiQuery<GraphGrid2D>() {
            @Override
            protected GraphGrid2D executeInEDT() throws Throwable {
                return new GraphGrid2D(null, options);
            }
        });
        frame = showInFrame(grid);
        frame.robot.settings().delayBetweenEvents(200);
        frame.resizeHeightTo(600);
        frame.resizeWidthTo(800);

        xacDialog = field("xacDialog").ofType(IAxisConfigurationView.class).in(grid).get();
        yacDialog = field("yacDialog").ofType(IAxisConfigurationView.class).in(grid).get();
    }

    @After
    public void tearDown() {
        frame.cleanUp();
    }

    @Test(timeout = 30000)
    public void singleGraphDisplayFrame() {
        setGraphs();
        frame.robot.rightClick(grid.getGraphPanelList().get(0).get(0));
        frame.menuItem(new GenericTypeMatcher<JMenuItem>(JMenuItem.class) {
            protected boolean isMatching(final JMenuItem other) {
                return other.getText().contains("separate");
            }
        }).click();

        frame.robot.waitForIdle();
        FrameFixture singleDisplay = WindowFinder.findFrame("singleDisplay").using(frame.robot);
        singleDisplay.robot.moveMouse(singleDisplay.component());
        singleDisplay.robot.rotateMouseWheel(4);
        singleDisplay.robot.rotateMouseWheel(-8);
        singleDisplay.button("close").click();
    }

    @Test(timeout = 30000)
    public void configureAxis() {
        frame.panel("xAxis").click();
        GuiActionRunner.execute(new GuiTask() {
            @Override
            protected void executeInEDT() throws Throwable {
                xacDialog.setDimensions(dimensions);
                xacDialog.setSelectedDimension(TestConstants.SELECTED_DIMENSION);
                xacDialog.setHierarchies(hierarchies);
                xacDialog.setSelectedHierarchy(TestConstants.SELECTED_X_HIERARCHY);
                xacDialog.setHierarchyLevels(hierarchyLevelsTimes);
                xacDialog.setSelectedHierarchyLevel(TestConstants.SELECTED_X_HIERARCHYLEVEL);
                xacDialog.setValues(valuesTimes);
                xacDialog.setSelectedValues(selectedValuesTimes);
            }
        });
        frame.dialog("xAxisConfigurationDialog").button("updateButton").click();
        assertThat(field("xAxis").ofType(Axis.class).in(grid).get().getValues()).containsOnly(
                TestConstants.SELECTED_X_VALUE_1, TestConstants.SELECTED_X_VALUE_2, TestConstants.SELECTED_X_VALUE_3);

        frame.panel("yAxis").click();
        GuiActionRunner.execute(new GuiTask() {
            @Override
            protected void executeInEDT() throws Throwable {
                yacDialog.setDimensions(dimensions);
                yacDialog.setSelectedDimension(TestConstants.SELECTED_DIMENSION);
                yacDialog.setHierarchies(hierarchies);
                yacDialog.setSelectedHierarchy(TestConstants.SELECTED_Y_HIERARCHY);
                yacDialog.setHierarchyLevels(hierarchyLevelsTimesWeekday);
                yacDialog.setSelectedHierarchyLevel(TestConstants.SELECTED_Y_HIERARCHYLEVEL);
                yacDialog.setValues(valuesTimesWeekday);
                yacDialog.setSelectedValues(selectedValuesTimesWeekday);
            }
        });
        frame.dialog("yAxisConfigurationDialog").button("updateButton").click();
        assertThat(field("yAxis").ofType(Axis.class).in(grid).get().getValues()).containsOnly(
                TestConstants.SELECTED_Y_VALUE_1, TestConstants.SELECTED_Y_VALUE_2, TestConstants.SELECTED_Y_VALUE_3);
    }

    @Test(timeout = 30000)
    public void setGraphs() {
        // Push state
        configureAxis();

        GuiActionRunner.execute(new GuiTask() {
            @Override
            protected void executeInEDT() throws Throwable {
                grid.setGraphs(graphs);
            }
        });
    }

    @Test(timeout=30000)
    public void changeSettingsAndLayout() {
        GuiTask updateOptions = new GuiTask() {
            @Override
            protected void executeInEDT() throws Throwable {
                grid.updateGlobalOptions();
            }
        };
        setGraphs();

        options.setForceLabels(true);
        options.setNodeColor(new Color(255, 0, 255));
        options.setRemoveIsolated(true);
        options.setShowLegend(false);
        options.setLayout(LayoutName.TreeLayout);

        GuiActionRunner.execute(updateOptions);

        options.setLayout(LayoutName.FRLayout);

        GuiActionRunner.execute(updateOptions);
    }
}
