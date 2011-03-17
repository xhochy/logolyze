package de.logotakt.logolyze.view.swing2d;

import de.logotakt.logolyze.view.interfaces.IViewState;

/**
 * Global state of the {@link Swing2DView}.
 */
public class Swing2DState implements IViewState, IState {
    private String cube;
    private IState graphGrid;
    private IState hierarchyTree;
    private Double xSpinner;
    private Double ySpinner;
    private DisplayOptions displayOptions;

    /**
     * Get the state of the {@link HierarchyTreeView}.
     * @return The state of the {@link HierarchyTreeView}.
     */
    public IState getHierarchyTree() {
        return hierarchyTree;
    }

    /**
     * Set the state of the {@link HierarchyTreeView}.
     * @param hierarchyTree The state of the {@link HierarchyTreeView}.
     */
    public void setHierarchyTree(final IState hierarchyTree) {
        this.hierarchyTree = hierarchyTree;
    }

    /**
     * Set the state of the {@link GraphGrid2D}.
     * @param graphGrid The state of the {@link GraphGrid2D}.
     */
    public void setGraphGrid(final IState graphGrid) {
        this.graphGrid = graphGrid;
    }

    /**
     * Get the state of the {@link GraphGrid2D}.
     * @return The saved state of the {@link GraphGrid2D}.
     */
    public IState getGraphGrid() {
        return graphGrid;
    }

    /**
     * Set the currently selected cube.
     * @param cube The currently selected cube.
     */
    public void setCube(final String cube) {
        this.cube = cube;
    }

    /**
     * Get the saved selected cube.
     * @return The saved cube selection.
     */
    public String getCube() {
        return cube;
    }

    /**
     * Set the value of the X-Spinner.
     * @param xSpinner The value of the X-Spinner.
     */
    public void setXSpinner(final Double xSpinner) {
        this.xSpinner = xSpinner;
    }

    /**
     * Get the value of the X-Spinner.
     * @return The value of the X-Spinner.
     */
    public Double getXSpinner() {
        return xSpinner;
    }

    /**
     * Set the value of the Y-Spinner.
     * @param ySpinner The value of the Y-Spinner.
     */
    public void setYSpinner(final Double ySpinner) {
        this.ySpinner = ySpinner;
    }
    /**
     * Get the value of the Y-Spinner.
     * @return The value of the Y-Spinner.
     */
    public Double getYSpinner() {
        return ySpinner;
    }

    /**
     * Set the DisplayOptions.
     * @param displayOptions The DisplayOptions.
     */
    public void setDisplayOptions(final DisplayOptions displayOptions) {
        this.displayOptions = displayOptions;
    }

    /**
     * Get the DisplayOptions.
     * @return The DisplayOptions.
     */
    public DisplayOptions getDisplayOptions() {
        return displayOptions;
    }
}
