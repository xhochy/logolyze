package de.logotakt.logolyze.controller;

import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.logotakt.logolyze.model.interfaces.IOLAPGraph;
import de.logotakt.logolyze.view.interfaces.EventType;
import de.logotakt.logolyze.view.interfaces.IAxisConfigurationView;
import de.logotakt.logolyze.view.interfaces.ILogolyzeView;

/**
 * Tests the axis* Events of the controller.
 */
public class ControllerAxisTest extends ControllerTestTemplate {
	@Inject
	@Named("logolyze view")
	private ILogolyzeView view;
	@Inject
	@Named("axis configuration view")
	private IAxisConfigurationView axisConfigView;

	/**
	 * Tests if it is impossible to configure an axis without selecting a cube.
	 */
	@Test
	public void axisConfigShowingCubeless() {
		// Let the controller do some work.
		fireEvent(axisConfigView, EventType.axisConfigShowing, axisConfigView);

		// If we haven't selected a cube it shouldn't be possible to configure an axis.
		verify(axisConfigView).displayError(anyString());
	}

	/**
	 * Tests the reaction of the Controller to the axisConfigShowing event with nothing selected.
	 */
	@Test
	public void axisConfigShowingStateless() {
		// We need to select the cube as state for the controller.
		fireEvent(view, EventType.cubeSelected, Constants.SELECTED_CUBE);

		// Fill the dialog with nothing
		when(axisConfigView.getSelectedDimension()).thenReturn(null);
		when(axisConfigView.getSelectedHierarchy()).thenReturn(null);
		when(axisConfigView.getSelectedHierarchyLevel()).thenReturn(null);
		when(axisConfigView.getSelectedValues()).thenReturn(new ArrayList<String>());

		// Let the controller do some work.
		fireEvent(axisConfigView, EventType.axisConfigShowing, axisConfigView);

		// The controller should have pushed us the list of dimensions.
		verify(axisConfigView, atLeastOnce()).setDimensions(anyListOf(String.class));
		// There should never occur an error.
		verify(axisConfigView, never()).displayError(anyString());
	}

	/**
	 * Tests the reaction of the Controller to the axisConfigShowing event with a constraint already present.
	 */
	@Test
	public void axisConfigShowingWithIState() {
		// Simulate a previous configuration
		axisConfigDone();

		// Fill the dialog with nothing
		when(axisConfigView.getSelectedDimension()).thenReturn(null);
		when(axisConfigView.getSelectedHierarchy()).thenReturn(null);
		when(axisConfigView.getSelectedHierarchyLevel()).thenReturn(null);
		when(axisConfigView.getSelectedValues()).thenReturn(new ArrayList<String>());

		// Call the controller to fill in the dialog
		fireEvent(axisConfigView, EventType.axisConfigShowing, axisConfigView);

		// The controller should have filled in all fields
		verify(axisConfigView, atLeastOnce()).setDimensions(anyListOf(String.class));
		verify(axisConfigView, atLeastOnce()).setHierarchies(anyListOf(String.class));
		verify(axisConfigView, atLeastOnce()).setHierarchyLevels(anyListOf(String.class));
		verify(axisConfigView, atLeastOnce()).setValues(anyListOf(String.class));

		// No error should have happened
		verify(axisConfigView, never()).displayError(anyString());
	}

	/**
	 * Tests the reaction of the Controller to the axisConfigChanged event with a dimension selected.
	 */
	@Test
	public void axisConfigChangedTDimension() {
		// Push state
		axisConfigShowingStateless();

		// Fill the dialog with a selected dimension and nothing else.
		when(axisConfigView.getSelectedDimension()).thenReturn(Constants.DBS_LOC);
		when(axisConfigView.getSelectedHierarchy()).thenReturn(null);
		when(axisConfigView.getSelectedHierarchyLevel()).thenReturn(null);
		when(axisConfigView.getSelectedValues()).thenReturn(new ArrayList<String>());

		// Let the controller do some work.
		fireEvent(axisConfigView, EventType.axisConfigChanged, axisConfigView);

		// If the controller does not ask us which entries are selected, we does he get his knowledge from?
		verify(axisConfigView, atLeastOnce()).getSelectedDimension();
		verify(axisConfigView, atLeastOnce()).setHierarchies(anyListOf(String.class));
		// There should never occur an error.
		verify(axisConfigView, never()).displayError(anyString());
	}

	/**
	 * Tests the reaction of the Controller to the axisConfigChanged event with a dimension and a hierarchy selected.
	 */
	@Test
	public void axisConfigChangedTDimensionHierarchy() {
		// Push state
		axisConfigChangedTDimension();

		// Fill the dialog with a selected dimension and hierarchy.
		when(axisConfigView.getSelectedDimension()).thenReturn(Constants.DBS_LOC);
		when(axisConfigView.getSelectedHierarchy()).thenReturn(Constants.DBS_HUB);
		when(axisConfigView.getSelectedHierarchyLevel()).thenReturn(null);
		when(axisConfigView.getSelectedValues()).thenReturn(new ArrayList<String>());

		// Let the controller do some work.
		fireEvent(axisConfigView, EventType.axisConfigChanged, axisConfigView);

		// If the controller does not ask us which entries are selected, we does he get his knowledge from?
		verify(axisConfigView, atLeastOnce()).getSelectedHierarchy();
		verify(axisConfigView, atLeastOnce()).setHierarchyLevels(anyListOf(String.class));
		verify(axisConfigView, atLeastOnce()).setValues(anyListOf(String.class));
		// There should never occur an error.
		verify(axisConfigView, never()).displayError(anyString());
	}

	/**
	 * Tests the reaction of the Controller to the axisConfigChanged event with a dimension and a hierarchy selected.
	 */
	@Test
	public void axisConfigChangedTDimensionHierarchyValues() {
		// Push state
		axisConfigChangedTDimensionHierarchy();

		// Fill the dialog with a selected dimension and hierarchy.
		when(axisConfigView.getSelectedDimension()).thenReturn(Constants.DBS_LOC);
		when(axisConfigView.getSelectedHierarchy()).thenReturn(Constants.DBS_HUB);
		when(axisConfigView.getSelectedHierarchyLevel()).thenReturn("<TOPOLOGY>");
		List<String> values = new ArrayList<String>();
		values.add(Constants.DBS_RAIL);
		when(axisConfigView.getSelectedValues()).thenReturn(values);

		// Let the controller do some work.
		fireEvent(axisConfigView, EventType.axisConfigChanged, axisConfigView);

		// If the controller does not ask us which entries are selected, we does he get his knowledge from?
		verify(axisConfigView, atLeastOnce()).getSelectedValues();
		// There should never occur an error.
		verify(axisConfigView, never()).displayError(anyString());
	}

	/**
	 * Tests the reaction of the Controller to the axisConfigDone event.
	 */
	@Test
	public void axisConfigTDone() {
		// Push state, the dialog will also be filled in this methods.
		axisConfigChangedTDimensionHierarchyValues();

		// Let the controller do some work.
		fireEvent(axisConfigView, EventType.axisConfigDone, axisConfigView);

		// Now we should be supplied with some graphs.
		verify(view).setGraphs(anyCollectionOf(IOLAPGraph.class));
		// There should never occur an error.
		verify(axisConfigView, never()).displayError(anyString());
	}

	/**
	 * Tests the reaction of the Controller to the axisConfigChanged event with a dimension selected.
	 */
	@Test
	public void axisConfigChangedDimension() {
		// Push state
		axisConfigShowingStateless();

		// Fill the dialog with a selected dimension and nothing else.
		when(axisConfigView.getSelectedDimension()).thenReturn(Constants.DBS_TIM);
		when(axisConfigView.getSelectedHierarchy()).thenReturn(null);
		when(axisConfigView.getSelectedHierarchyLevel()).thenReturn(null);
		when(axisConfigView.getSelectedValues()).thenReturn(new ArrayList<String>());

		// Let the controller do some work.
		fireEvent(axisConfigView, EventType.axisConfigChanged, axisConfigView);

		// If the controller does not ask us which entries are selected, we does he get his knowledge from?
		verify(axisConfigView, atLeastOnce()).getSelectedDimension();
		verify(axisConfigView, atLeastOnce()).setHierarchies(anyListOf(String.class));
		// There should never occur an error.
		verify(axisConfigView, never()).displayError(anyString());
	}

	/**
	 * Tests the reaction of the Controller to the axisConfigChanged event with a dimension and a hierarchy selected.
	 */
	@Test
	public void axisConfigChangedDimensionHierarchy() {
		// Push state
		axisConfigChangedDimension();

		// Fill the dialog with a selected dimension and hierarchy.
		when(axisConfigView.getSelectedDimension()).thenReturn(Constants.DBS_TIM);
		when(axisConfigView.getSelectedHierarchy()).thenReturn(Constants.DBS_TIM_W);
		when(axisConfigView.getSelectedHierarchyLevel()).thenReturn(null);
		when(axisConfigView.getSelectedValues()).thenReturn(new ArrayList<String>());

		// Let the controller do some work.
		fireEvent(axisConfigView, EventType.axisConfigChanged, axisConfigView);

		// If the controller does not ask us which entries are selected, we does he get his knowledge from?
		verify(axisConfigView, atLeastOnce()).getSelectedHierarchy();
		verify(axisConfigView, atLeastOnce()).setHierarchyLevels(anyListOf(String.class));
		// There should never occur an error.
		verify(axisConfigView, never()).displayError(anyString());
	}

	/**
	 * Tests the reaction of the Controller to the axisConfigChanged event with a dimension, a hierarchy and a
	 * hierarchylevel selected.
	 */
	@Test
	public void axisConfigChangedDimensionHierarchyHierarchyLevel() {
		// Push state
		axisConfigShowingStateless();
		// Make the changes one by one
		axisConfigChangedDimensionHierarchy();

		// Fill the dialog with dimension, hierarchy and hierarchy level.
		when(axisConfigView.getSelectedDimension()).thenReturn(Constants.DBS_TIM);
		when(axisConfigView.getSelectedHierarchy()).thenReturn(Constants.DBS_TIM_W);
		when(axisConfigView.getSelectedHierarchyLevel()).thenReturn(Constants.DBS_DAY);
		when(axisConfigView.getSelectedValues()).thenReturn(new ArrayList<String>());

		// Let the controller do some work.
		fireEvent(axisConfigView, EventType.axisConfigChanged, axisConfigView);

		// If the controller does not ask us which entries are selected, we does he get his knowledge from?
		verify(axisConfigView, atLeastOnce()).getSelectedHierarchyLevel();
		verify(axisConfigView, atLeastOnce()).setValues(anyListOf(String.class));
		// There should never occur an error.
		verify(axisConfigView, never()).displayError(anyString());
	}

	/**
	 * Tests the reaction of the Controller to the axisConfigChanged event with a dimension, a hierarchy, a
	 * hierarchyLevel and values selected.
	 */
	@Test
	public void axisConfigChangedDimensionHierarchyHierarchyLevelValues() {
		// Push state
		axisConfigChangedDimensionHierarchyHierarchyLevel();

		// Fill all fields of the dialog.
		when(axisConfigView.getSelectedDimension()).thenReturn(Constants.DBS_TIM);
		when(axisConfigView.getSelectedHierarchy()).thenReturn(Constants.DBS_TIM_W);
		when(axisConfigView.getSelectedHierarchyLevel()).thenReturn(Constants.DBS_DAY);
		List<String> list = new ArrayList<String>();
		list.add(Constants.DBS_12);
		list.add(Constants.DBS_13);
		when(axisConfigView.getSelectedValues()).thenReturn(list);

		// Let the controller do some work.
		fireEvent(axisConfigView, EventType.axisConfigChanged, axisConfigView);

		// If the controller does not ask us which entries are selected, we does he get his knowledge from?
		verify(axisConfigView, atLeastOnce()).getSelectedValues();
		// There should no graphs been sent yet, this should only happen after an axisConfigDone event.
		verify(view, never()).setGraphs(anyCollectionOf(IOLAPGraph.class));
		// There should never occur an error.
		verify(axisConfigView, never()).displayError(anyString());
	}

	/**
	 * Tests the reaction of the Controller to the axisConfigDone event.
	 */
	@Test
	public void axisConfigDone() {
		// Push state, the dialog will also be filled in this methods.
		axisConfigChangedDimensionHierarchyHierarchyLevelValues();

		// Let the controller do some work.
		fireEvent(axisConfigView, EventType.axisConfigDone, axisConfigView);

		// Now we should be supplied with some graphs.
		verify(view).setGraphs(anyCollectionOf(IOLAPGraph.class));
		// There should never occur an error.
		verify(axisConfigView, never()).displayError(anyString());
	}

}
