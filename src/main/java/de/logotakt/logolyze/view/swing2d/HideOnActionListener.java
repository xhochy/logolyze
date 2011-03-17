package de.logotakt.logolyze.view.swing2d;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;

/**
 * Implements an {@link ActionListener} that hides a {@link JDialog} on an Action.
 */
class HideOnActionListener implements ActionListener {

    private final JDialog dialog;

    /**
     * Create a new instance connected to owner.
     * @param dialog The {@link JDialog} that should be hidden on an action.
     */
    HideOnActionListener(final JDialog dialog) {
        this.dialog = dialog;
    }

    /**
     * when an action is performed, hide the dialog.
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        dialog.setVisible(false);
    }

}
