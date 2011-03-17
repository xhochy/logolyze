package de.logotakt.logolyze.view.swing2d;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import de.logotakt.logolyze.view.interfaces.EventType;
import de.logotakt.logolyze.view.interfaces.IEventHandler;
import de.logotakt.logolyze.view.interfaces.IHierarchyTreeView;
import de.tinloaf.snbreadtree.SnBreadTree;
import de.tinloaf.snbreadtree.SnTreeModel;
import de.tinloaf.snbreadtree.SnTreeNode;

/**
 * The HierarchyTreeView allows the user to select a cube and entries in the hierarchies.
 */
public class HierarchyTreeView extends Swing2dEventedPanel implements IHierarchyTreeView, TreeSelectionListener,
        IStateful {
    private static final long serialVersionUID = 1891710340654462592L;
    protected static final EventType[] TRIGGERED_EVENTS = new EventType[] {EventType.cubeSelected,
            EventType.treeNodeSelected, EventType.treeLoad };

    private JComboBox cubesBox;
    private SnBreadTree hierarchyTree;
    private Object treeRoot;

    /**
     * Create a HierarchyTreeView inside the owner.
     */
    public HierarchyTreeView() {
        super();
        setupGui();

        for (EventType type : TRIGGERED_EVENTS) {
            getListeners().put(type, new HashSet<IEventHandler>());
        }
    }

    private void setupGui() {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {0 };
        gridBagLayout.rowHeights = new int[] {0, 0, 0 };
        gridBagLayout.columnWeights = new double[] {1.0 };
        gridBagLayout.rowWeights = new double[] {0.0, 0.0, 1.0 };
        setLayout(gridBagLayout);

        cubesBox = new JComboBox();
        cubesBox.setName("cubesBox");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 5, 0);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(cubesBox, gbc);

        // listen for new selections that are not empty (like when creating the box for the first time
        // and fire cubeSelected and treeLoad.
        cubesBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED && !e.getItem().equals("")) {
                    if (cubesBox.getItemAt(0).equals("")) {
                        cubesBox.removeItem("");
                    }
                    fireEvent(EventType.cubeSelected, (String) e.getItem());
                    fireEvent(EventType.treeLoad, HierarchyTreeView.this);
                }
            }
        });

        // force a minimum size for the hierarchy tree view.
        Component horizontalStrut = Box.createHorizontalStrut(150);
        GridBagConstraints gbcHorizontalStrut = new GridBagConstraints();
        gbcHorizontalStrut.insets = new Insets(0, 0, 5, 0);
        gbcHorizontalStrut.gridx = 0;
        gbcHorizontalStrut.gridy = 1;
        add(horizontalStrut, gbcHorizontalStrut);

        JScrollPane scrollPane = new JScrollPane();
        GridBagConstraints gbcScrollPane = new GridBagConstraints();
        gbcScrollPane.fill = GridBagConstraints.BOTH;
        gbcScrollPane.gridx = 0;
        gbcScrollPane.gridy = 2;
        add(scrollPane, gbcScrollPane);

        hierarchyTree = new SnBreadTree();
        scrollPane.setViewportView(hierarchyTree);
        hierarchyTree.setRootVisible(false);

        hierarchyTree.setNonleafsSelectable(false);
        hierarchyTree.setSize(50, 200);

        hierarchyTree.addTreeCheckingListener(this);
    }

    /**
     * Populate the cubes list with the names of cubes from the database.
     * @param names list of names of the cubes.
     */
    public void setCubesList(final List<String> names) {
        suspendEvents();
        cubesBox.removeAllItems();
        cubesBox.addItem("");
        for (String name : names) {
            cubesBox.addItem(name);
        }
        resumeEvents();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addNode(final Object parent, final Object child, final boolean selectable, final boolean selected) {
        Object effectiveParent = parent;
        if (effectiveParent == null) {
            effectiveParent = treeRoot;
        }

        suspendEvents();
        ((SnTreeModel) hierarchyTree.getModel()).addNode(effectiveParent, child);
        resumeEvents();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        SnTreeModel newModel = new SnTreeModel();
        hierarchyTree.setModel(newModel);
        treeRoot = new Object();
        newModel.addNode(null, treeRoot);
        hierarchyTree.addTreeCheckingListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Object> getSelected() {
        int selCt = hierarchyTree.getCheckedCount();
        List<Object> selections = new ArrayList<Object>(selCt);
        if (selCt != 0) {
            for (TreePath path : hierarchyTree.getCheckedPaths()) {
                selections.add(((SnTreeNode) path.getLastPathComponent()).getUserObject());
            }
        }
        return selections;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSelected(final Object element, final boolean selected) {
        SnTreeNode node = ((SnTreeModel) hierarchyTree.getModel()).getNodeByObject(element);
        TreePath path = new TreePath(node.getPath());
        suspendEvents();
        if (selected) {
            hierarchyTree.addCheckedPath(path);
        } else {
            hierarchyTree.removeCheckedPath(path);
        }
        resumeEvents();
        return;
    }

    /**
     * Get the name of the currently selected cube.
     * @return the name of the currently selected cube.
     */
    public String getSelectedCube() {
        return cubesBox.getSelectedItem().toString();
    }

    /**
     * Set the currently selected cube. Will fire an event.
     * @param selection the cube to select.
     */
    public void setSelectedCube(final String selection) {
        cubesBox.setSelectedItem(selection);
    }

    /**
     * The implementation of updateDone in this IHierarchyTreeView is a No-Op.
     */
    @Override
    public void updateDone() {
        // nothing to be done here.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void valueChanged(final TreeSelectionEvent e) {
        TreePath[] paths = e.getPaths();
        for (int index = 0; index < paths.length; index++) {
            fireEvent(EventType.treeNodeSelected, this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IState getState() {
        HierarchyTreeViewState state = new HierarchyTreeViewState();
        List<String[]> paths = new LinkedList<String[]>();
        TreePath[] checkedPaths = hierarchyTree.getCheckedPaths();
        if (checkedPaths != null) {
            for (TreePath path : checkedPaths) {
                String[] newPath = new String[path.getPathCount() - 1];
                for (int cmpIdx = 1; cmpIdx < path.getPathCount(); cmpIdx++) {
                    newPath[cmpIdx - 1] = path.getPathComponent(cmpIdx).toString();
                }
                paths.add(newPath);
            }
            state.setSelectedNodePaths(paths.toArray(new String[0][0]));
        } else {
            state.setSelectedNodePaths(new String[0][0]);
        }

        return state;
    }

    /**
     * This method recurses the JTree and turns a List<String> into a TreePath
     * @param start A TreePath to start at
     * @param path The rest of the Path to walk.
     * @return The final TreePath that corresponds to path.
     */
    private TreePath walkPath(final TreePath start, final List<String> path) {
        SnTreeNode cursor = (SnTreeNode) start.getLastPathComponent();
        for (int childIdx = 0; childIdx < cursor.getChildCount(); childIdx++) {
            if (cursor.getChildAt(childIdx).toString().equals(path.get(0))) {
                if (path.size() == 1) {
                    return start.pathByAddingChild(cursor.getChildAt(childIdx));
                } else {
                    List<String> restPath = new LinkedList<String>(path);
                    restPath.remove(0);
                    return walkPath(start.pathByAddingChild(cursor.getChildAt(childIdx)), restPath);
                }
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("BC_UNCONFIRMED_CAST")
    public void setState(final IState state) {
        Object[] pathObj = new Object[1];
        pathObj[0] = hierarchyTree.getModel().getRoot();
        TreePath rootPath = new TreePath(pathObj);
        hierarchyTree.setCheckedPaths(new TreePath[0]);
        for (String[] path : ((HierarchyTreeViewState) state).getSelectedNodePaths()) {
            List<String> pathList = new ArrayList<String>(Arrays.asList(path));
            TreePath pathToAdd = walkPath(rootPath, pathList);
            if (pathToAdd == null) {
                displayError("The saved State is not compatible with this database.");
            }
            hierarchyTree.addCheckedPath(pathToAdd);
            hierarchyTree.expandPath(pathToAdd);
            forceEvent(EventType.treeNodeSelected, this);
        }
    }

    void shutdown() {
        suspendEvents();
        setVisible(false);
    }

    /**
     * Gets the {@link HierarchyTreeView} in a fresh state.
     */
    public void getFresh() {
        clear();
        fireEvent(EventType.treeLoad, HierarchyTreeView.this);
    }
}
