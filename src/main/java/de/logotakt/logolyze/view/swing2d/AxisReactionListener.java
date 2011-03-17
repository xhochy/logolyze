package de.logotakt.logolyze.view.swing2d;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A listener which reacts to clicks on a panel, shows the {@link AxisConfigurationDialog}.
 */
class AxisReactionListener extends MouseAdapter {
    private final AxisConfigurationDialog dialog;

    /**
     *  Create a new instance of this listener assiociated to an {@link AxisConfigurationDialog}.
     * @param dialog The associated dialog that will be shown on a mouse click.
     */
    public AxisReactionListener(final AxisConfigurationDialog dialog) {
        this.dialog = dialog;
    }

    /**
     * {@inheritDoc}
     * Make the associated {@link AxisConfigurationDialog} visible.
     */
    @Override
    public void mouseClicked(final MouseEvent e) {
        dialog.setVisible(true);
    }
}
