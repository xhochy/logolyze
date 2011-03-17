package de.logotakt.logolyze.view;

import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.atLeast;

import java.awt.GraphicsEnvironment;
import java.util.List;

import org.fest.swing.edt.FailOnThreadViolationRepaintManager;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.fixture.DialogFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.logotakt.logolyze.view.interfaces.EventArgs;
import de.logotakt.logolyze.view.interfaces.EventType;
import de.logotakt.logolyze.view.interfaces.IEventHandler;
import de.logotakt.logolyze.view.swing2d.DbConfigDialog;

/**
 * Test the {@link DbConfigDialog}.
 */
public class DbConfigDialogTest {
    @Inject
    @Named("controller")
    private IEventHandler controller;

    @Captor
    private ArgumentCaptor<EventArgs> eventArgsCaptor;

    private DbConfigDialog configDialog;
    private DialogFixture dialog;

    /**
     * Assure that we only make thread safe calls to the GUI.
     */
    @BeforeClass
    public static void setUpOnce() {
        assumeTrue(!GraphicsEnvironment.isHeadless());

        FailOnThreadViolationRepaintManager.install();
    }

    /**
     * Setup the dialog we are testing here.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Guice.createInjector(new ViewTestModule()).injectMembers(this);
        buildDialog();
        wireEvents();
    }

    private void buildDialog() {
        configDialog = GuiActionRunner.execute(new GuiQuery<DbConfigDialog>() {
            @Override
            protected DbConfigDialog executeInEDT() throws Throwable {
                return new DbConfigDialog(null);
            }
        });
        // We don't want any modality in unit tests.
        configDialog.setModal(false);

        dialog = new DialogFixture(configDialog);
    }

    private void wireEvents() {
        configDialog.addEventListener(controller, EventType.connectionListShowing);
        configDialog.addEventListener(controller, EventType.dbConfigChanged);
        configDialog.addEventListener(controller, EventType.dbConfigChanging);
        configDialog.addEventListener(controller, EventType.dbConfigCreated);
        configDialog.addEventListener(controller, EventType.dbConfigRemoved);
    }

    /**
     * Clean up after each test.
     */
    @After
    public void tearDown() {
        // cover that, too :)
        configDialog.removeEventListener(controller, EventType.connectionListShowing);

        dialog.cleanUp();
    }

    private void inputDataForConnection(final String name, final String string, final String init) {
        dialog.dialog("editDialog").textBox("name").enterText(name);
        dialog.dialog("editDialog").textBox("string").enterText(string);
        dialog.dialog("editDialog").textBox("initString").enterText(init);
    }

    private void doConnection(final boolean doNew, final boolean accept, final String name, final String string,
            final String init) {
        if (doNew) {
            dialog.button("newButton").click();
        } else {
            dialog.list("connectionsList").selectItem(name);
            dialog.button("editButton").click();
            dialog.dialog("editDialog").textBox("name").deleteText();
            dialog.dialog("editDialog").textBox("string").deleteText();
            dialog.dialog("editDialog").textBox("initString").deleteText();
        }

        inputDataForConnection(name, string, init);

        if (accept) {
            dialog.button("update").click();
        } else {
            dialog.button("cancel").click();
        }
    }

    /**
     * Tests the whole life of a connection in the dialog.
     */
    @Test
    public void addChangeDeleteConnect() {
        configDialog.setVisible(true);

        dialog.button("connectButton").requireDisabled();
        dialog.button("editButton").requireDisabled();
        dialog.button("deleteButton").requireDisabled();
        dialog.button("newButton").requireEnabled();

        doConnection(true, true, "local", "odbc", "moo=foo");
        doConnection(true, true, "remote1", "mysql", "a=b+c");
        doConnection(false, false, "local", "a", "soso");
        doConnection(false, true, "local", "b", "so!");

        dialog.list("connectionsList").selectItem(0);
        dialog.button("deleteButton").click();
        dialog.list("connectionsList").selectItem(0);
        dialog.button("deleteButton").click();
        dialog.button("connectButton").requireDisabled();

        doConnection(true, true, "a", "b", "c");
        dialog.list("connectionsList").selectItem(0);
        dialog.button("connectButton").click();

        assert configDialog.getSelectedConnection().equals("a");

        /*EventType[] typeOrder = new EventType[] {
                EventType.dbConfigCreated,
                EventType.dbConfigCreated,
                EventType.dbConfigChanging,
                EventType.dbConfigChanging,
                EventType.dbConfigChanged,
                EventType.dbConfigRemoved,
                EventType.dbConfigRemoved,
                EventType.dbConfigCreated,
                EventType.dbConfigSelected
        };

        verify(controller, atLeast(typeOrder.length)).event(eventArgsCaptor.capture());

        List<EventArgs> args = eventArgsCaptor.getAllValues();

        // match as many connectionListShowing events as there are.
        EventArgs matchShowing = EventArgsEventMatcher.any(EventType.connectionListShowing);

        int eventIdx = 0;
        for (int typeIdx = 0; typeIdx < typeOrder.length; typeIdx++) {
            while (matchShowing.equals(args.get(eventIdx))) {
                eventIdx++;
            }
            EventArgs match = EventArgsEventMatcher.any(typeOrder[eventIdx]);
            assert match.equals(args.get(eventIdx));
            eventIdx++;
        }
        assert eventIdx == args.size();
        verifyNoMoreInteractions(controller);*/
    }

    @Test
    public void testCloseButtonToo() {
        configDialog.setVisible(true);
        dialog.button("closeButton").click();
    }
}
