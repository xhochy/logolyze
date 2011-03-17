package de.logotakt.logolyze.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import de.logotakt.logolyze.model.interfaces.DimensionType;
import de.logotakt.logolyze.model.interfaces.IConstraint;
import de.logotakt.logolyze.model.interfaces.ICube;
import de.logotakt.logolyze.model.interfaces.IDimension;
import de.logotakt.logolyze.model.interfaces.IHierarchy;
import de.logotakt.logolyze.model.interfaces.IHierarchyLevel;
import de.logotakt.logolyze.model.interfaces.IHierarchyLevelValue;
import de.logotakt.logolyze.model.interfaces.IModelDataFactory;
import de.logotakt.logolyze.view.interfaces.EventArgs;
import de.logotakt.logolyze.view.interfaces.IAxisConfigurationView;
import de.logotakt.logolyze.view.interfaces.IEventHandler;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * This is the sub-controller for managing an IAxisConfigurationView. Its job is to handle the configuration of the
 * GraphPanel's axes. This controller handles the events axisConfigShowing, axisConfigChanged, axisConfigDone.
 */
public class AxisConfigurationController implements IEventHandler {
	private static Logger logger = Logger.getLogger(AxisConfigurationController.class);

	// The text that appears in the HierarchyLevel slot, if the dimension is topological.
	private static final String TOPOLOGY_FILLER = "<TOPOLOGY>";

	// The item to select in the Dimension slot to mean "no filtering".
	private static final String NOTHING = "<none>";

	/**
	 * This class acts as a HierarchyLevel. An object of this class is stored in currentHierarchyLevel when a
	 * t-Dimension is selected.
	 */
	private class TopologyFillLevel implements IHierarchyLevel {
		private static final String PLACEHOLDER_ERROR = "This is only a placeholder";

		TopologyFillLevel() {
		}

		public String getName() {
			return TOPOLOGY_FILLER;
		}

		public Collection<IHierarchyLevelValue> getValues() {
			throw new UnsupportedOperationException(PLACEHOLDER_ERROR);
		}

		public IHierarchyLevel parentLevel() {
			throw new UnsupportedOperationException(PLACEHOLDER_ERROR);
		}

		public IHierarchyLevel childLevel() {
			throw new UnsupportedOperationException(PLACEHOLDER_ERROR);
		}

		public Iterator<IHierarchyLevelValue> iterator() {
			throw new UnsupportedOperationException(PLACEHOLDER_ERROR);
		}
	}

	/*
	 * Due to the way Constraints on t-Dimensions are represented, this Controller actually has to change its behaviour
	 * quite drastically depending on the type of the current dimension. This variable controls this Controller's
	 * behavior in that respect.
	 */
	private int operatingMode = 0;
	private static final int DONT_KNOW = 0;
	private static final int I_DIMENSION = 1;
	private static final int T_DIMENSION = 2;

	/*
	 * To simplify the detection of changed constraints, the previous selections are saved here.
	 */
	private IDimension currentDimension = null;
	private IHierarchy currentHierarchy = null;
	private IHierarchyLevel currentHierarchyLevel = null;
	private List<IHierarchyLevel> currentTopologyLevels = null;
	private List<IHierarchyLevelValue> currentValues = null;

	// This controller's superior Controller.
	private Controller controller;

	/**
	 * Initializes a new instance of AxisConfigurationController.
	 * @param controller This controller's superior controller.
	 */
	public AxisConfigurationController(final Controller controller) {
		this.controller = controller;
	}

	/**
	 * Dispatches the events this class can handle. AxisConfigurationController implements this from the IEventHandler
	 * interface. The AxisConfigurationController class accepts the following events: axisConfigShowing,
	 * axisConfigChanged, axisConfigDone
	 * @param e The arguments for the current event.
	 */
	@Override
	public void event(final EventArgs e) {
		switch (e.getType()) {
		case axisConfigShowing:
			handleConfigShowing(e);
			break;
		case axisConfigChanged:
			handleConfigChanged(e);
			break;
		case axisConfigDone:
			handleConfigDone(e);
			break;
		default:
			throw new IllegalArgumentException("Wrong event was sent to ConnectionListController.");
		}
	}

	/**********************************************************************/
	/* EVENT HANDLERS */
	/**********************************************************************/

	// This event is fired when the dialog is first shown.
	// Here, we fill in the slots with the constraint for this dialog, if one exists.
	private void handleConfigShowing(final EventArgs e) {
		logger.debug("ConfigShowing event received");
	    
		IAxisConfigurationView acv = (IAxisConfigurationView) e.getDetails();

		// Before possibly making a new constraint, make sure there is
		// already a cube selected.
		if (controller.getSelectedCube() == null) {
			acv.displayError("Cannot configure an axis unless there is a Cube selected.");
			logger.debug("User error: No cube was selected.");
			return;
		}

		Collection<IConstraint> allConstraints = controller.getConstraintsForView(acv);

		if (allConstraints == null || allConstraints.size() == 0) {
			logger.debug("No Constraints present.");

			fillEmptyForm(acv);
		} else {
			logger.debug("Constraints present.");
			
			IConstraint first = allConstraints.iterator().next();

			if (first.getDimension().getType() == DimensionType.iDimension) {
				if (allConstraints.size() != 1) {
					throw new IllegalArgumentException("More than one Constraint for I-Dimension");
				} else {
					logger.debug("Operating mode: i-Dimension");
					operatingMode = I_DIMENSION;
					iFillWithConstraint(first, acv);
				}
			} else {
				logger.debug("Operating mode: t-Dimension");
				operatingMode = T_DIMENSION;
				tFillWithConstraints(allConstraints, acv);
			}
		}
	}

	// This event is fired on every change to the selections the user made.
	// If necessary, the lists of available Hierarchies etc. are updated.
	private void handleConfigChanged(final EventArgs e) {
		logger.debug("ConfigChanged event received");

		IAxisConfigurationView acv = (IAxisConfigurationView) e.getDetails();

		// If in any of the following tests, one of the current... variables
		// is found to be null, this was the one that was changed:
		// - If the change occured in a higher level, it has been checked before.
		// - The change cannot occur in a lower level, if the level in question
		// has not been set.
		if (currentDimension == null || !acv.getSelectedDimension().equals(currentDimension.getName())) {
			handleDimensionChange(acv);
		} else if (currentHierarchy == null || !acv.getSelectedHierarchy().equals(currentHierarchy.getName())) {
			handleHierarchyChange(acv);
		} else if (currentHierarchyLevel == null
			   || !acv.getSelectedHierarchyLevel().equals(currentHierarchyLevel.getName())) {
			handleHierarchyLevelChange(acv);
		} else if (currentValues == null || valuesChanged(acv.getSelectedValues())) {
			handleValueChange(acv);
		} else {
			// Nothing's actually changed. Thats not a big problem.
			logger.warn("Spurious ConfigChanged event");
		}
	}

	// This event is fired when the dialog is closed.
	// The new Constraint (if not empty) is written back to the main controller.
	private void handleConfigDone(final EventArgs e) {
		logger.debug("ConfigDone event received");

		IAxisConfigurationView acv = (IAxisConfigurationView) e.getDetails();

		if (currentDimension == null) {
			emptyHandleConfigDone(acv);
		} else if (operatingMode == I_DIMENSION) {
			iHandleConfigDone(acv);
		} else if (operatingMode == T_DIMENSION) {
			tHandleConfigDone(acv);
		} else {
			throw new IllegalArgumentException(
				"Don't know operating mode of AxisConfigurationController in handleConfigDone.");
		}

		controller.displayNewGraphs(acv);
	}

	/**********************************************************************/
	/* INVARIANT IMPLEMENTATIONS / DISPATCHERS */
	/**********************************************************************/

	// Fill the dimension slot of an empty form, waiting for user input.
	private void fillEmptyForm(final IAxisConfigurationView acv) {
		logger.debug("Filling empty form.");
		
		ICube cube = controller.getSelectedCube();

		List<String> dimensionNames = new ArrayList<String>();
		// First, add a selection for "nothing".
		dimensionNames.add(NOTHING);
		for (IDimension dim : cube) {
			dimensionNames.add(dim.getName());
		}
		acv.setDimensions(dimensionNames);
		acv.setSelectedDimension(NOTHING);
		currentDimension = null;
		// There may be leftover parameters from a previously cancelled invocation
		clearFrom(HIERARCHY, acv);
	}

	// Fills those slots with data from the given Constraint, which are to be treated the same,
	// whether the selected dimension is an i- or t-Dimension.
	private void fillInvariantSlotsWithConstraint(final IConstraint cons, final IAxisConfigurationView acv) {
		ICube cube = controller.getSelectedCube();

		List<String> dimensionNames = new ArrayList<String>();
		dimensionNames.add(NOTHING);
		for (IDimension dim : (Iterable<IDimension>) cube) {
			dimensionNames.add(dim.getName());
		}
		acv.setDimensions(dimensionNames);
		acv.setSelectedDimension(cons.getDimension().getName());
		currentDimension = cons.getDimension();

		List<String> hierarchyNames = new ArrayList<String>();
		for (IHierarchy hier : cons.getDimension()) {
			hierarchyNames.add(hier.getName());
		}
		acv.setHierarchies(hierarchyNames);
		acv.setSelectedHierarchy(cons.getHierarchy().getName());
		currentHierarchy = cons.getHierarchy();
	}

	private static final int HIERARCHY = 0;
	private static final int HIERARCHY_LEVEL = 1;
	private static final int HIERARCHY_LEVEL_VALUE = 2;

	/**
	 * Clears the view's slots and the current... members starting at the given level. It is intended that the cases are
	 * falling through.
	 */
	@SuppressWarnings(value = "SF_SWITCH_FALLTHROUGH")
	private void clearFrom(final int level, final IAxisConfigurationView acv) {
		switch (level) {
		case HIERARCHY:
			acv.setHierarchies(null);
			currentHierarchy = null;
		case HIERARCHY_LEVEL:
			acv.setHierarchyLevels(null);
			currentHierarchyLevel = null;
		case HIERARCHY_LEVEL_VALUE:
			acv.setValues(null);
			currentValues = null;
			currentTopologyLevels = null;
			break;
		default:
			throw new IllegalStateException("Internal logic error in AxisConfigurationController.clearFrom");
		}
	}

	// Handles a change of the Dimension selection by loading the appropriate
	// hierarchies and setting the operating mode to the Dimension's type.
	private void handleDimensionChange(final IAxisConfigurationView acv) {
		logger.debug("Dimension changed");
		
		ICube cube = controller.getSelectedCube();
		String newName = acv.getSelectedDimension();
		currentDimension = null;

		if (newName.equals(NOTHING)) {
			operatingMode = DONT_KNOW;
			clearFrom(HIERARCHY, acv);
		} else {
			for (IDimension dim : (Iterable<IDimension>) cube) {
				if (dim.getName().equals(newName)) {
					currentDimension = dim;
					break;
				}
			}
			if (currentDimension == null) {
				throw new IllegalStateException("A Dimension suddenly disappeared");
			}

			if (currentDimension.getType() == DimensionType.iDimension) {
				logger.debug("Operating mode: i-Dimension");
				operatingMode = I_DIMENSION;
			} else {
				logger.debug("Operating mode: t-Dimension");
				operatingMode = T_DIMENSION;
			}

			List<String> hierarchyNames = new ArrayList<String>();
			for (IHierarchy h : (Iterable<IHierarchy>) currentDimension) {
				hierarchyNames.add(h.getName());
			}
			acv.setHierarchies(hierarchyNames);

			clearFrom(HIERARCHY_LEVEL, acv);
		}
	}

	/*
	 * The following methods only dispatch to the i- or t-dimension implementations.
	 */

	private void handleHierarchyChange(final IAxisConfigurationView acv) {
		logger.debug("Hierarchy changed");

		if (operatingMode == I_DIMENSION) {
			iHandleHierarchyChange(acv);
		} else if (operatingMode == T_DIMENSION) {
			tHandleHierarchyChange(acv);
		} else {
			throw new IllegalStateException(
				"Don't know operating mode of AxisConfigurationController in handleHierarchyChange.");
		}
	}

	private void handleHierarchyLevelChange(final IAxisConfigurationView acv) {
		logger.debug("HierarchyLevel changed");

		if (operatingMode == I_DIMENSION) {
			iHandleHierarchyLevelChange(acv);
		} else if (operatingMode == T_DIMENSION) {
			// Allow changing hierarchyLevel to the placeholder.
			throw new IllegalArgumentException("Attempted to change hierarchy level in a t-Dimension.");
		} else {
			throw new IllegalArgumentException("Don't know operating mode of AxisConfigurationController in"
							   + "handleHierarchyLevelChange.");
		}
	}

	private void handleValueChange(final IAxisConfigurationView acv) {
		logger.debug("HierarchyLevelValue changed");

		if (operatingMode == I_DIMENSION) {
			iHandleValueChange(acv);
		} else if (operatingMode == T_DIMENSION) {
			tHandleValueChange(acv);
		} else {
			throw new UnsupportedOperationException("Don't know operating mode of AxisConfigurationController in "
								+ "handleValueChange.");
		}
	}

	// Handles a configDone event when the user has selected not to filter anything.
	private void emptyHandleConfigDone(final IAxisConfigurationView acv) {
		logger.debug("ConfigDone - no Constraints");
		// We already know that the user selected nothing to filter.
		controller.setConstraintsForView(acv, null);
	}

	/**********************************************************************/
	/* I-DIMENSION IMPLEMENTATIONS */
	/**********************************************************************/

	// Fills these slots with data from a constraint, which are specific to i-Dimensions.
	private void iFillWithConstraint(final IConstraint cons, final IAxisConfigurationView acv) {
		fillInvariantSlotsWithConstraint(cons, acv);

		List<String> levelNames = new ArrayList<String>();
		for (IHierarchyLevel level : cons.getHierarchy()) {
			levelNames.add(level.getName());
		}
		acv.setHierarchyLevels(levelNames);
		acv.setSelectedHierarchyLevel(cons.getHierarchyLevel().getName());
		currentHierarchyLevel = cons.getHierarchyLevel();

		List<String> valueNames = new ArrayList<String>();
		for (IHierarchyLevelValue val : (Iterable<IHierarchyLevelValue>) cons.getHierarchyLevel()) {
			valueNames.add(val.getValue());
		}
		acv.setValues(valueNames);
		List<String> selectedValues = new ArrayList<String>();
		for (IHierarchyLevelValue val : cons.getValues()) {
			selectedValues.add(val.getValue());
		}
		acv.setSelectedValues(selectedValues);
		currentValues = new ArrayList<IHierarchyLevelValue>(cons.getValues());
	}

	// Handles a change in Hierarchy selection in an i-dimension by loading the appropriate HierarchyLevels.
	private void iHandleHierarchyChange(final IAxisConfigurationView acv) {
		String newName = acv.getSelectedHierarchy();
		currentHierarchy = null;

		for (IHierarchy hier : (Iterable<IHierarchy>) currentDimension) {
			if (hier.getName().equals(newName)) {
				currentHierarchy = hier;
				break;
			}
		}
		if (currentHierarchy == null) {
			throw new IllegalStateException("Hierarchy suddenly disappeared");
		}

		List<String> levelNames = new ArrayList<String>();
		for (IHierarchyLevel level : (Iterable<IHierarchyLevel>) currentHierarchy) {
			levelNames.add(level.getName());
		}
		acv.setHierarchyLevels(levelNames);

		clearFrom(HIERARCHY_LEVEL_VALUE, acv);
	}

	// Handles a change in HierarchyLevel selection in an i-dimension by loading the appropriate
	// HierarchyLevelValues.
	private void iHandleHierarchyLevelChange(final IAxisConfigurationView acv) {
		String newName = acv.getSelectedHierarchyLevel();
		currentHierarchyLevel = null;

		for (IHierarchyLevel level : (Iterable<IHierarchyLevel>) currentHierarchy) {
			if (level.getName().equals(newName)) {
				currentHierarchyLevel = level;
				break;
			}
		}
		if (currentHierarchyLevel == null) {
			throw new IllegalStateException("HierarchyLevel suddenly disappeared");
		}

		List<String> valueNames = new ArrayList<String>();
		for (IHierarchyLevelValue value : (Iterable<IHierarchyLevelValue>) currentHierarchyLevel) {
			valueNames.add(value.getValue());
		}
		acv.setValues(valueNames);
	}

	// Handles a change in HierarchyLevelValue selection in an i-Dimension.
	private void iHandleValueChange(final IAxisConfigurationView acv) {
		currentValues = new ArrayList<IHierarchyLevelValue>(acv.getSelectedValues().size());

		for (String name : acv.getSelectedValues()) {
			for (IHierarchyLevelValue value : (Iterable<IHierarchyLevelValue>) currentHierarchyLevel) {
				if (value.getValue().equals(name)) {
					currentValues.add(value);
					break;
				}
			}
		}
		if (currentValues.size() != acv.getSelectedValues().size()) {
			throw new IllegalStateException("HierarchyLevelValue suddenly disappeared");
		}
	}

	// Handles the configDone event when an i-dimension is selected.
	// This adds exactly one constraint for acv to the Controller.
	private void iHandleConfigDone(final IAxisConfigurationView acv) {
		// If at all, all values must be selected.
		if (currentHierarchy == null || currentHierarchyLevel == null || currentValues == null) {
			acv.displayError("Please fill in all fields, or select the empty entry in the Dimension "
					 + "field to disable filtering.");
			return;
		}

		Collection<IConstraint> theConstraint = new ArrayList<IConstraint>(1);
		theConstraint.add(controller.getModel().getDataFactory()
				  .makeConstraint(currentDimension, currentHierarchy, currentHierarchyLevel, currentValues));
		controller.setConstraintsForView(acv, theConstraint);
	}

	/**********************************************************************/
	/* T-DIMENSION IMPLEMENTATIONS */
	/**********************************************************************/

	// Fills these slots with data from a constraint, which are specific to i-Dimensions.
	private void tFillWithConstraints(final Collection<IConstraint> cons, final IAxisConfigurationView acv) {
		IConstraint first = cons.iterator().next();
		fillInvariantSlotsWithConstraint(first, acv);

		List<String> levelFiller = new ArrayList<String>(1);
		levelFiller.add(TOPOLOGY_FILLER);
		acv.setHierarchyLevels(levelFiller);
		acv.setSelectedHierarchyLevel(TOPOLOGY_FILLER);
		currentHierarchyLevel = new TopologyFillLevel();

		// For t-Dimensions, levels act as values.
		List<String> levelNames = new ArrayList<String>();
		for (IHierarchyLevel level : (Iterable<IHierarchyLevel>) first.getHierarchy()) {
			levelNames.add(level.getName());
		}
		acv.setValues(levelNames);

		List<String> selectedLevels = new ArrayList<String>();
		currentTopologyLevels = new ArrayList<IHierarchyLevel>();
		for (IConstraint c : (Iterable<IConstraint>) cons) {
			selectedLevels.add(c.getHierarchyLevel().getName());
			currentTopologyLevels.add(c.getHierarchyLevel());
		}
		acv.setSelectedValues(selectedLevels);

	}

	// Handles a change in Hierarchy selection in a t-Dimension by displaying a filler
	// in the HierarchyLevelSlot and loading the appropriate HierarchyLevels, which are to
	// be displayed as values.
	private void tHandleHierarchyChange(final IAxisConfigurationView acv) {
		String newName = acv.getSelectedHierarchy();
		currentHierarchy = null;

		for (IHierarchy hier : (Iterable<IHierarchy>) currentDimension) {
			if (hier.getName().equals(newName)) {
				currentHierarchy = hier;
				break;
			}
		}
		if (currentHierarchy == null) {
			throw new IllegalStateException("Hierarchy suddenly disappeared");
		}

		List<String> levelFiller = new ArrayList<String>(1);
		levelFiller.add(TOPOLOGY_FILLER);
		acv.setHierarchyLevels(levelFiller);
		acv.setSelectedHierarchyLevel(TOPOLOGY_FILLER);
		currentHierarchyLevel = new TopologyFillLevel();

		List<String> levelNames = new ArrayList<String>();
		for (IHierarchyLevel level : (Iterable<IHierarchyLevel>) currentHierarchy) {
			levelNames.add(level.getName());
		}
		acv.setValues(levelNames);
	}

	// NOTE: There is nothing to handle a change in HierarchyLevel here. In t-dimensions,
	// HierarchyLevels are treated as values (see next method).

	// Handles a change in values (i.e. HierarchyLevels) in a t-Dimension.
	private void tHandleValueChange(final IAxisConfigurationView acv) {
		currentTopologyLevels = new ArrayList<IHierarchyLevel>(acv.getSelectedValues().size());

		for (String name : acv.getSelectedValues()) {
			for (IHierarchyLevel level : (Iterable<IHierarchyLevel>) currentHierarchy) {
				if (level.getName().equals(name)) {
					currentTopologyLevels.add(level);
					break;
				}
			}
		}
		if (currentTopologyLevels.size() != acv.getSelectedValues().size()) {
			throw new IllegalStateException("HierarchyLevel suddenly disappeared");
		}

	}

	// Handles the configDone event when a t-Dimension is selected.
	// This adds one constraint for acv to the Controller for each selected value (i.e. HierarchyLevel).
	private void tHandleConfigDone(final IAxisConfigurationView acv) {
		// If at all, all values must be selected.
		if (currentHierarchy == null || currentTopologyLevels == null) {
			acv.displayError("Please fill in all fields, or select the empty entry in the Dimension "
					 + "field to disable filtering.");
			return;
		}

		IModelDataFactory factory = controller.getModel().getDataFactory();
		Collection<IConstraint> constraints = new ArrayList<IConstraint>(currentTopologyLevels.size());

		for (IHierarchyLevel level : currentTopologyLevels) {
			constraints.add(factory.makeConstraint(currentDimension, currentHierarchy, level,
							       java.util.Collections.EMPTY_LIST));
		}

		controller.setConstraintsForView(acv, constraints);
	}

	// Returns true, if both collections consist of pairwise equal elements.
	private boolean equalElements(final Collection c1, final Collection c2) {
		return c1.containsAll(c2) && c2.containsAll(c1);
	}

	// Returns true if the given values are different from those stored in currentValues.
	private boolean valuesChanged(final List<String> valueNames) {
		List<String> currentValueNames = new ArrayList<String>(currentValues.size());
		for (IHierarchyLevelValue value : currentValues) {
			currentValueNames.add(value.getValue());
		}

		return !equalElements(valueNames, currentValueNames);
	}

	/**
	 * Make this controller forget all its state.
	 */
	void clean() {
		logger.debug("AxisConfigurationController cleaning itself.");
		
		currentDimension = null;
		currentHierarchy = null;
		currentHierarchyLevel = null;
		currentTopologyLevels = null;
		currentValues = null;
	}
}
