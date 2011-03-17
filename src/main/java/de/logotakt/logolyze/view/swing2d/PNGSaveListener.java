package de.logotakt.logolyze.view.swing2d;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;

import de.logotakt.logolyze.model.interfaces.IEdge;
import de.logotakt.logolyze.model.interfaces.INode;
import edu.uci.ics.jung.visualization.VisualizationViewer;

/**
 * A listener that saves the graph from the associated {@link VisualizationViewer} as PNG.
 */
final class PNGSaveListener implements ActionListener {
    private static final Logger LOGGER = Logger.getLogger(PNGSaveListener.class);
    private final GraphGrid2D graphGrid2D;
    private final JFrame owner;

    PNGSaveListener(final GraphGrid2D graphGrid2D, final JFrame owner) {
        this.graphGrid2D = graphGrid2D;
        this.owner = owner;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        JFileChooser fc = new JFileChooser();
        int ret = fc.showSaveDialog(owner);
        if (ret == JFileChooser.APPROVE_OPTION) {
            try {
                JMenuItem menuItem = (JMenuItem) e.getSource();
                JPopupMenu menu = (JPopupMenu) menuItem.getParent();
                @SuppressWarnings("unchecked")
                VisualizationViewer<INode, IEdge> vv = (VisualizationViewer<INode, IEdge>) menu.getInvoker();
                BufferedImage img = new BufferedImage(vv.getWidth(), vv.getHeight(), BufferedImage.TYPE_INT_RGB);
                vv.paint(img.getGraphics());
                ImageIO.write(img, "png", fc.getSelectedFile());
            } catch (IOException e1) {
                this.graphGrid2D.displayError("Could not save the Graph.");
                LOGGER.error("Could not save the Graph.", e1);
            }
        }
    }
}
