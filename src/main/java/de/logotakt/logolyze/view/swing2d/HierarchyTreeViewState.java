package de.logotakt.logolyze.view.swing2d;

/**
 * Saves the state of the HierarchyTreeView (selected nodes, expanded branches).
 */
public class HierarchyTreeViewState implements IState {
    private static String[][] deepArrayCopy(final String[][] original) {
        String[][] result = new String[original.length][];
        for (int idx = 0; idx < original.length; idx++) {
            result[idx] = new String[original[idx].length];
            System.arraycopy(original[idx], 0, result[idx], 0, original[idx].length);
        }
        return result;
    }

    /**
     * Get all selected node paths as an array of Paths. Every Path is an Array of Strings from root to leaf,
     * including both
     * @return An Array of Paths in the form of String Arrays.
     */
    public String[][] getSelectedNodePaths() {
        return selectedNodePaths;
    }

    /**
     * Set the selected Node Paths as an array of Paths. Every Path is an Array of Strings from root to leaf,
     * including both
     * @param selectedNodePaths An Array of Paths in the form of String Arrays.
     */
    public void setSelectedNodePaths(final String[][] selectedNodePaths) {
        this.selectedNodePaths = deepArrayCopy(selectedNodePaths);
    }
    /**
     * The selected paths.
     */
    private String[][] selectedNodePaths;
}
