package de.logotakt.logolyze.controller;

import de.logotakt.logolyze.view.interfaces.EventArgs;
import de.logotakt.logolyze.view.interfaces.IEventHandler;
import de.logotakt.logolyze.view.interfaces.IMeasureConfigurationView;

/**
 * This is the sub-controller for managing a MeasureConfigurationView. Its job is to handle the configuration of
 * measures displayed on the Graphs. This controller handles the events measuresChanging, measuresChanged.
 */
public class MeasureConfigurationController implements IEventHandler {
	// This controller's superior Controller.
	private Controller controller;

	/**
	 * Initializes a new instance of MeasureConfigurationController.
	 *
	 * @param controller This controller's superior controller.
	 */
	public MeasureConfigurationController(final Controller controller) {
		this.controller = controller;
	}

	/**
	 * Dispatches the events this class can handle. MeasureConfigurationController implements this from the
	 * IEventHandler interface. The MeasureConfigurationController class accepts the following events:
	 * measuresChanging, measuresChanged
	 *
	 * @param e The arguments for the current event.
	 */
	@Override
	public void event(final EventArgs e) {
		switch (e.getType()) {
		case measuresChanging:
			handleMeasuresChanging(e);
			break;
		case measuresChanged:
			handleMeasuresChanged(e);
			break;
		default:
			throw new IllegalArgumentException("Wrong event was sent to ConnectionListController.");
		}
	}

	/* Handler functions for the individual events. */

	// This event is fired when the measure configuation dialog is shown to the user.
	// All available measures are passed to the dialog to display.
	private void handleMeasuresChanging(final EventArgs e) {
		IMeasureConfigurationView mcv = (IMeasureConfigurationView) e.getDetails();
		if (controller.getSelectedCube() != null) {
			mcv.setMeasures(controller.getSelectedCube().getMeasureTypes());
			mcv.setSelectedMeasures(controller.getMeasures());
		} else {
			mcv.displayError("Cannot configure the axes unless there is a cube selected.");
		}
	}

	// This event is fired when the user is finished selecting the measures to display.
	// The selected measures are retrieved from the dialog and passed to the controller.
	// In addition, a graph update is done.
	private void handleMeasuresChanged(final EventArgs e) {
		IMeasureConfigurationView mcv = (IMeasureConfigurationView) e.getDetails();
		controller.setMeasures(mcv.getSelectedMeasures());
		controller.displayNewGraphs(mcv);
	}

	/**
	 * Make this controller forget all its state.
	 */
	public void clean() { }
}
