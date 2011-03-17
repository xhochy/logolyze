package de.logotakt.logolyze.view.swing2d;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;

import de.logotakt.logolyze.model.interfaces.IEdge;
import de.logotakt.logolyze.model.interfaces.INode;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.io.GraphMLWriter;
import edu.uci.ics.jung.visualization.VisualizationViewer;

/**
 * A listener that saves the graph that is embedded in the {@link VisualizationViewer} on which the user clicked.
 */
final class GraphMLSaveListener implements ActionListener {
    private static final Logger LOGGER = Logger.getLogger(GraphMLSaveListener.class);

    private final GraphGrid2D graphGrid2D;
    private final JFrame owner;

    GraphMLSaveListener(final GraphGrid2D graphGrid2D, final JFrame owner) {
        this.graphGrid2D = graphGrid2D;
        this.owner = owner;
    }

    /**
     * {@inheritDoc} Shows a {@link JFileChooser} to the user and saves the graph to the choosen file as GraphML.
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        JFileChooser fc = new JFileChooser();
        int ret = fc.showSaveDialog(owner);
        if (ret == JFileChooser.APPROVE_OPTION) {
            FileWriter writer = null;
            try {
                writer = new FileWriter(fc.getSelectedFile(), false);
                GraphMLWriter<INode, IEdge> graphWriter = new GraphMLWriter<INode, IEdge>();
                JMenuItem menuItem = (JMenuItem) e.getSource();
                JPopupMenu menu = (JPopupMenu) menuItem.getParent();
                @SuppressWarnings("unchecked")
                VisualizationViewer<INode, IEdge> vv = (VisualizationViewer<INode, IEdge>) menu.getInvoker();
                Hypergraph<INode, IEdge> graph = vv.getGraphLayout().getGraph();
                graphWriter.save(graph, writer);
            } catch (IOException e1) {
                graphGrid2D.displayError("Could not save the Graph.");
                LOGGER.error("Could not save the Graph.", e1);
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e1) {
                        graphGrid2D.displayError("Could not save the Graph.");
                        LOGGER.error("Could not save the Graph.", e1);
                    }
                }
            }
        }
    }
}
