package de.logotakt.logolyze.view.swing2d;

import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import de.logotakt.logolyze.model.interfaces.IEdge;
import de.logotakt.logolyze.model.interfaces.INode;
import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractPopupGraphMousePlugin;

/**
 * Handles the popup menus shown on right mouse click.
 */
public class GraphPanelPopupMousePlugin extends AbstractPopupGraphMousePlugin {
    private JPopupMenu vertexPopup;
    private JPopupMenu edgePopup;
    private JPopupMenu elsePopup;

    /**
     * Create a new Instance of this plugin.
     */
    public GraphPanelPopupMousePlugin() {
        super(MouseEvent.BUTTON3_MASK);
        vertexPopup = new JPopupMenu();
        edgePopup = new JPopupMenu();
        elsePopup = new JPopupMenu();
    }

    /**
     * Add a menu entry to all available Items. This method creates the {@link JMenuItem} instances in its own as a
     * {@link JMenuItem} can only belong to one menu.
     * @param name The name displayed to the user.
     * @param listener The listener triggered on a click on this {@link JMenuItem}.
     */
    public void addGlobalMenuItem(final String name, final ActionListener listener) {
        vertexPopup.add(makeMenuItem(name, listener));
        edgePopup.add(makeMenuItem(name, listener));
        elsePopup.add(makeMenuItem(name, listener));
    }

    private JMenuItem makeMenuItem(final String name, final ActionListener listener) {
        JMenuItem item = new JMenuItem(name);
        item.addActionListener(listener);
        return item;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handlePopup(final MouseEvent e) {
        @SuppressWarnings("unchecked")
        final VisualizationViewer<INode, IEdge> vv = (VisualizationViewer<INode, IEdge>) e.getSource();
        Point2D p = e.getPoint();

        GraphElementAccessor<INode, IEdge> pickSupport = vv.getPickSupport();
        if (pickSupport != null) {
            final INode v = pickSupport.getVertex(vv.getGraphLayout(), p.getX(), p.getY());
            if (v != null) {
                vertexPopup.show(vv, e.getX(), e.getY());
            } else {
                final IEdge edge = pickSupport.getEdge(vv.getGraphLayout(), p.getX(), p.getY());
                if (edge != null) {
                    edgePopup.show(vv, e.getX(), e.getY());
                } else {
                    elsePopup.show(vv, e.getX(), e.getY());
                }
            }
        } else {
            elsePopup.show(vv, e.getX(), e.getY());
        }
    }
}
