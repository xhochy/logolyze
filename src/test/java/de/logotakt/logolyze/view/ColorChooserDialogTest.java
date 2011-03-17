package de.logotakt.logolyze.view;

import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.times;

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.fest.swing.edt.FailOnThreadViolationRepaintManager;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.fixture.DialogFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.logotakt.logolyze.view.interfaces.IEventHandler;
import de.logotakt.logolyze.view.swing2d.ColorChooserDialog;

/**
 * Test the {@link ColorChooserDialogTest}.
 */
public class ColorChooserDialogTest {
    @Inject
    @Named("controller")
    private IEventHandler controller;

    private ColorChooserDialog colorDialog;
    private DialogFixture dialog;

    private class MyColorReceiver {
        Color clr;
        public void setColor(final Color col) {
            clr = col;
        }

        public Color getColor() {
            return clr;
        }
    }

    private class MyChangeListener implements ChangeListener {
        MyColorReceiver target;

        MyChangeListener(final MyColorReceiver tgt) {
            target = tgt;
        }

        @Override
        public void stateChanged(final ChangeEvent arg0) {
            target.setColor(((ColorChooserDialog) arg0.getSource()).getColor());
        }
    }

    @Mock
    MyColorReceiver colorReceiver;

    MyChangeListener listener;

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
        colorDialog = GuiActionRunner.execute(new GuiQuery<ColorChooserDialog>() {
            @Override
            protected ColorChooserDialog executeInEDT() throws Throwable {
                return new ColorChooserDialog(new Color(100, 200, 50));
            }
        });
        // We don't want any modality in unit tests.
        colorDialog.setModal(false);
        colorDialog.setVisible(true);

        dialog = new DialogFixture(colorDialog);
    }

    private void wireEvents() {
        listener = new MyChangeListener(colorReceiver);
        colorDialog.addChangeListener(listener);
    }

    /**
     * Clean up after each test.
     */
    @After
    public void tearDown() {
        // cover that, too :)
        colorDialog.removeChangeListneer(listener);

        dialog.cleanUp();
    }

    @Test
    public void changeColor() {
        dialog.slider("sliderR").slideTo(30);
        dialog.slider("sliderG").slideTo(100);
        dialog.slider("sliderB").slideTo(200);
        dialog.button("apply").click();
        dialog.slider("sliderR").slideTo(50);
        dialog.button("ok").click();

        ArgumentCaptor<Color> arg = ArgumentCaptor.forClass(Color.class);

        verify(colorReceiver, times(2)).setColor(arg.capture());

        List<Color> colors = arg.getAllValues();
        assert colors.get(0).equals(new Color(30, 100, 200));
        assert colors.get(1).equals(new Color(50, 100, 200));
    }

    @Test
    public void cancelChanges() {
        dialog.slider("sliderR").slideTo(10);
        dialog.slider("sliderR").slideTo(20);
        dialog.slider("sliderG").slideTo(20);
        dialog.slider("sliderG").slideTo(10);
        dialog.slider("sliderB").slideTo(30);
        dialog.slider("sliderB").slideTo(0);

        dialog.button("cancel").click();

        verifyNoMoreInteractions(colorReceiver);
    }
}
