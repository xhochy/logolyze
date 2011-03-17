package de.logotakt.logolyze.view.swing2d;

import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import de.logotakt.logolyze.model.interfaces.IEdge;
import de.logotakt.logolyze.model.interfaces.INode;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.visualization.VisualizationViewer;

/**
 * Reacts to resizes of the parent component of a displayed graph (i.e. a GraphPanel)
 * and resizes the graph visualization to use all available space.
 */
final class GraphResizeListener implements ComponentListener {
    private final GraphPanel graphPanel;
    private final VisualizationViewer<INode, IEdge> bv;

    /**
     * Creates a new GraphResizeListener
     * @param graphPanel The GraphPanel that the graph is being displayed upon
     * @param bv The VisualizationViewer that is displaying the graph
     */
    GraphResizeListener(final GraphPanel graphPanel, final VisualizationViewer<INode, IEdge> bv) {
        this.graphPanel = graphPanel;
        this.bv = bv;
    }

    @Override
    public void componentShown(final ComponentEvent e) {
    }

    @Override
    public void componentResized(final ComponentEvent e) {
        react();
    }

    /**
     * Performs the task of resizing the VisualizationViewer to fill the whole GraphPanel
     */
    void react() {
        bv.getGraphLayout().setSize(new Dimension(this.graphPanel.getWidth() - 10, this.graphPanel.getHeight() - 10));
        bv.getGraphLayout().initialize();

        Relaxer relaxer = bv.getModel().getRelaxer();
        if (relaxer != null) {
            relaxer.stop();
            relaxer.prerelax();
            relaxer.relax();
        }

        Dimension size = graphPanel.getSize();
        size.width -= 2;
        size.height -= 2;
        bv.setPreferredSize(size);
        bv.setSize(size);

        // invalidate anything cached.
        graphPanel.invalidateCache();
    }

    @Override
    public void componentMoved(final ComponentEvent e) {
    }

    @Override
    public void componentHidden(final ComponentEvent e) {
    }
}
