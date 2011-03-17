package de.logotakt.logolyze.view.swing2d;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.List;

/**
 * Listener which cares about the scrolling of the Graphs.
 */
public class GraphScrollListener implements ComponentListener {
    private final GraphGrid2D graphGrid2D;

    /**
     * Creates a new GraphScrollListener belonging to a specific GraphGrid2D.
     * @param graphGrid2D The GraphGrid2D that this listener belongs to
     */
    public GraphScrollListener(final GraphGrid2D graphGrid2D) {
        this.graphGrid2D = graphGrid2D;
    }

    @Override
    public void componentShown(final ComponentEvent e) {
    }

    @Override
    public void componentResized(final ComponentEvent e) {
        // Check if we should/could resize anything.
        if (graphGrid2D.getGraphPanelList() == null || graphGrid2D.getGraphPanelList().size() == 0
                || this.graphGrid2D.getGraphPanelList().get(0).size() == 0) {
            return;
        }

        // Resize all graphPanels.
        for (List<GraphPanel> list : graphGrid2D.getGraphPanelList()) {
            for (GraphPanel graphPanel : list) {
                graphPanel.setSize(graphGrid2D.getGraphsFoundation().getWidth()
                        / graphGrid2D.getGraphPanelList().size(), graphGrid2D.getGraphsFoundation().getHeight()
                        / graphGrid2D.getGraphPanelList().get(0).size());
            }
        }
    }

    @Override
    public void componentMoved(final ComponentEvent e) {
    }

    @Override
    public void componentHidden(final ComponentEvent e) {
    }
}
