package de.logotakt.logolyze.view.interfaces;

import java.util.Collection;

/**
 * Interface to communicate with view components that display a tree of the hierarchies.
 */
public interface IHierarchyTreeView extends IErrorReporter {
    /**
     * Add a node to the tree.
     * @param parent The parent node, if null, than this is a root node
     * @param child The child that should be added to the tree
     * @param selectable Is this node selectable?
     * @param selected Is this node selected?
     */
    void addNode(Object parent, Object child, boolean selectable, boolean selected);

    /**
     * Clear the tree structure.
     */
    void clear();

    /**
     * Get all selected elements.
     * @return All selected elements
     */
    Collection<Object> getSelected();

    /**
     * Update the selection an element.
     * @param element The updated node
     * @param selected Should it be selected?
     */
    void setSelected(Object element, boolean selected);

    /**
     * Mark, that the tree update has been done.
     */
    void updateDone();
}
