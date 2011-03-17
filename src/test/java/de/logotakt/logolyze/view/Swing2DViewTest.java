package de.logotakt.logolyze.view;

import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.util.List;

import javax.swing.JComponent;

import org.apache.batik.swing.JSVGCanvas;
import org.fest.swing.edt.FailOnThreadViolationRepaintManager;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.edt.GuiTask;
import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.logotakt.logolyze.view.interfaces.EventType;
import de.logotakt.logolyze.view.interfaces.IEventHandler;
import de.logotakt.logolyze.view.swing2d.Swing2DView;

/**
 * Test the functionality of a {@link Swing2DView}.
 */
public class Swing2DViewTest {

    @Inject
    @Named("database connection names")
    private List<String> connectionNames;

    @Inject
    @Named("controller")
    private IEventHandler controller;

    private Swing2DView view;
    private FrameFixture frame;

    /**
     * Assure that we only make thread safe calls to the GUI.
     */
    @BeforeClass
    public static void setUpOnce() {
        assumeTrue(!GraphicsEnvironment.isHeadless());
        FailOnThreadViolationRepaintManager.install();
    }

    /**
     * Setup the frame we are testing.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Guice.createInjector(new ViewTestModule()).injectMembers(this);
        view = GuiActionRunner.execute(new GuiQuery<Swing2DView>() {
            @Override
            protected Swing2DView executeInEDT() throws Throwable {
                return new Swing2DView();
            }
        });
        frame = new FrameFixture(view);
        frame.robot.settings().delayBetweenEvents(50);
        frame.show(new Dimension(800, 600));
    }

    /**
     * Test the handling of measures.
     */
    @Test
    public void changeMeasures() {
        view.addEventListener(controller, EventType.measuresChanging);
        view.addEventListener(controller, EventType.measuresChanged);

        selectCube();

        JComponent foo = GuiActionRunner.execute(new GuiQuery<JComponent>() {

            JComponent recursiveSearch(final JComponent thing) {
                for (Component cmp : thing.getComponents()) {
                    if (cmp instanceof JSVGCanvas) {
                        return (JComponent) cmp;
                    } else {
                        if (cmp instanceof JComponent) {
                            JComponent res = recursiveSearch((JComponent) cmp);
                            if (res != null) {
                                return res;
                            }
                        }
                    }
                }
                return null;
            }

            @Override
            protected JComponent executeInEDT() throws Throwable {
                for (Component cmp : view.getComponents()) {
                    if (cmp instanceof JComponent) {
                        JComponent res = recursiveSearch((JComponent) cmp);
                        if (res != null) {
                            return res;
                        }
                    }
                }
                return null;
            }
        });

        frame.robot.click(foo, new Point(foo.getWidth() / 2, foo.getHeight() / 2));

        frame.dialog("measureDialog").comboBox("Measure Measure 1").selectItem("colour");
        frame.dialog("measureDialog").comboBox("Measure Measure 2").selectItem("colour");
        frame.dialog("measureDialog").comboBox("Measure Measure 3").selectItem("text");
        frame.dialog("measureDialog").comboBox("Measure Measure 4").selectItem("text");

        frame.dialog("measureDialog").button("update").click();

        frame.robot.click(foo, new Point(foo.getWidth() / 2, foo.getHeight() / 2));

        frame.dialog("measureDialog").comboBox("Measure Measure 2").selectItem("tooltip");
        frame.dialog("measureDialog").button("cancel").click();

        verify(controller, times(2)).event(EventArgsEventMatcher.any(EventType.measuresChanging));
        verify(controller).event(EventArgsEventMatcher.any(EventType.measuresChanged));
    }

    /**
     * Test if we could connect to a database using the menu.
     */
    @Test
    public void connectToDbLastConnection() {
        view.addEventListener(controller, EventType.dbConfigSelected);
        GuiActionRunner.execute(new GuiTask() {
            @Override
            protected void executeInEDT() throws Throwable {
                view.setConnectionList(connectionNames);
            }
        });

        frame.menuItemWithPath("Database", "Last Connections", connectionNames.get(0)).click();
        verify(controller).event(EventArgsEventMatcher.any(EventType.dbConfigSelected));
    }

    /**
     * Test if we could connect to a database using the dialog.
     */
    @Test
    public void connectToDbDialog() {
        view.addEventListener(controller, EventType.dbConfigSelected);
        view.addEventListener(controller, EventType.dbConfigChanged);
        view.addEventListener(controller, EventType.dbConfigChanging);
        view.addEventListener(controller, EventType.dbConfigCreated);
        view.addEventListener(controller, EventType.connectionListShowing);
        frame.menuItemWithPath("Database", "Connect").click();

        frame.dialog("dbConfigDialog").button("newButton").click();
        frame.dialog("dbConfigDialog").dialog("editDialog").textBox("name").enterText("testcon");
        frame.dialog("dbConfigDialog").dialog("editDialog").textBox("string").enterText("jdbc:myDummy");
        frame.dialog("dbConfigDialog").dialog("editDialog").textBox("initString")
                .enterText("init string");
        frame.dialog("dbConfigDialog").button("update").click();
        frame.dialog("dbConfigDialog").list("connectionsList").selectItem(0);
        frame.dialog("dbConfigDialog").button("connectButton").click();

        verify(controller).event(EventArgsEventMatcher.any(EventType.dbConfigSelected));
    }

    /**
     * Test the selection of a cube.
     */
    @Test
    public void selectCube() {
        connectToDbLastConnection();
        view.addEventListener(controller, EventType.cubeSelected);

        frame.comboBox("cubesBox").selectItem("eins");
        verify(controller).event(EventArgsEventMatcher.any(EventType.cubeSelected));
    }

    /**
     * Test the configuration of the axes.
     */
    //@Test
    public void configureAxes() {
        selectCube();

        // TODO configure both axes.
    }

    /**
     * Test selecting constraints in the hierarchy tree.
     */
    @Test
    public void selectThingsFromTheHierarchy() {
        view.addEventListener(controller, EventType.treeLoad);
        view.addEventListener(controller, EventType.treeNodeSelected);

        selectCube();

        frame.tree().clickPath("bar/quux");
        frame.tree().clickPath("bar/baz");
        frame.tree().clickPath("bar/quux");

        verify(controller).event(EventArgsEventMatcher.any(EventType.treeLoad));
        verify(controller, times(3)).event(EventArgsEventMatcher.any(EventType.treeNodeSelected));
    }

    /**
     * Test the retrieval and reloading of a view state.
     */
    @Test(timeout = 20000)
    public void tryGeneratingAndSettingAState() {
        selectThingsFromTheHierarchy();
        GuiActionRunner.execute(new GuiTask() {

            @Override
            protected void executeInEDT() throws Throwable {
                view.setViewState(view.getViewState());
            }
        });
    }

    /**
     * Clean up everything.
     */
    @After
    public void tearDown() {
        assumeTrue(!GraphicsEnvironment.isHeadless());

        frame.cleanUp();
    }
}
