package de.logotakt.logolyze.view.swing2d;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;
import org.apache.log4j.Logger;

import de.logotakt.logolyze.model.interfaces.IEdge;
import de.logotakt.logolyze.model.interfaces.IMeasure;
import de.logotakt.logolyze.model.interfaces.INode;
import de.logotakt.logolyze.model.interfaces.IOLAPGraph;
import edu.uci.ics.jung.algorithms.filters.VertexPredicateFilter;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.PluggableGraphMouse;
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.decorators.DirectionalEdgeArrowTransformer;
import edu.uci.ics.jung.visualization.renderers.BasicVertexLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.renderers.VertexLabelAsShapeRenderer;

/**
 * Panel for rendering OLAP graphs.
 */
@SuppressWarnings("serial")
public class GraphPanel extends JPanel implements MouseListener {
    private static Logger logger = Logger.getLogger(GraphPanel.class);

    /**
     * This filter removes nodes that have no Edges point to or from them.
     */
    private static final class IsolationFilter implements Predicate<INode> {
        private final IOLAPGraph graph;

        private IsolationFilter(final IOLAPGraph graph) {
            this.graph = graph;
        }

        public boolean evaluate(final INode node) {
            return graph.getIncidentEdges(node).size() != 0;
        }
    }
    
    /**
     * This predicate decides on whether to draw arrows or not.
     */
    private static final class ArrowFilter implements Predicate<Context<Graph<INode,IEdge>,IEdge>> {
        private final boolean drawArrows;
        
        ArrowFilter(IOLAPGraph graph) {
        	// Those extra tests are needed since the test cases don't supply fully functional requests...
        	// TODO improve the mocks used in the tests?
            if ((graph != null) && (graph.getResultOf() != null) && (graph.getResultOf().getCube() != null)) {
                this.drawArrows = graph.getResultOf().getCube().isDirected();
            } else {
                // If we don't have a graph or that one is borked, this does not really matter.
            	if ((graph != null) && ((graph.getResultOf() == null) || (graph.getResultOf().getCube() == null))) {
            		logger.error("We got a graph with no request or cube here. In test cases, this is OK. In productive use, it is not.");
            	}

            	this.drawArrows = true;
            }
        }
        
        @Override
        public boolean evaluate(Context<Graph<INode,IEdge>, IEdge> ctx) {
           return this.drawArrows;
        }
        
    }

    private GraphResizeListener graphResizeListener;
    private GraphPanelPopupMousePlugin gppmp;
    private MeasureConfigurationDialog configDialog;

    private TranslatingGraphMousePlugin tgmp;
    private PickingGraphMousePlugin<INode, IEdge> pgmp;
    private PluggableGraphMouse pgm;

    private JLabel legendLabel = null;
    private DirectionalEdgeArrowTransformer defaultArrowTransformer;

    /* The Graph that we are showing */
    private IOLAPGraph graph = null;
    /* The same graph, before any filtering was done */
    private IOLAPGraph rawGraph = null;

    /* Caching. We cache the graphs image in this BufferedImage, to make scrolling smooth */
    private BufferedImage img = null;
    /* If this is true, buffering is disabled */
    private boolean noCache = false;

    /* Various transformers. See addVertexRenderer() for details. */
    private VertexLabelAsShapeRenderer<INode, IEdge> labelVertexRenderer = null;
    private Transformer<INode, String> labelNameTransformer = null;
    private Transformer<INode, String> tooltipOnlyTransformer = null;
    private BasicVertexLabelRenderer<INode, IEdge> basicVertexRenderer = null;
    private Transformer<INode, String> labelNullTransformer = null;
    private Transformer<INode, Shape> shapeNullTransformer = null;
    private Transformer<INode, String> tooltipAndLabelTransformer = null;

    // Label length in characters from which to split the line.
    private static final int MAX_LABEL_LENGTH = 15;

    /*
     * VERTEX SPACING The method adjustLayoutSpacing determines a value of X and Y spacing to distribute the vertices of
     * the displayed graphs nicely. The following constants are parameters to tune its behaviour and work thus: 1. A
     * minimum value for spacing is computed. Using this value, the biggest vertices will just about touch, so it needs
     * to be enlarged in this case. However, with a wide distribution of vertex widths, this is usually too much. 2.
     * Therefore, depending on the ratio between the largest and the average-sized vertex, one of two scaling factors is
     * applied. 3. An additional spacing is added in any case. (This works the same for width and height)
     */

    // Extra spacing applied between the vertices
    private static final double WIDTH_SPACING = 1; // Do not change to 0, because TreeLayout will then crash on an empty
    // graph.
    private static final double HEIGHT_SPACING = TreeLayout.DEFAULT_DISTY;

    // Treshold from which to regard a graph as having great differences in vertex size.
    private static final double WIDTH_TRESHOLD = 1.3;
    private static final double HEIGHT_TRESHOLD = 1.3;

    // Compression, when vertices are similar size
    private static final double WIDTH_COMPRESSION_SIMILAR = 0.7;
    private static final double HEIGHT_COMPRESSION_SIMILAR = 1;

    // Compression, when vertices are different in size.
    private static final double WIDTH_COMPRESSION_DIFFERENT = 1.2;
    private static final double HEIGHT_COMPRESSION_DIFFERENT = 1.2;

    // How many nodes per square pixel should there be at a maximum to display labels?
    private static final double THRESHOLD_LABELCOVERED = 0.2;

    /* EDGE STYLE */
    private static final float MAX_EDGE_WIDTH = 10;
    private static final float DASH_PATTERN_LENGTH = 30;

    /* VERTEX STYLE */
    private static final int MIN_FONT_SIZE = 5;
    private static final int MAX_FONT_SIZE = 20;
    private static final int MAX_BOBBEL_SIZE = 35;

    /* Mouse plugins */
    /* Plugin to enable scaling the graphs */
    private EventFiringScalingGraphMousePlugin sgmp;

    /* The visualization viewer is the JUNG component that will give us something viewable */
    private VisualizationViewer<INode, IEdge> bv;

    /*
     * This stores the average size (in square-pixels) of a label in the graph. Needed for auto-bobbeling.
     */
    private Double avgLabelArea = new Double(0); // Initialize this, value does not really matter

    private DisplayOptions options;

    /**
     * Change the DisplayOptions, only changing what's necessary.
     * @param newOptions The new Display Options.
     */
    public void updateOptions(final DisplayOptions newOptions) {
        logger.debug("Updating display options.");

        if (graph == null) {
            return;
        }

        if (newOptions.getLayout() != this.options.getLayout()) {
            this.options.setLayout(newOptions.getLayout());
            this.rebuildLayout(this.options.getLayout());
        }

        if (newOptions.getNodeColor() != this.options.getNodeColor()) {
            this.options.setNodeColor(newOptions.getNodeColor());
        }

        if (newOptions.isLabelDisplayForced() != this.options.isLabelDisplayForced()) {
            this.options.setForceLabels(newOptions.isLabelDisplayForced());

            this.updateShapes();
        }

        if (newOptions.isRemoveIsolatedNodesWanted() != this.options.isRemoveIsolatedNodesWanted()) {
            this.options.setRemoveIsolated(newOptions.isRemoveIsolatedNodesWanted());

            this.setGraph(rawGraph); // Ugly hack to recalculate really everything.
        }

        if (newOptions.isShowLegendWanted() != this.options.isShowLegendWanted()) {
            this.options.setShowLegend(newOptions.isShowLegendWanted());
            this.buildMeasuresLegend();
        }

        this.invalidateCache();
        this.repaint();
    }

    /**
     * Get the currently used DisplayOptions.
     * @return The currently used DisplayOptions.
     */
    public DisplayOptions getOptions() {
        return new DisplayOptions(this.options);
    }

    /**
     * Generate a new graph display pane.
     * @param gppmp The GraphPanelPopupMousePlugin to use for this GraphPanel
     * @param configDialog The MeasureConfigurationDialog holding the settings of which measures to show in this
     *        GraphPanel, and how to show them
     * @param opts The DisplayOptions.
     */
    public GraphPanel(final GraphPanelPopupMousePlugin gppmp, final MeasureConfigurationDialog configDialog,
            final DisplayOptions opts) {
        this.gppmp = gppmp;
        this.addMouseListener(this);
        this.configDialog = configDialog;
        this.options = new DisplayOptions(opts);

        this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    }

    /**
     * Returns the IOLAPGraph that is currently being shown by this panel.
     * @return The IOLAPGraph currently being shown by this panel
     */
    IOLAPGraph getGraph() {
        return this.graph;
    }

    private void addGraphMouse() {
        this.pgm = new PluggableGraphMouse();
        this.pgmp = new PickingGraphMousePlugin<INode, IEdge>(InputEvent.SHIFT_MASK | MouseEvent.BUTTON1_MASK,
                InputEvent.CTRL_DOWN_MASK | MouseEvent.BUTTON1_MASK);
        this.tgmp = new TranslatingGraphMousePlugin(MouseEvent.BUTTON1_MASK);

        this.pgm.add(this.tgmp);
        this.pgm.add(this.pgmp);
        sgmp = new EventFiringScalingGraphMousePlugin(new CrossoverScalingControl(), 0, 1.1f, 0.9f);
        this.pgm.add(sgmp);
        this.pgm.add(gppmp);

        sgmp.addListener(new ChangeListener() {

            @Override
            public void stateChanged(final ChangeEvent e) {
                GraphPanel.this.updateShapes();
            }
        });

        this.bv.setGraphMouse(this.pgm);
    }

    /**
     * This updates the AutoBobbeling decision whether to show Bobbels or the labels. It actually should not be needed
     * to call this from the outside.
     */
    private void updateShapes() {
        Double zoomLevel = GraphPanel.this.sgmp.getZoom();
        GraphPanel.this.setVertexRenderer(zoomLevel);
    }

    /**
     * This function applies all filtering operations to be done to the Graph before it is displayed. Currentĺy, that is
     * only removing isolated nodes.
     * @param graph The graph to filter.
     */
    private IOLAPGraph applyGraphFiltering(final IOLAPGraph graph) {
        if (this.options.isRemoveIsolatedNodesWanted()) {
            IOLAPGraph newGraph;
            
            logger.debug("Removing isolated nodes");

            VertexPredicateFilter<INode, IEdge> vFilter = new VertexPredicateFilter<INode, IEdge>(new IsolationFilter(
                    graph));
            newGraph = (IOLAPGraph) vFilter.transform(graph);
            newGraph.setResultOf(graph.getResultOf());
            return newGraph;
        } else {
            return graph;
        }
    }

    /**
     * This function implements large parts of the AutoBobbeling: Switching to whatever renderer is appropriate here.
     * @param zoomLevel The current zoom level
     */
    private void setVertexRenderer(final Double zoomLevel) {
        if (this.graph == null) {
            logger.debug("Not re-evaluating autobobbeling decision for null graph");
            return;
        }
        logger.debug("Re-evaluating autobobbeling decision");

        /*
         * This heuristic compares the (average, assumed) area covered with labels at the current zoom level with a
         * maximum coverage. These heuristic is actually pretty good, considering how simple it is.
         */
        
        if (this.graph == null) {
        	System.out.println("WTF? Graph!");
        }

        if (this.options == null) {
        	System.out.println("WTF? Options!");
        }
        
        if (this.avgLabelArea == null) {
        	System.out.println("WTF? LabelArea!");
        }
        
        if ((this.graph.getVertexCount() * 1 / (zoomLevel * zoomLevel) * this.avgLabelArea < (this.getWidth()
                * this.getHeight() * THRESHOLD_LABELCOVERED))
                || this.options.isLabelDisplayForced()) {
            // There is less than THRESHOLD_LABELCOVERED space covered, show the labels
            logger.debug("Labels will be rendered");
            this.bv.getRenderer().setVertexLabelRenderer(this.labelVertexRenderer);
            this.bv.getRenderContext().setVertexShapeTransformer(this.labelVertexRenderer);
            this.bv.getRenderContext().setVertexLabelTransformer(this.labelNameTransformer);
            this.bv.setVertexToolTipTransformer(this.tooltipOnlyTransformer);
        } else {
            // There is more than THRESHOLD_LABELCOVERED space covered, show the bobbels
            logger.debug("Bobbels will be rendered");
            this.bv.getRenderer().setVertexLabelRenderer(this.basicVertexRenderer);
            this.bv.getRenderContext().setVertexShapeTransformer(this.shapeNullTransformer);
            this.bv.getRenderContext().setVertexLabelTransformer(this.labelNullTransformer);
            this.bv.setVertexToolTipTransformer(this.tooltipAndLabelTransformer);
        }
    }

    private void addVertexRenderer() {
        // First, the rendering components affected by autobobbeling.
        // These are only stored in private fields for now and are
        // set in setVertexRenderer.

        // Renderers used when labels are displayed

        // This renderer builds a shape for every Vertex that is large enough to hold its label.
        this.labelVertexRenderer = new VertexLabelAsShapeRenderer<INode, IEdge>(this.bv.getRenderContext());
        this.labelVertexRenderer.setPosition(Renderer.VertexLabel.Position.AUTO);

        // Build the label of a vertex, using the label for the Node it represents
        // and any measures selected to be displayed as text. A newline is added to the
        // Node label if it is too long.
        this.labelNameTransformer = new Transformer<INode, String>() {
            public String transform(final INode node) {
                String label = node.getLabel();
                if (label.length() > MAX_LABEL_LENGTH) {
                    int middle = label.length() / 2;
                    int spaceBefore = label.lastIndexOf(' ', middle);
                    spaceBefore = spaceBefore == -1 ? 0 : spaceBefore;
                    int spaceAfter = label.indexOf(' ', middle);
                    spaceAfter = spaceAfter == -1 ? label.length() : spaceAfter;
                    int target;
                    if (middle - spaceBefore <= spaceAfter - middle) {
                        target = spaceBefore;
                    } else {
                        target = spaceAfter;
                    }
                    label = label.substring(0, target) + "<br/>" + label.substring(target);
                }

                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, ? extends IMeasure> ent : node.getMeasures().entrySet()) {
                    if (configDialog.getNodeDisplayType(ent.getValue().getSet().getType())
                            == NodeMeasureDisplayType.text) {
                        sb.append(ent.getKey());
                        sb.append(": ");
                        sb.append(ent.getValue().getText());
                        sb.append("<br/>");
                    }
                }

                return "<html>" + label + "<br/>" + sb.toString() + "</html>";
            }
        };

        // Build a tooltip that only contains the measures (the label is displayed in the vertex itself)
        this.tooltipOnlyTransformer = new Transformer<INode, String>() {
            public String transform(final INode node) {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, ? extends IMeasure> ent : node.getMeasures().entrySet()) {
                    if (configDialog.getNodeDisplayType(ent.getValue().getSet().getType())
                            != NodeMeasureDisplayType.none) {
                        sb.append(ent.getKey());
                        sb.append(": ");
                        sb.append(ent.getValue().getText());
                        sb.append("<br/>");
                    }
                }

                if (sb.length() == 0) {
                    return null;
                } else {
                    return "<html>" + sb.toString() + "</html>";
                }
            }
        };

        // Renderers used when bobbels are displayed.

        this.basicVertexRenderer = new BasicVertexLabelRenderer<INode, IEdge>();

        // Builds an empty label for every Vertex.
        this.labelNullTransformer = new Transformer<INode, String>() {
            @Override
            public String transform(final INode arg0) {
                return "";
            }
        };

        // Builds a bobbel for every Vertex. The bobbel size is either fixed or depends
        // on the measure currently assigned to Vertex size.
        this.shapeNullTransformer = new Transformer<INode, Shape>() {
            @Override
            public Shape transform(final INode input) {
                float bobbelSize = 0;
                IMeasure sizeMeasure = null;

                sizeMeasure = getNodeMeasureForDisplayType(NodeMeasureDisplayType.size, input.getMeasures().values(),
                        configDialog);

                if (sizeMeasure != null) {
                    bobbelSize = (float) (sizeMeasure.getNumber() / sizeMeasure.getSet().getMax() * MAX_BOBBEL_SIZE);
                } else {
                    bobbelSize = 14.0f;
                }

                return new Ellipse2D.Float(-bobbelSize / 2.0f, -bobbelSize / 2.0f, bobbelSize, bobbelSize);
            }
        };

        // Builds a tooltip thath contains both label and selected measures.
        this.tooltipAndLabelTransformer = new Transformer<INode, String>() {
            public String transform(final INode node) {
                StringBuilder sb = new StringBuilder();

                sb.append(node.getLabel()).append("<br/>");

                for (Map.Entry<String, ? extends IMeasure> ent : node.getMeasures().entrySet()) {
                    if (configDialog.getNodeDisplayType(ent.getValue().getSet().getType())
                            != NodeMeasureDisplayType.none) {
                        sb.append(ent.getKey());
                        sb.append(": ");
                        sb.append(ent.getValue().getText());
                        sb.append("<br/>");
                    }
                }

                return "<html>" + sb.toString() + "</html>";
            }
        };

        // Second, the rendering components invariant to autobobbeling.

        // Paint the nodes in the currently selected color or according to the gradient, if
        // a measure is selected for the node.
        this.bv.getRenderContext().setVertexFillPaintTransformer(new Transformer<INode, Paint>() {
            @Override
            public Paint transform(final INode input) {
                IMeasure colorMeasure = null;

                colorMeasure = getNodeMeasureForDisplayType(NodeMeasureDisplayType.colour,
                        input.getMeasures().values(), configDialog);

                if (colorMeasure != null) {
                    return GraphPanel.getGradientColor(colorMeasure);
                } else {
                    return GraphPanel.this.options.getNodeColor();
                }

            }
        });

        // Adjust the font size according to the measure assigned to Vertex size. This Transformer
        // is always active, but will only have an effect when labels are displayed.
        this.bv.getRenderContext().setVertexFontTransformer(new Transformer<INode, Font>() {
            @Override
            public Font transform(final INode input) {
                IMeasure sizeMeasure = null;

                sizeMeasure = getNodeMeasureForDisplayType(NodeMeasureDisplayType.size, input.getMeasures().values(),
                        configDialog);

                if (sizeMeasure != null) {
                    return new Font(Font.SANS_SERIF, Font.PLAIN, (int) (sizeMeasure.getNumber()
                            / sizeMeasure.getSet().getMax() * (MAX_FONT_SIZE - MIN_FONT_SIZE))
                            + MIN_FONT_SIZE);
                } else {
                    return new Font(Font.SANS_SERIF, Font.PLAIN, 11);
                }
            }
        });
    }

    /**
     * This function does three things: it calculates some values needed for the 'compression' function of the graph
     * renderer library (i.e. whether to use compression at all), and it calculates the average label size, needed for
     * AutoBobbeling. It does all this with respect to the layout given. After calculating all this, it switches the
     * display to the layout given.
     * @param layoutName The layout that we should calculate the values for, and that should be effective afterwards
     */
    private void rebuildLayout(final LayoutName layoutName) {
        logger.debug("Rebuilding layout");

        // Important: We *have* to set the 'real' renderer here first, since the 'adjustLayoutSpacing'
        // function depends on it!
        this.bv.getRenderer().setVertexLabelRenderer(this.labelVertexRenderer);
        this.bv.getRenderContext().setVertexShapeTransformer(this.labelVertexRenderer);
        this.bv.getRenderContext().setVertexLabelTransformer(this.labelNameTransformer);

        // This only works, if this.bv is already connected to the GUI.
        // The vertexRenderer apparently needs somewhere to draw on to measure the sizes.
        // Therefore, we give it a dummy screenDevice
        JComponent prevDevice = this.bv.getRenderContext().getScreenDevice();
        this.bv.getRenderContext().setScreenDevice(new JPanel());

        Graph<INode, IEdge> otherGraph = this.bv.getGraphLayout().getGraph();
        int maxWidth = 0, maxHeight = 0, count = 0;

        double avgWidth = 0, avgHeight = 0;

        for (INode node : otherGraph.getVertices()) {
            Rectangle labelBounds = this.labelVertexRenderer.transform(node).getBounds();
            Rectangle shapeBounds = this.bv.getRenderContext().getVertexShapeTransformer().transform(node).getBounds();

            int height = Math.max(labelBounds.height, shapeBounds.height);
            int width = Math.max(labelBounds.width, shapeBounds.width);

            avgHeight += height;
            avgWidth += width;
            count++;

            if (height > maxHeight) {
                maxHeight = height;
            }
            if (width > maxWidth) {
                maxWidth = width;
            }
        }

        avgHeight /= count * 1.0;
        avgWidth /= count * 1.0;

        this.avgLabelArea = avgHeight * avgWidth;

        double actualWidthCompression, actualHeightCompression;

        // Only enable compression, if the vertices have sufficiently different sizes.
        // If they are all sized the same, collisions due to compression are much more likely.
        if (maxWidth / avgWidth >= WIDTH_TRESHOLD) {
            actualWidthCompression = WIDTH_COMPRESSION_DIFFERENT;
        } else {
            actualWidthCompression = WIDTH_COMPRESSION_SIMILAR;
        }

        if (maxHeight / avgHeight >= HEIGHT_TRESHOLD) {
            actualHeightCompression = HEIGHT_COMPRESSION_DIFFERENT;
        } else {
            actualHeightCompression = HEIGHT_COMPRESSION_SIMILAR;
        }

        if (prevDevice != null) {
            this.bv.getRenderContext().setScreenDevice(prevDevice);
        }

        switch (layoutName) {
        case TreeLayout:
            logger.debug("Creating new TreeLayout");
            this.bv.setGraphLayout(new SpanningTreeLayout<INode, IEdge>(otherGraph, (int) (maxWidth
                    / actualWidthCompression + WIDTH_SPACING),
                    (int) (maxHeight / actualHeightCompression + HEIGHT_SPACING)));
            break;
        case FRLayout:
            logger.debug("Creating new FRLayout");
            this.bv.setGraphLayout(new FRLayout<INode, IEdge>(otherGraph));
            break;
        case CircleLayout:
            logger.debug("Creating new CircleLayout");
            this.bv.setGraphLayout(new CircleLayout<INode, IEdge>(otherGraph));
            break;
        default:
        case ISOMLayout:
            logger.debug("Creating new ISOMLayout");
            this.bv.setGraphLayout(new ISOMLayout<INode, IEdge>(otherGraph));
        }

        this.updateShapes();
        this.invalidateCache();
        this.repaint();
    }

    private void addEdgeMeasuresRenderer() {
        this.bv.getRenderContext().setEdgeLabelTransformer(new Transformer<IEdge, String>() {
            public String transform(final IEdge edge) {
                // start with a somewhat reasonable size
                StringBuilder sb = new StringBuilder(12 + edge.getMeasures().size() * 20);
                sb.append("<html>");
                for (Map.Entry<String, ? extends IMeasure> ent : edge.getMeasures().entrySet()) {
                    if (configDialog.getEdgeDisplayType(ent.getValue().getSet().getType())
                            == EdgeMeasureDisplayType.text) {
                        sb.append(ent.getKey());
                        sb.append(": ");
                        sb.append(ent.getValue().getText());
                        sb.append("<br/>");
                    }
                }
                sb.append("</html>");

                return sb.toString();
            }
        });

        /*
         * This transformer transforms an edge so that the stroke style of that edge represents some measure value.
         */
        this.bv.getRenderContext().setEdgeStrokeTransformer(new Transformer<IEdge, Stroke>() {
            public Stroke transform(final IEdge edge) {
                IMeasure strokeWidthMeasure = null;
                IMeasure strokeStyleMeasure = null;

                // Performance problem to iterate twice?
                strokeWidthMeasure = getEdgeMeasureForDisplayType(EdgeMeasureDisplayType.strokeWidth, edge
                        .getMeasures().values(), configDialog);
                strokeStyleMeasure = getEdgeMeasureForDisplayType(EdgeMeasureDisplayType.strokeStyle, edge
                        .getMeasures().values(), configDialog);

                float strokeWidth = 1.0f;
                float[] strokeDashing = new float[] {1.0f };

                if (strokeWidthMeasure != null) {
                    strokeWidth = (float) (strokeWidthMeasure.getNumber()
                            / strokeWidthMeasure.getSet().getMax() * MAX_EDGE_WIDTH);
                }
                if (strokeStyleMeasure != null) {
                    strokeDashing = new float[2];
                    strokeDashing[0] = (float) (strokeStyleMeasure.getNumber()
                            / strokeStyleMeasure.getSet().getMax() * DASH_PATTERN_LENGTH);
                    strokeDashing[1] = DASH_PATTERN_LENGTH - strokeDashing[0];
                }

                // Additional arguments are those used in the default constructor.
                return new BasicStroke(strokeWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f,
                        strokeDashing, 0);

            }
        });

        /*
         * This transformer transforms an edge so that the color of that edge represents some measure value.
         */
        this.bv.getRenderContext().setEdgeDrawPaintTransformer(new Transformer<IEdge, Paint>() {
            public Paint transform(final IEdge edge) {
                IMeasure colorMeasure = null;

                colorMeasure = getEdgeMeasureForDisplayType(EdgeMeasureDisplayType.colour, edge.getMeasures().values(),
                        configDialog);

                if (colorMeasure != null) {
                    return GraphPanel.getGradientColor(colorMeasure);
                } else {
                    return Color.black;
                }
            }

        });

        /*
         * This transformer displays all selected measures in text form in the edge tooltip.
         */
        this.bv.setEdgeToolTipTransformer(new Transformer<IEdge, String>() {
            public String transform(final IEdge edge) {
                // start with a somewhat reasonable size
                StringBuilder sb = new StringBuilder(edge.getMeasures().size() * 20);

                for (Map.Entry<String, ? extends IMeasure> ent : edge.getMeasures().entrySet()) {
                    if (configDialog.getEdgeDisplayType(ent.getValue().getSet().getType())
                            != EdgeMeasureDisplayType.none) {
                        sb.append(ent.getKey());
                        sb.append(": ");
                        sb.append(ent.getValue().getText());
                        sb.append("<br/>");
                    }
                }

                if (sb.length() == 0) {
                    return null;
                } else {
                    return "<html>" + sb.toString() + "</html>";
                }
            }
        });
    }

    /**
     * Computes the color representing a given measure on the standard gradient (green -> yellow -> red).
     */
    private static Color getGradientColor(final IMeasure colorMeasure) {
        float percentage = (float) (colorMeasure.getNumber() / colorMeasure.getSet().getMax());

        if (Float.isNaN(percentage)) {
            // 0/0 occured. Default to 0
            percentage = 0.0f;
        }

        if (percentage < 0.5) {
            return new Color(percentage * 2.0f, 1.0f, 0.0f);
        } else if (percentage <= 1.0f) {
            return new Color(1.0f, 1 - (percentage - 0.5f) * 2.0f, 0.0f);
        } else {
            logger.error("Measure value was higher than reported maximum.");
            throw new IllegalStateException("Measure value was higher than reported maximum.");
        }
    }

    /**
     * Returns the Measure that was selected to be visualized using the given measure display type in the given
     * configuration dialog, or null if no such measure exists.
     */
    private static IMeasure getEdgeMeasureForDisplayType(final EdgeMeasureDisplayType dt,
            final Collection<? extends IMeasure> measures, final MeasureConfigurationDialog conf) {
        for (IMeasure m : measures) {
            if (conf.getEdgeDisplayType(m.getSet().getType()) == dt) {
                return m;
            }
        }

        return null;
    }

    /**
     * Returns the Measure that was selected to be visualized using the given measure display type in the given
     * configuration dialog, or null if no such measure exists.
     */
    private static IMeasure getNodeMeasureForDisplayType(final NodeMeasureDisplayType dt,
            final Collection<? extends IMeasure> measures, final MeasureConfigurationDialog conf) {
        for (IMeasure m : measures) {
            if (conf.getNodeDisplayType(m.getSet().getType()) == dt) {
                return m;
            }
        }

        return null;
    }

    private void buildMeasuresLegend() {
        String result = "";

        if (options.isShowLegendWanted()) {
            // Select an arbitrary edge to get the MeasureSets.
            Collection<? extends IMeasure> edgeMeasures = graph.getEdges().iterator().next().getMeasures().values();
            Collection<? extends IMeasure> nodeMeasures = graph.getVertices().iterator().next().getMeasures().values();

            String edgeLabels = "";
            String nodeLabels = "";

            IMeasure edgeColorMeasure = getEdgeMeasureForDisplayType(EdgeMeasureDisplayType.colour, edgeMeasures,
                    configDialog);
            if (edgeColorMeasure != null) {
                edgeLabels += " " + edgeColorMeasure.getSet().getType().getKey()
                        + ": <font color=lime>\u2588</font> 0.0 to <font color=red>\u2588</font> "
                        + edgeColorMeasure.getSet().getMax();
            }
            IMeasure edgeWidthMeasure = getEdgeMeasureForDisplayType(EdgeMeasureDisplayType.strokeWidth, edgeMeasures,
                    configDialog);
            if (edgeWidthMeasure != null) {
                edgeLabels += " " + edgeWidthMeasure.getSet().getType().getKey() + ": \u2500 0.0 to \u25ac "
                        + edgeWidthMeasure.getSet().getMax();
            }
            IMeasure edgeStrokeMeasure = getEdgeMeasureForDisplayType(EdgeMeasureDisplayType.strokeStyle, edgeMeasures,
                    configDialog);
            if (edgeStrokeMeasure != null) {
                edgeLabels += " " + edgeStrokeMeasure.getSet().getType().getKey()
                        + ": \u2010\u2005\u2010\u2005\u2010 0.0 to \u2500\u2500 " + edgeStrokeMeasure.getSet().getMax();
            }

            IMeasure nodeColorMeasure = getNodeMeasureForDisplayType(NodeMeasureDisplayType.colour, nodeMeasures,
                    configDialog);
            if (nodeColorMeasure != null) {
                nodeLabels += " " + nodeColorMeasure.getSet().getType().getKey()
                        + ": <font color=lime>\u2588</font> 0.0 to <font color=red>\u2588</font> "
                        + nodeColorMeasure.getSet().getMax();
            }
            IMeasure nodeSizeMeasure = getNodeMeasureForDisplayType(NodeMeasureDisplayType.size, nodeMeasures,
                    configDialog);
            if (nodeSizeMeasure != null) {
                nodeLabels += " " + nodeSizeMeasure.getSet().getType().getKey() + ": \u00b7 0.0 to \u2b24 "
                        + nodeSizeMeasure.getSet().getMax();
            }

            if (!edgeLabels.isEmpty()) {
                result += "Edges: " + edgeLabels;
            }
            if (!edgeLabels.isEmpty() && !nodeLabels.isEmpty()) {
                result += "<br/>";
            }
            if (!nodeLabels.isEmpty()) {
                result += "Nodes: " + nodeLabels;
            }

            if (!edgeLabels.isEmpty() || !nodeLabels.isEmpty()) {
                result = "<html>" + result + "</html>";
            }
        }
        logger.debug("Measures legend: " + result);

        this.legendLabel.setText(result);
        this.legendLabel.setSize(this.legendLabel.getPreferredSize());
    }

    /**
     * Sets the graph that should be displayed. Please note that this will reset all the settings made to zooming,
     * AutoBobbeling, etc. It displays a whole new graph afterwards.
     * @param newGraph The graph to be displayed.
     */
    public void setGraph(final IOLAPGraph newGraph) {
        if (newGraph == null) {
            logger.error("Graph in a GraphPanel set to null!");
            throw new NullPointerException("Graph in a GraphPanel set to null!");
        }
        Layout<INode, IEdge> layout;

        logger.debug("Setting new graph with " + newGraph.getVertexCount() + " nodes.");
        
        this.rawGraph = newGraph;
        this.graph = applyGraphFiltering(newGraph);

        this.setLayout(new BorderLayout());

        if (this.bv != null) {
            // TODO is this even needed? Isn't this also being done in rebuildLayout()?
            layout = this.bv.getGraphLayout();
            layout.setGraph(graph);
        } else {
            // we don't have a layout yet - create a default layout
            layout = new ISOMLayout<INode, IEdge>(graph);
            this.bv = new VisualizationViewer<INode, IEdge>(layout);

            graphResizeListener = new GraphResizeListener(this, this.bv);
            addComponentListener(graphResizeListener);
            this.bv.setPreferredSize(new Dimension(150, 150));
            this.initializeBV();
            this.add(this.bv, BorderLayout.CENTER);
        }

        // Finally, filter out arrows if the cube is undirected
        this.bv.getRenderContext().setEdgeArrowPredicate(new GraphPanel.ArrowFilter(newGraph));

        rebuildLayout(options.getLayout());

        // Now, for the real rendering.
        // Current zoom level is 1.0
        this.sgmp.setZoom(1.0);
        this.setVertexRenderer(1.0);

        if (this.legendLabel == null) {
            this.legendLabel = new JLabel();
            add(this.legendLabel, BorderLayout.NORTH);
        }

        buildMeasuresLegend();

        graphResizeListener.react();

        this.invalidateCache();
    }

    private void initializeBV() {
        this.addComponentListener(new ComponentListener() {

            @Override
            public void componentHidden(final ComponentEvent arg0) {
            }

            @Override
            public void componentMoved(final ComponentEvent arg0) {
            }

            @Override
            public void componentResized(final ComponentEvent arg0) {
                GraphPanel.this.updateShapes();
            }

            @Override
            public void componentShown(final ComponentEvent arg0) {
            }
        });

        addGraphMouse();

        addVertexRenderer();

        addEdgeMeasuresRenderer();

        this.bv.addMouseListener(this);
    }

    /*
     * We override the paint method here to implement caching of the graph's image inside a BufferedImage
     */
    @Override
    public void paintComponent(final Graphics g) {
        if (this.noCache) {
            logger.debug("Rendering - caching disabled");
            super.paintComponent(g);
        } else {
            // OK, we should buffer. See if we have a valid buffered image.
            if (this.img == null) {
                logger.debug("Creating new cache image");

                // OK, create the buffer and draw it.
                this.img = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
                super.paintComponent(this.img.createGraphics());
            }

            logger.debug("Rendering - cached image present");
            g.drawImage(this.img, 0, 0, null);
        }
    }

    @Override
    public void mouseClicked(final MouseEvent e) {

    }

    @Override
    public void mouseEntered(final MouseEvent e) {

    }

    @Override
    public void mouseExited(final MouseEvent e) {

    }

    @Override
    public void mousePressed(final MouseEvent e) {
        this.noCache = true;
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
        logger.debug("Dragging finished");

        this.noCache = false;
        this.invalidateCache();
    }

    /**
     * This invalidates the painting cache, forcing the panel to re-paint the graph the next time it's being painted.
     */
    void invalidateCache() {
        logger.debug("Cache invalidated");

        this.img = null;
    }
}
