package de.logotakt.logolyze.view.swing2d;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import java.util.List;

import de.logotakt.logolyze.model.interfaces.IEdge;
import de.logotakt.logolyze.model.interfaces.INode;
import edu.uci.ics.jung.visualization.VisualizationViewer;

/**
 * This listener listens for events (i.e. menu clicks) that indicate that
 * a certain graph should be viewed in fullscreen (i.e. in an external window).
 */
final class FullscreenViewListener implements ActionListener {
	private final GraphGrid2D graphGrid2D;

	/**
	 * Creates a new FullscreenViewListener
	 * @param graphGrid2D The GraphGrid2D that this listener belongs to
	 */
	FullscreenViewListener(final GraphGrid2D graphGrid2D) {
		this.graphGrid2D = graphGrid2D;
	}

	private String makeSingleGraphTitle(final GraphPanel gp) {
		List<String> axisLabels = graphGrid2D.getPanelAxisLabels(gp);

		switch (axisLabels.size()) {
		case 0:
			return "Single Graph";
		case 1:
			return "Single Graph (" + axisLabels.get(0) + ")";
		case 2:
			return "Single Graph (" + axisLabels.get(0) + ", " + axisLabels.get(1) + ")";
		default:
			throw new IllegalArgumentException("Unexpectedly high number of axes present.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed(final ActionEvent e) {
		SingleGraphDisplayFrame f;
		GraphPanel gp;

		JMenuItem menuItem = (JMenuItem) e.getSource();
		JPopupMenu menu = (JPopupMenu) menuItem.getParent();
		@SuppressWarnings("unchecked")
			VisualizationViewer<INode, IEdge> vv = (VisualizationViewer<INode, IEdge>) menu.getInvoker();
		gp = (GraphPanel) vv.getParent();

		f = new SingleGraphDisplayFrame(this.graphGrid2D.getGppmp(),
						this.graphGrid2D.getMeasureDialog(),
						gp.getOptions(),
						makeSingleGraphTitle(gp));
		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		f.setGraph(gp.getGraph());
		f.setVisible(true);
		f.setExtendedState(f.getExtendedState() | JFrame.MAXIMIZED_BOTH);

	}
}
