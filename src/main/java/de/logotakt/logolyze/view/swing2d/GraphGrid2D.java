package de.logotakt.logolyze.view.swing2d;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import de.logotakt.logolyze.model.interfaces.DimensionType;
import de.logotakt.logolyze.model.interfaces.IConstraint;
import de.logotakt.logolyze.model.interfaces.IOLAPGraph;
import de.logotakt.logolyze.model.interfaces.IRequest;
import de.logotakt.logolyze.view.interfaces.EventArgs;
import de.logotakt.logolyze.view.interfaces.EventType;
import de.logotakt.logolyze.view.interfaces.IEventHandler;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * Displays graphs in a 2D grid including the x- and y-axis.
 */
public class GraphGrid2D extends Swing2dEventedPanel implements IStateful, IEventHandler {

    private static final long serialVersionUID = 2238772291567841033L;

    private static final Logger LOGGER = Logger.getLogger(GraphGrid2D.class);

    protected static final EventType[] TRIGGERED_EVENTS = new EventType[] {EventType.axisConfigChanged,
            EventType.axisConfigDone, EventType.axisConfigShowing, EventType.measuresChanged,
            EventType.measuresChanging };

    private GraphPanelPopupMousePlugin gppmp;
    private AxisConfigurationDialog xacDialog;
    private AxisConfigurationDialog yacDialog;
    private double offsetX = 0;
    private double offsetY = 0;
    private double panelDisplayX = 2;
    private double panelDisplayY = 2;

    private MeasureConfigurationDialog measureDialog;

    /**
     * Get access to the measure dialog.
     * @return The MeasureConfigurationDialog.
     */
    MeasureConfigurationDialog getMeasureDialog() {
        return measureDialog;
    }

    /**
     * Ordered by x,y-Coordinates.
     */
    private List<List<GraphPanel>> graphPanelList;
    private Axis yAxis;
    private Axis xAxis;

    /* The panel that will hold all the GraphPanels */
    private JPanel graphsFoundation;

    private DisplayOptions globalDisplayOptions;

    DisplayOptions getGlobalDisplayOptions() {
        return globalDisplayOptions;
    }

    /**
     * Returns how may panels are currently being displayed simultaneously on the x axis.
     * @return The number of panels currently being displayed in x-direction
     */
    public double getPanelDisplayX() {
        return panelDisplayX;
    }

    /**
     * Sets how many panels should be displayed simultaneously in x-direction.
     * @param panelDisplayX The number of panels to display simultaneously in x-direction
     */
    public void setPanelDisplayX(final double panelDisplayX) {
        double width;

        this.panelDisplayX = panelDisplayX;

        if (getXAxisLength() < this.panelDisplayX) {
            width = (double) graphsFoundation.getWidth() / getXAxisLength();
        } else {
            width = (graphsFoundation.getWidth() / this.panelDisplayX);
        }

        this.xAxis.setPanelWidth(width);
        this.resizeGraphPanels();
    }

    /**
     * Returns how may panels are currently being displayed simultaneously on the y axis.
     * @return The number of panels currently being displayed in y-direction
     */
    public double getPanelDisplayY() {
        return panelDisplayY;
    }

    /**
     * Sets how many panels should be displayed simultaneously in y-direction.
     * @param panelDisplayY The number of panels to display simultaneously in y-direction
     */
    public void setPanelDisplayY(final double panelDisplayY) {
        double width;

        this.panelDisplayY = panelDisplayY;

        if (getYAxisLength() < this.panelDisplayY) {
            width = (double) graphsFoundation.getHeight() / getYAxisLength();
        } else {
            width = (graphsFoundation.getHeight() / this.panelDisplayY);
        }

        this.yAxis.setPanelWidth(width);
        this.resizeGraphPanels();
    }

    /**
     * Returns the list of GraphPanels being displayed on the GraphGrid2D.
     * @return the list of GraphPanels being displayed on the GraphGrid2D
     */
    public List<List<GraphPanel>> getGraphPanelList() {
        return graphPanelList;
    }

    /**
     * Returns the panel holding all the GraphPanels.
     * @return the panel holding all the GraphPanels
     */
    public JPanel getGraphsFoundation() {
        return graphsFoundation;
    }

    /**
     * Update the options of all {@link GraphPanel}.
     */
    public void updateGlobalOptions() {
        for (List<GraphPanel> gpl : this.graphPanelList) {
            for (GraphPanel gp : gpl) {
                gp.updateOptions(this.globalDisplayOptions);
            }
        }
    }

    /**
     * Create a new empty graph grid.
     * @param owner The owner of opened dialogs in this component.
     * @param options The Structure that holds Display Options.
     */
    public GraphGrid2D(final JFrame owner, final DisplayOptions options) {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWeights = new double[] {0.0, 1.0 };
        gridBagLayout.rowWeights = new double[] {1.0, 0.0 };
        setLayout(gridBagLayout);

        for (EventType type : TRIGGERED_EVENTS) {
            getListeners().put(type, new HashSet<IEventHandler>());
        }

        this.globalDisplayOptions = options;

        setGppmp(new GraphPanelPopupMousePlugin());
        getGppmp().addGlobalMenuItem("Save graph as SVG", new SVGSaveListener(this, owner));
        getGppmp().addGlobalMenuItem("Save graph as PNG", new PNGSaveListener(this, owner));
        getGppmp().addGlobalMenuItem("Save graph as GraphML", new GraphMLSaveListener(this, owner));
        getGppmp().addGlobalMenuItem("Open this graph in separate window", new FullscreenViewListener(this));
        getGppmp().addGlobalMenuItem("Toggle force labels", new ToggleLabelListener());
        initGraphsFoundation();
        initAxisSurface(owner);
        initMeasureCanvas();
        initMeasureDialog(owner);
        addComponentListener(new GraphScrollListener(this));
    }

    private void initMeasureDialog(final JFrame owner) {
        measureDialog = new MeasureConfigurationDialog(owner);
        measureDialog.setName("measureDialog");
        for (EventType typ : MeasureConfigurationDialog.TRIGGERED_EVENTS) {
            addEventListener(this, typ);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(1024, 768);
    }

    /**
     * Initializes the surfaces for the axes.
     * @param owner The owner of opened dialogs in this component.
     */
    private void initAxisSurface(final JFrame owner) {
        yAxis = new Axis(AxisOrientation.YAxis);
        yAxis.setName("yAxis");
        yacDialog = new AxisConfigurationDialog(owner);
        yacDialog.setName("yAxisConfigurationDialog");
        yacDialog.setAxis(yAxis);
        yAxis.addMouseListener(new AxisReactionListener(yacDialog));
        GridBagConstraints gbcYAxisPanel = new GridBagConstraints();
        gbcYAxisPanel.insets = new Insets(0, 0, 5, 5);
        gbcYAxisPanel.fill = GridBagConstraints.BOTH;
        gbcYAxisPanel.gridx = 0;
        gbcYAxisPanel.gridy = 0;
        add(yAxis, gbcYAxisPanel);

        /* This listener will notice when the axis is moved... */
        this.yAxis.addOffsetChangeListener(new OffsetChangeListener() {

            @Override
            public void offsetChanged(final OffsetEvent e) {
                GraphGrid2D.this.offsetY = e.offset();
                GraphGrid2D.this.repositionGraphPanels();
            }
        });
        for (EventType typ : AxisConfigurationDialog.TRIGGERED_EVENTS) {
            yacDialog.addEventListener(this, typ);
        }

        xAxis = new Axis(AxisOrientation.XAxis);
        xAxis.setName("xAxis");
        xacDialog = new AxisConfigurationDialog(owner);
        xacDialog.setName("xAxisConfigurationDialog");
        xacDialog.setAxis(xAxis);
        xAxis.addMouseListener(new AxisReactionListener(xacDialog));
        GridBagConstraints gbcXAxisPanel = new GridBagConstraints();
        gbcXAxisPanel.fill = GridBagConstraints.BOTH;
        gbcXAxisPanel.gridx = 1;
        gbcXAxisPanel.gridy = 1;
        add(xAxis, gbcXAxisPanel);
        graphPanelList = new ArrayList<List<GraphPanel>>();
        this.xAxis.addOffsetChangeListener(new OffsetChangeListener() {

            @Override
            public void offsetChanged(final OffsetEvent e) {
                GraphGrid2D.this.offsetX = e.offset();
                GraphGrid2D.this.repositionGraphPanels();
            }
        });
        for (EventType typ : AxisConfigurationDialog.TRIGGERED_EVENTS) {
            xacDialog.addEventListener(this, typ);
        }
    }

    /**
     * This initializes the little spot where we display the 'click here to configure the measures'-image.
     */
    private void initMeasureCanvas() {
        final JSVGCanvas measureButton;
        measureButton = new JSVGCanvas();
        measureButton.setName("measureButton");
        measureButton.setEnablePanInteractor(false);
        measureButton.setEnableImageZoomInteractor(false);
        measureButton.setRecenterOnResize(false);
        measureButton.setEnableResetTransformInteractor(false);
        measureButton.setEnableRotateInteractor(false);
        measureButton.setEnableZoomInteractor(false);
        measureButton.setMySize(new Dimension(50, 50));
        measureButton.setBackground(getBackground());
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
        try {
            URL resource = getClass().getResource("/measures.svg");
            String uri = resource.toURI().toString();
            Document svg = f.createDocument(uri);
            measureButton.setDocument(svg);
        } catch (IOException e) {
            LOGGER.error("Could not load image for measure dialog button", e);
        } catch (URISyntaxException e) {
            LOGGER.error("Could not load image for measure dialog button", e);
        }
        measureButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                measureDialog.setVisible(true);
            }

            @Override
            public void mouseEntered(final MouseEvent e) {
                measureButton.setBackground(Color.darkGray);
            }

            @Override
            public void mouseExited(final MouseEvent e) {
                measureButton.setBackground(GraphGrid2D.this.getBackground());
            }
        });

        GridBagConstraints gbcCanvas = new GridBagConstraints();
        gbcCanvas.insets = new Insets(0, 0, 0, 5);
        gbcCanvas.fill = GridBagConstraints.BOTH;
        gbcCanvas.gridx = 0;
        gbcCanvas.gridy = 1;
        add(measureButton, gbcCanvas);
    }

    /**
     * Initializes the surface for the graphs.
     */
    private void initGraphsFoundation() {
        graphsFoundation = new JPanel();
        // graphsFoundation.setBackground(Color.GREEN);
        graphsFoundation.setLayout(null);
        graphsFoundation.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(final ComponentEvent e) {
                resizeGraphPanels();
            }
        });
        GridBagConstraints gbcGraphsPanel = new GridBagConstraints();
        gbcGraphsPanel.insets = new Insets(0, 0, 5, 0);
        gbcGraphsPanel.fill = GridBagConstraints.BOTH;
        gbcGraphsPanel.gridx = 1;
        gbcGraphsPanel.gridy = 0;
        add(graphsFoundation, gbcGraphsPanel);
    }

    /**
     * Display a new set of graphs.
     * @param graphs The displayed graphs.
     */
    public void setGraphs(final Collection<? extends IOLAPGraph> graphs) {
        LOGGER.debug("getting " + graphs.size() + " graphs from the controller.");
        updateGraphGrid();
        boolean allGraphsMatch = true;
        for (IOLAPGraph olapGraph : graphs) {
            allGraphsMatch &= matchGraphToPanel(olapGraph);
        }
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                resizeGraphPanels();
            }
        });
        if (!allGraphsMatch) {
            displayError("Could not position all graphs.");
        }
        LOGGER.debug("finished matching graphs to panels.");
    }

    /**
     * Position a graph in the Grid.
     * @param olapGraph The graph will shall be shown in the grid.
     * @param forceLabels Force displaying the complete labels even if it would get too crowded.
     * @param removeIsolated Remove Nodes that have no Edges from display.
     */
    private boolean matchGraphToPanel(final IOLAPGraph olapGraph) {
        int x = -1;
        int y = -1;
        IRequest req = olapGraph.getResultOf();
        for (IConstraint constraint : req) {
            if (constraintMatchesDialog(xacDialog, constraint)) {
                // Assume that the returned constraint has only one value (as documented).
                if (constraint.getDimension().getType() == DimensionType.tDimension) {
                    x = xAxis.getIndexOf(constraint.getHierarchyLevel().getName());
                } else {
                    x = xAxis.getIndexOf(constraint.getValues().toArray()[0].toString());
                }
            } else if (constraintMatchesDialog(yacDialog, constraint)) {
                // Assume that the returned constraint has only one value (as documented).
                if (constraint.getDimension().getType() == DimensionType.tDimension) {
                    y = yAxis.getIndexOf(constraint.getHierarchyLevel().getName());
                } else {
                    y = yAxis.getIndexOf(constraint.getValues().toArray()[0].toString());
                }
            }
        }

        // Consider the case that one axis has nothing selected.
        if (x == -1 && xAxis.isBlank()) {
            x = 0;
        }
        if (y == -1 && yAxis.isBlank()) {
            y = 0;
        }

        if (x == -1 || y == -1) {
            LOGGER.error("Matching produced invalid coordinates: (" + Integer.toString(x) + ", " + Integer.toString(y)
                    + ")");
            return false;
        } else {
            GraphPanel graphPanel = graphPanelList.get(x).get(y);
            LOGGER.debug("matched graph to panel.");
            graphPanel.setGraph(olapGraph);
            return true;
        }
    }

    /**
     * @return The position at which the given GraphPanel is located.
     * @param gp The panel to get the position of.
     */
    private List<Integer> getPanelPosition(final GraphPanel gp) {
        for (int x = 0; x < graphPanelList.size(); x++) {
            int y = graphPanelList.get(x).indexOf(gp);
            if (y != -1) {
                List<Integer> result = new ArrayList<Integer>(2);
                result.add(x);
                result.add(y);
                return result;
            }
        }

        throw new IllegalArgumentException("getPanelPosition: Given panel not found");
    }

    /**
     * @return The axis labels at the positions of the given GraphPanel. If no axes are present, null is returned.
     * @param gp The panel to get the axis labels for.
     */
    public List<String> getPanelAxisLabels(final GraphPanel gp) {
        List<Integer> panelPos = getPanelPosition(gp);
        List<String> result = new ArrayList<String>(2);

        if (!xAxis.isBlank()) {
            result.add(xAxis.getValues().get(panelPos.get(0)));
        }
        if (!yAxis.isBlank()) {
            result.add(yAxis.getValues().get(panelPos.get(1)));
        }

        return result;
    }

    /**
     * Check if an constraint matches an axis configuration so that this constraint is used to position the graph.
     * @param acDialog The axis configuration (dialog)
     * @param constraint The constraint which may be used for positioning.
     * @return True, if this constraint is relevant for an axis.
     */
    private boolean constraintMatchesDialog(final AxisConfigurationDialog acDialog, final IConstraint constraint) {
        boolean result = constraint.getDimension().getName().equals(acDialog.getSelectedDimension());
        result &= constraint.getHierarchy().getName().equals(acDialog.getSelectedHierarchy());
        if (constraint.getDimension().getType() != DimensionType.tDimension) {
            result &= constraint.getHierarchyLevel().getName().equals(acDialog.getSelectedHierarchyLevel());
        }
        return result;
    }

    /**
     * Checks if the GraphPanel are in the right positions and that there are enough of them.
     */
    private void updateGraphGrid() {
        // Clear it firstpanel
        graphsFoundation.removeAll();

        int xAxisLength = getXAxisLength();
        int yAxisLength = getYAxisLength();

        // Renew the array of panels
        graphPanelList = new ArrayList<List<GraphPanel>>(xAxisLength);
        for (int i = 0; i < xAxisLength; i++) {
            graphPanelList.add(new ArrayList<GraphPanel>(yAxisLength));
        }

        // Add the panels
        for (int i = 0; i < yAxisLength; i++) {
            for (int j = 0; j < xAxisLength; j++) {
                GraphPanel panel = new GraphPanel(getGppmp(), measureDialog, globalDisplayOptions);
                panel.setVisible(true);
                graphsFoundation.add(panel);
                graphPanelList.get(j).add(panel);
            }
        }

        resizeGraphPanels();
    }

    private int getYAxisLength() {
        return Math.max(1, yAxis.getValues().size());
    }

    private int getXAxisLength() {
        return Math.max(1, xAxis.getValues().size());
    }

    /**
     * This function updates the position of all the GraphPanels inside the graphsFoundation. This is to be used when
     * the axis were moved.
     */
    private void repositionGraphPanels() {
        int panelWidth;
        int panelHeight;

        if (getXAxisLength() < this.panelDisplayX) {
            panelWidth = graphsFoundation.getWidth() / getXAxisLength();
        } else {
            panelWidth = (int) (graphsFoundation.getWidth() / this.panelDisplayX);
        }

        if (getYAxisLength() < this.panelDisplayY) {
            panelHeight = graphsFoundation.getHeight() / getYAxisLength();
        } else {
            panelHeight = (int) (graphsFoundation.getHeight() / this.panelDisplayY);
        }

        for (int i = 0; i < graphPanelList.size(); i++) {
            for (int j = 0; j < graphPanelList.get(i).size(); j++) {
                GraphPanel graphSurface = graphPanelList.get(i).get(j);
                graphSurface.setLocation((int) ((i * panelWidth) + this.offsetX),
                        (int) ((j * panelHeight) + this.offsetY));
            }
        }
    }

    /**
     * This resizes *and* repositions all the GraphPanels inside the graphFoundation. This is to be used when the
     * GraphGrid2D was resized. Don't use this when you could use repositionGraphPanels(), since this will ask the graph
     * renderer for a new image, making the graphs 'jump around'.
     */
    private void resizeGraphPanels() {
        int panelWidth;
        int panelHeight;

        // graphsPanel.setBounds(yAxis.getWidth(), 0, getWidth() - yAxis.getWidth(), getHeight() - xAxis.getHeight());
        if (getXAxisLength() < this.panelDisplayX) {
            panelWidth = graphsFoundation.getWidth() / getXAxisLength();
        } else {
            panelWidth = (int) (graphsFoundation.getWidth() / this.panelDisplayX);
        }

        if (getYAxisLength() < this.panelDisplayY) {
            panelHeight = graphsFoundation.getHeight() / getYAxisLength();
        } else {
            panelHeight = (int) (graphsFoundation.getHeight() / this.panelDisplayY);
        }

        xAxis.setPanelWidth(panelWidth);
        yAxis.setPanelWidth(panelHeight);

        for (int i = 0; i < graphPanelList.size(); i++) {
            for (int j = 0; j < graphPanelList.get(i).size(); j++) {
                GraphPanel graphSurface = graphPanelList.get(i).get(j);
                graphSurface.setBounds((int) ((i * panelWidth) + this.offsetX),
                        (int) ((j * panelHeight) + this.offsetY), panelWidth, panelHeight);
                graphSurface.setBorder(BorderFactory.createLineBorder(Color.black));
            }
        }
    }

    /**
     * Added a listener to a given {@link EventType}.
     * @param l The lister for this event.
     * @param event The {@link EventType} this listener should be called on.
     */
    public void addEventListener(final IEventHandler l, final EventType event) {
        if (java.util.Arrays.asList(AxisConfigurationDialog.TRIGGERED_EVENTS).contains(event)) {
            xacDialog.addEventListener(l, event);
            yacDialog.addEventListener(l, event);
        } else {
            measureDialog.addEventListener(l, event);
        }
    }

    /**
     * Remove a listener from an event.
     * @param l The listener that should be removed.
     * @param event The {@link EventType} this listener was called on.
     */
    public void removeEventListener(final IEventHandler l, final EventType event) {
        if (java.util.Arrays.asList(AxisConfigurationDialog.TRIGGERED_EVENTS).contains(event)) {
            xacDialog.removeEventListener(l, event);
            yacDialog.removeEventListener(l, event);
        } else {
            measureDialog.removeEventListener(l, event);
        }
    }

    /**
     * Returns the 'private state object' of this GraphGrid2D. With this (to everything but GraphGrid2D opaque) object
     * it is later possible to restore the current state of view.
     * @return The newly created State object.
     */
    public IState getState() {
        GraphGrid2DState state = new GraphGrid2DState();
        state.setxAxis(xacDialog.getState());
        state.setyAxis(yacDialog.getState());
        state.setMeasureState(measureDialog.getState());

        state.setxAxisOffset(this.xAxis.getPanelOffset());
        state.setyAxisOffset(this.yAxis.getPanelOffset());
        return state;
    }

    /**
     * Sets the state of view of this GraphGrid2D to a previously saved one.
     * @param rawState An object that was previously returned by getState()
     */
    @SuppressWarnings("BC_UNCONFIRMED_CAST")
    public void setState(final IState rawState) {
        final GraphGrid2DState state = (GraphGrid2DState) rawState;
        xacDialog.setState(state.getxAxis());
        yacDialog.setState(state.getyAxis());
        measureDialog.setState(state.getMeasureState());

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                yAxis.setPanelOffset(state.getyAxisOffset());
                xAxis.setPanelOffset(state.getxAxisOffset());
            }
        });
    }

    /**
     * Return the GraphGrid2D to a mint state.
     */
    public void getFresh() {
        clear();
        measureDialog.getFresh();
    }

    /**
     * Clears the display, i.e. it will not be showing any graphs afterwards.
     */
    public void clear() {
        xAxis.setValues(new ArrayList<String>());
        yAxis.setValues(new ArrayList<String>());
        updateGraphGrid();
    }

    @Override
    public void event(final EventArgs e) {
        fireEvent(e.getType(), e.getDetails());
    }

    /**
     * Sets the GraphPanelPopupMousePlugin for this GraphGrid2D
     * @param gppmp The GraphPanelPopupMousePlugin to be set
     */
    private void setGppmp(final GraphPanelPopupMousePlugin gppmp) {
        this.gppmp = gppmp;
    }

    /**
     * Returns the GraphPanelPopupMousePlugin for this GraphGrid2D
     * @return the GraphPanelPopupMousePlugin for this GraphGrid2D
     */
    GraphPanelPopupMousePlugin getGppmp() {
        return gppmp;
    }

    /**
     * Shutdown as elegantly as we can.
     */
    void shutdown() {
        suspendEvents();
        yacDialog.setVisible(false);
        xacDialog.setVisible(false);
        measureDialog.shutdown();
    }
}
