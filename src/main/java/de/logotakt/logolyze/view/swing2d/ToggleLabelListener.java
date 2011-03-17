package de.logotakt.logolyze.view.swing2d;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import de.logotakt.logolyze.model.interfaces.IEdge;
import de.logotakt.logolyze.model.interfaces.INode;
import edu.uci.ics.jung.visualization.VisualizationViewer;

/**
 * This listener listens for events (i.e. menu clicks) that invoke a
 * change in whether showing labels is being forced for a graph or not.
 */
final class ToggleLabelListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent e) {
        GraphPanel gp;
        DisplayOptions options;

        JMenuItem menuItem = (JMenuItem) e.getSource();
        JPopupMenu menu = (JPopupMenu) menuItem.getParent();
        @SuppressWarnings("unchecked")
        VisualizationViewer<INode, IEdge> vv = (VisualizationViewer<INode, IEdge>) menu.getInvoker();
        gp = (GraphPanel) vv.getParent();

        options = gp.getOptions();
        if (!gp.getOptions().isLabelDisplayForced()) {
            options.setForceLabels(true);
        } else {
            options.setForceLabels(false);
        }
        gp.updateOptions(options);
    }
}
