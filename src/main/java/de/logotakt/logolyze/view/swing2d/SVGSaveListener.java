package de.logotakt.logolyze.view.swing2d;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.log4j.Logger;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import de.logotakt.logolyze.model.interfaces.IEdge;
import de.logotakt.logolyze.model.interfaces.INode;
import edu.uci.ics.jung.visualization.VisualizationViewer;

/**
 * A listener that saves the graph from a {@link VisualizationViewer} as a SVG image.
 */
final class SVGSaveListener implements ActionListener {
    private static final Logger LOGGER = Logger.getLogger(SVGSaveListener.class);
    private final GraphGrid2D graphGrid2D;
    private final JFrame owner;

    SVGSaveListener(final GraphGrid2D graphGrid2D, final JFrame owner) {
        this.graphGrid2D = graphGrid2D;
        this.owner = owner;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        JFileChooser fc = new JFileChooser();
        int ret = fc.showSaveDialog(owner);
        if (ret == JFileChooser.APPROVE_OPTION) {
            // Get a DOMImplementation.
            DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

            // Create an instance of org.w3c.dom.Document.
            String svgNS = "http://www.w3.org/2000/svg";
            Document document = domImpl.createDocument(svgNS, "svg", null);

            // Create an instance of the SVG Generator.
            SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
            FileWriter writer = null;
            try {
                writer = new FileWriter(fc.getSelectedFile(), false);
                JMenuItem menuItem = (JMenuItem) e.getSource();
                JPopupMenu menu = (JPopupMenu) menuItem.getParent();
                @SuppressWarnings("unchecked")
                VisualizationViewer<INode, IEdge> vv = (VisualizationViewer<INode, IEdge>) menu.getInvoker();
                vv.paint(svgGenerator);
                svgGenerator.stream(writer);
            } catch (IOException e1) {
                this.graphGrid2D.displayError("Could not save the Graph.");
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
