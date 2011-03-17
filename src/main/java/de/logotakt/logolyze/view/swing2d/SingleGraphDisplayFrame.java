package de.logotakt.logolyze.view.swing2d;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;

import de.logotakt.logolyze.model.interfaces.IOLAPGraph;

/**
 * This frame is for displaying only a single graph. It is being used as 'detailed view'
 * from the main view.
 */
@SuppressWarnings("serial")
public class SingleGraphDisplayFrame extends JFrame {
	private GraphPanel graphPanel;

	/**
	 * Create a new SingleGraphDisplayFrame.
	 * @param gppmp The GraphPanelPopupMousePlugin to use for the graph being displayed
	 * @param configDialog The MeasureConfigurationDialog describing which Measures
	 *        to display (and how) in the graph.
	 * @param options The DisplayOptions.
	 * @param title The window title to display.
	 */
	public SingleGraphDisplayFrame(final GraphPanelPopupMousePlugin gppmp,
				       final MeasureConfigurationDialog configDialog,
				       final DisplayOptions options,
				       final String title) {

		this.setPreferredSize(new Dimension(200, 200));
		this.setTitle(title);

		getContentPane().setLayout(new BorderLayout(0, 0));

		graphPanel = new GraphPanel(gppmp, configDialog, options);
		graphPanel.setVisible(true);
		getContentPane().add(graphPanel);

		JButton button = new JButton("Close");
		button.setName("close");
		getContentPane().add(button, BorderLayout.NORTH);

		button.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent arg0) {
					SingleGraphDisplayFrame.this.setVisible(false);
					SingleGraphDisplayFrame.this.dispose();
				}

			});

		setName("singleDisplay");

		pack();
	}

	/**
	 * Sets the graph that should be displayed by this SingleGraphDisplayFrame.
	 * @param graph The graph to be displayed
	 */
	public void setGraph(final IOLAPGraph graph) {
		this.graphPanel.setGraph(graph);
	}
}
