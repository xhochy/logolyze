package de.logotakt.logolyze.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import de.logotakt.logolyze.model.interfaces.IConstraint;
import de.logotakt.logolyze.model.interfaces.ICube;
import de.logotakt.logolyze.model.interfaces.IDimension;
import de.logotakt.logolyze.model.interfaces.IHierarchy;
import de.logotakt.logolyze.model.interfaces.IHierarchyLevel;
import de.logotakt.logolyze.model.interfaces.IHierarchyLevelValue;
import de.logotakt.logolyze.view.interfaces.EventArgs;
import de.logotakt.logolyze.view.interfaces.IEventHandler;
import de.logotakt.logolyze.view.interfaces.IHierarchyTreeView;

/**
 * This is the sub-controller for managing a HierarchyTreeView. Its job is to handle the tree of hierarchies displayed
 * in the View. This controller handles the events treeLoad, treeNodeSelected.
 */
public class HierarchyTreeController implements IEventHandler {
	// This controller's superior Controller.
	private Controller controller;

	/**
	 * Initializes a new instance of HierarchyTreeController.
	 *
	 * @param controller This controller's superior controller.
	 */
	public HierarchyTreeController(final Controller controller) {
		this.controller = controller;
	}

	/**
	 * Dispatches the events this class can handle. HierarchyTreeController implements this from the IEventHandler
	 * interface. The HierarchyTreeController class accepts the following events: treeLoad, treeNodeSelected
	 * @param e The arguments for the current event.
	 */
	@Override
	public void event(final EventArgs e) {
		switch (e.getType()) {
		case treeLoad:
			handleTreeLoad(e);
			break;
		case treeNodeSelected:
			handleNodeSelected(e);
			break;
		default:
			throw new IllegalArgumentException("Wrong event was sent to ConnectionListController.");
		}
	}

	/* Handler functions for the individual events. */

	// This event is fired when the tree is first loaded.
	// A node is set up in the tree for each database element (Dimension, Hierarchy, etc.)
	private void handleTreeLoad(final EventArgs e) {
		IHierarchyTreeView htv = (IHierarchyTreeView) e.getDetails();
		ICube cube = controller.getSelectedCube();

		htv.clear();

		for (IDimension d : (Iterable<IDimension>) cube) {
			htv.addNode(null, d, false, false);
			for (IHierarchy h : (Iterable<IHierarchy>) d) {
				htv.addNode(d, h, false, false);
				for (IHierarchyLevel l : (Iterable<IHierarchyLevel>) h) {
					switch (d.getType()) {
					case iDimension:
						// Make the values in the level selectable.
						htv.addNode(h, l, false, false);
						for (IHierarchyLevelValue v : (Iterable<IHierarchyLevelValue>) l) {
							htv.addNode(l, v, true, false);
						}
						break;
					case tDimension:
						// Make the level directly selectable.
						htv.addNode(h, l, true, false);
						break;
					}
				}
			}
		}

		htv.updateDone();
	}

	// This event is fired when a node in the hierarchy tree has been selected or deselected.
	// Here, the newly (de)selected Node is searched and the constraints for this view are
	// updates appropriately. Conflicting previous selections are undone.
	// In addition, a graph update is done.
	private void handleNodeSelected(final EventArgs e) {
		IHierarchyTreeView htv = (IHierarchyTreeView) e.getDetails();
		ICube cube = controller.getSelectedCube();
		Collection<IConstraint> constraints  = controller.getConstraintsForView(htv);

		boolean constraintAdded = false;

		if (constraints == null) {
			constraints = new ArrayList<IConstraint>();
			controller.setConstraintsForView(htv, constraints);
		}

		for (IDimension d : (Iterable<IDimension>) cube) {
			for (IHierarchy h : (Iterable<IHierarchy>) d) {
				for (IHierarchyLevel l : (Iterable<IHierarchyLevel>) h) {

					switch (d.getType()) {
					case iDimension:
						for (IHierarchyLevelValue v : (Iterable<IHierarchyLevelValue>) l) {

							IConstraint nodeConstraint = null;
							for (IConstraint c : (Iterable<IConstraint>) constraints) {
								if (c.getValues().contains(v)
								    && c.getHierarchyLevel() == l
								    && c.getHierarchy() == h
								    && c.getDimension() == d) {
									nodeConstraint = c;
									break;
								}
							}

							if (htv.getSelected().contains(v) && nodeConstraint == null) {
								// Verify that only one Constraint was added
								if (constraintAdded) {
									throw new IllegalArgumentException(
									        "Two nodes in the hierarchy tree "
									      + "changed simultaneously.");
								}

								List<IHierarchyLevelValue> newValues =
									new ArrayList<IHierarchyLevelValue>(1);
								newValues.add(v);
								addConstraint(controller.getModel().getDataFactory().
									      makeConstraint(d, h, l, newValues),
									      constraints, htv);
								constraintAdded = true;

							} else if (!htv.getSelected().contains(v)
								   && nodeConstraint != null) {
								constraints.remove(nodeConstraint);
							}
						}
						break;

					case tDimension:
						IConstraint nodeConstraint = null;
						for (IConstraint c : (Iterable<IConstraint>) constraints) {
							if (c.getHierarchyLevel() == l
							    && c.getHierarchy() == h
							    && c.getDimension() == d) {
								nodeConstraint = c;
								break;
							}
						}

						if (htv.getSelected().contains(l) && nodeConstraint == null) {
							// Verify that only one Constraint was added
							if (constraintAdded) {
								throw new IllegalArgumentException(
								          "Two nodes in the hierarchy "
								        + "tree changed simultaneously.");
							}

							addConstraint(controller.getModel().getDataFactory().
								      makeConstraint(d, h, l,
										     java.util.Collections.EMPTY_LIST),
								      constraints, htv);
							constraintAdded = true;

						} else if (!htv.getSelected().contains(l) && nodeConstraint != null) {
							constraints.remove(nodeConstraint);
						}
					}
				}
			}
		}

		controller.displayNewGraphs(htv);
	}

	/* Adds the Constraint newConstraint to the Collection constraints, removing any
	   conflicting Constraints already in the Collection. */
	private void addConstraint(final IConstraint newConstraint, final Collection<IConstraint> constraints,
				   final IHierarchyTreeView htv) {
		Iterator<IConstraint> iter = constraints.iterator();
		while (iter.hasNext()) {
			IConstraint c = iter.next();

			switch (newConstraint.getDimension().getType()) {
			case iDimension:
				if (c.getHierarchy() == newConstraint.getHierarchy()) {
					htv.setSelected(c.getValues().iterator().next(), false);
					iter.remove();
				}
				break;
			case tDimension:
				if (c.getDimension() == newConstraint.getDimension()) {
					htv.setSelected(c.getHierarchyLevel(), false);
					iter.remove();
				}
				break;
			}
		}

		constraints.add(newConstraint);
	}

	/**
	 * Make this controller forget all its state.
	 */
	public void clean() { }
}
