package de.logotakt.logolyze.view.swing2d;

import java.awt.Color;

/**
 * This class holds options for displaying graphs.
 */
public class DisplayOptions {

    private LayoutName layout;
    private Color nodeColor;
    private boolean forceLabels;
    private boolean removeIsolated;
    private boolean showLegend;

    /**
     * Get the layout used for graphs.
     * @return The layout to be used for graphs.
     */
    public LayoutName getLayout() {
        return layout;
    }

    /**
     * Set the layout to be used for graphs.
     * @param layout The layout to be used for graphs
     */
    public void setLayout(final LayoutName layout) {
        this.layout = layout;
    }

    /**
     * Set whether to force display of levels at all zoom levels.
     * @param forceLabels Whether to force display of labels.
     */
    public void setForceLabels(final boolean forceLabels) {
        this.forceLabels = forceLabels;
    }

    /**
     * Set whether to remove Nodes without Nodes attached to them.
     * @param removeIsolated Wether to remove isolated nodes.
     */
    public void setRemoveIsolated(final boolean removeIsolated) {
        this.removeIsolated = removeIsolated;
    }

    /**
     * Set the Color to be used for Graph Nodes.
     * @param nodeColor The node color to be used.
     */
    public void setNodeColor(final Color nodeColor) {
        this.nodeColor = nodeColor;
    }

    /**
     * Set whether to display legends for the Measure Display Types.
     * @param showLegend Whether to show legends.
     */
    public void setShowLegend(final boolean showLegend) {
        this.showLegend = showLegend;
    }

    /**
     * Get the Color to be used for Nodes.
     * @return The Color to be used for Nodes.
     */
    public Color getNodeColor() {
        return this.nodeColor;
    }

    /**
     * Get whether to force display of labels.
     * @return Whether to force display of labels.
     */
    public boolean isLabelDisplayForced() {
        return this.forceLabels;
    }

    /**
     * Get whether to remove isolated nodes.
     * @return whether to remove isolated nodes.
     */
    public boolean isRemoveIsolatedNodesWanted() {
        return this.removeIsolated;
    }

    /**
     * Get whether to display legends.
     * @return whether to display legends.
     */
    public boolean isShowLegendWanted() {
        return this.showLegend;
    }

    /**
     * Construct a new DisplayOptions object.
     * @param layout The layout to use for graphs.
     * @param nodeColor The color to use for nodes.
     * @param forceLabels Whether to force display of labels.
     * @param removeIsolated Whether to remove isolated nodes.
     * @param showLegend Whether to display legends.
     */
    public DisplayOptions(final LayoutName layout, final Color nodeColor, final boolean forceLabels,
            final boolean removeIsolated, final boolean showLegend) {
        this.layout = layout;
        this.nodeColor = nodeColor;
        this.forceLabels = forceLabels;
        this.removeIsolated = removeIsolated;
        this.showLegend = showLegend;
    }

    /**
     * Construct a DisplayOptions object from another DisplayObtions object.
     * @param from The DisplayObject to copy from.
     */
    public DisplayOptions(final DisplayOptions from) {
        updateFrom(from);
    }

    /**
     * Update the Options from another DisplayOptions object.
     * @param from The DisplayObject to copy from
     */
    public void updateFrom(final DisplayOptions from) {
        this.layout = from.getLayout();
        this.nodeColor = from.getNodeColor();
        this.forceLabels = from.isLabelDisplayForced();
        this.removeIsolated = from.isRemoveIsolatedNodesWanted();
        this.showLegend = from.isShowLegendWanted();
    }

    // This is a method on its own because we might do this via some configuration manager.
    /**
     * Get the default DisplayOptions.
     * @return the default DisplayOptions.
     */
    public static DisplayOptions getDefault() {
        return new DisplayOptions(LayoutName.ISOMLayout, new Color(149, 206, 255), false, true, true);
    }
}
