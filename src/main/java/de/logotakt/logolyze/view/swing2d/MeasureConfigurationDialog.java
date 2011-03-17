package de.logotakt.logolyze.view.swing2d;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.logotakt.logolyze.model.interfaces.IMeasureType;
import de.logotakt.logolyze.model.interfaces.MeasureAssociation;
import de.logotakt.logolyze.model.interfaces.MeasureClass;
import de.logotakt.logolyze.view.interfaces.EventType;
import de.logotakt.logolyze.view.interfaces.IEventHandler;
import de.logotakt.logolyze.view.interfaces.IMeasureConfigurationView;

/**
 * This dialog allows the user to configure what measures to display for edges and what measures to display for nodes.
 */
@SuppressWarnings("serial")
public class MeasureConfigurationDialog extends Swing2DEventedModalDialog implements IMeasureConfigurationView,
        IStateful {
    /**
     * A listener that reacts to selecting a certain display option for a measure and causes the options to stay
     * coherent.
     * @param <T> An Enum this Listener operates on.
     */
    public class MeasureDisplayOptionsListener<T extends Enum<T>> implements ActionListener {
        private final IMeasureType measure;
        private final Object ignoreValue;
        private final MeasureConfigurationDialog dialog;
        private final Collection<? extends IMeasureType> allMeasures;

        /**
         * Create a MeasureDisplayOptionsListener for validation of MeasureDisplayOptions.
         * @param measure The measure to act on.
         * @param ignoreValue The value that signifies not using a MeasureDisplayType.
         * @param dialog The Dialog for handling combo box changing and such.
         * @param allMeasures A Collection of all available measures.
         */
        public MeasureDisplayOptionsListener(final IMeasureType measure, final T ignoreValue,
                final MeasureConfigurationDialog dialog, final Collection<? extends IMeasureType> allMeasures) {
            this.measure = measure;
            this.ignoreValue = ignoreValue;
            this.dialog = dialog;
            this.allMeasures = allMeasures;
        }

        /**
         * Set the MeasureDisplayOption for the measure and — if needed — unset other measures to use the same Option.
         * @param e The event that was fired.
         */
        @Override
        public void actionPerformed(final ActionEvent e) {
            String strtype = (String) ((JComboBox) e.getSource()).getSelectedItem();
            Object type = Enum.valueOf((Class<T>) ignoreValue.getClass(), strtype);

            // text, none and tooltip are valid for more than one measure at a time
            if (!(strtype.equals("none") || strtype.equals("text") || strtype.equals("tooltip"))) {
                // Deactivate possibly other measure that is displayed using this type.
                for (final IMeasureType otherMeasure : allMeasures) {
                    // ignore the same measure and measures with the wrong association.
                    if (otherMeasure.equals(this.measure) || this.measure.getAssoc() != otherMeasure.getAssoc()) {
                        continue;
                    }


                    if (otherMeasure.getAssoc() == MeasureAssociation.edgeMeasure) {
                        Object dType = Enum.valueOf(EdgeMeasureDisplayType.class,
                                (String) measureDisplayTypeBoxes.get(otherMeasure).getSelectedItem());
                        if (type == (EdgeMeasureDisplayType) dType) {
                            dialog.selectEdgeMeasureDisplayType(otherMeasure, (EdgeMeasureDisplayType) ignoreValue);
                        }
                    } else if (otherMeasure.getAssoc() == MeasureAssociation.nodeMeasure) {
                        Object dType = Enum.valueOf(NodeMeasureDisplayType.class,
                                (String) measureDisplayTypeBoxes.get(otherMeasure).getSelectedItem());
                        if (type == (NodeMeasureDisplayType) dType) {
                            dialog.selectNodeMeasureDisplayType(otherMeasure, (NodeMeasureDisplayType) ignoreValue);
                        }
                    }
                }
            }

            if (measure.getAssoc() == MeasureAssociation.edgeMeasure) {
                dialog.selectEdgeMeasureDisplayType(measure, (EdgeMeasureDisplayType) type);
            } else if (measure.getAssoc() == MeasureAssociation.nodeMeasure) {
                dialog.selectNodeMeasureDisplayType(measure, (NodeMeasureDisplayType) type);
            }
        }
    }

    protected static final EventType[] TRIGGERED_EVENTS = new EventType[] {EventType.measuresChanged,
            EventType.measuresChanging };

    /**
     * Map from name of the measure to instance of IMeasureType for simple access.
     */
    private Map<String, IMeasureType> measures;
    /**
     * Map what combo box is used for what MeasureType
     */
    private Map<IMeasureType, JComboBox> measureDisplayTypeBoxes;

    /**
     * Map that maps measures to their currently set display types.
     */
    private Map<IMeasureType, Object> measureDisplayTypes;
    private boolean displayTypesKept;

    private JPanel buttonsPanel;

    /**
     * Create a MeasureConfigurationDialog for the given owner.
     * @param owner The owner of the Dialog
     */
    public MeasureConfigurationDialog(final JDialog owner) {
        super(owner, "Edit Measures");
        setupGui();
        setupFields();
        pack();
    }

    /**
     * Create a MeasureConfigurationDialog for the given owner.
     * @param owner The owner for the Dialog.
     * @wbp.parser.constructor
     */
    public MeasureConfigurationDialog(final JFrame owner) {
        super(owner, "Edit Measures");
        setupGui();
        setupFields();
        pack();
    }

    private void setupFields() {
        measures = new HashMap<String, IMeasureType>();
        measureDisplayTypeBoxes = new HashMap<IMeasureType, JComboBox>();
        measureDisplayTypes = new HashMap<IMeasureType, Object>();

        for (EventType type : TRIGGERED_EVENTS) {
            getListeners().put(type, new HashSet<IEventHandler>());
        }
    }

    private void setupGui() {
        GridBagLayout gridBagLayout = new GridBagLayout();
        getContentPane().setLayout(gridBagLayout);

        buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        JButton btnUpdate = new JButton("Update");
        btnUpdate.setName("update");

        btnUpdate.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent arg0) {
                saveMeasureDisplayTypes();
                fireEvent(EventType.measuresChanged, MeasureConfigurationDialog.this);
                setVisible(false);
            }
        });
        buttonsPanel.add(btnUpdate);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setName("cancel");
        btnCancel.addActionListener(new HideOnActionListener(this));
        buttonsPanel.add(btnCancel);
    }

    private void createCheckPanel(final IMeasureType measure, final Collection<? extends IMeasureType> allMeasures,
            final GridBagConstraints position, final Container container) {
        GridBagConstraints c = (GridBagConstraints) position.clone();

        container.add(new JLabel(measure.getKey()), c);

        JComboBox displayType = new JComboBox();
        displayType.setName("Measure " + measure.getKey());
        c.gridx += 1;
        container.add(displayType, c);
        measureDisplayTypeBoxes.put(measure, displayType);

        // TODO the action listeners have to operate on a separate map for settings, so that the update and cancel
        // button can have different effects

        // Add a combo box to display options on the handling of measure.
        if (measure.getAssoc() == MeasureAssociation.edgeMeasure) {
            for (final EdgeMeasureDisplayType type : EdgeMeasureDisplayType.values()) {
                if (measure.getMeasureClass() == MeasureClass.NumeralMeasure || type == EdgeMeasureDisplayType.none
                        || type == EdgeMeasureDisplayType.text || type == EdgeMeasureDisplayType.tooltip) {
                    // only display "text" or "none" or "tooltip" for measures other than DoubleMeasure
                    displayType.addItem(type.toString());
                }
            }

            if (!displayTypesKept) {
                measureDisplayTypes.put(measure, EdgeMeasureDisplayType.none);
            }

            displayType.addActionListener(new MeasureDisplayOptionsListener<EdgeMeasureDisplayType>(measure,
                    EdgeMeasureDisplayType.none, this, allMeasures));

        } else if (measure.getAssoc() == MeasureAssociation.nodeMeasure) {
            for (final NodeMeasureDisplayType type : NodeMeasureDisplayType.values()) {
                if (measure.getMeasureClass() == MeasureClass.NumeralMeasure || type == NodeMeasureDisplayType.none
                        || type == NodeMeasureDisplayType.text || type == NodeMeasureDisplayType.tooltip) {
                    displayType.addItem(type.toString());
                }
            }

            if (!displayTypesKept) {
                measureDisplayTypes.put(measure, NodeMeasureDisplayType.none);
            }

            displayType.addActionListener(new MeasureDisplayOptionsListener<NodeMeasureDisplayType>(measure,
                    NodeMeasureDisplayType.none, this, allMeasures));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMeasures(final Collection<? extends IMeasureType> m) {
        Container measuresPanel = getContentPane();
        measuresPanel.removeAll();
        measureDisplayTypeBoxes.clear();
        measures.clear();

        if (!displayTypesKept) {
            measureDisplayTypes.clear();
        }

        GridBagLayout gridBagLayout = new GridBagLayout();
        measuresPanel.setLayout(gridBagLayout);

        JLabel lblEdgeMeasures = new JLabel("Edge Measures");
        GridBagConstraints gbcLblEdgeMeasures = new GridBagConstraints();
        gbcLblEdgeMeasures.insets = new Insets(5, 5, 5, 5);
        gbcLblEdgeMeasures.gridx = 0;
        gbcLblEdgeMeasures.gridy = 0;
        gbcLblEdgeMeasures.gridwidth = 2;
        measuresPanel.add(lblEdgeMeasures, gbcLblEdgeMeasures);

        JLabel lblNodeMeasures = new JLabel("Node Measures");
        GridBagConstraints gbcLblNodeMeasures = new GridBagConstraints();
        gbcLblNodeMeasures.insets = new Insets(5, 5, 5, 5);
        gbcLblNodeMeasures.gridx = 2;
        gbcLblNodeMeasures.gridy = 0;
        gbcLblNodeMeasures.gridwidth = 2;
        measuresPanel.add(lblNodeMeasures, gbcLblNodeMeasures);

        int nodePosition = 0;
        int edgePosition = 0;
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.fill = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.WEST;

        for (IMeasureType measure : m) {
            measures.put(measure.getKey(), measure);

            if (measure.getAssoc() == MeasureAssociation.edgeMeasure) {
                gbc.gridx = 0;
                gbc.gridy = edgePosition + 1;
                edgePosition += 1;
            } else {
                gbc.gridx = 2;
                gbc.gridy = nodePosition + 1;
                nodePosition += 1;
            }
            createCheckPanel(measure, m, gbc, measuresPanel);
        }

        // if either of the measure types have no entries, or if neither have entries,
        // display a nice label so that the layout won't break.
        if (nodePosition == 0 || edgePosition == 0) {
            GridBagConstraints egbc = new GridBagConstraints();
            egbc.insets = new Insets(0, 0, 5, 5);
            egbc.fill = GridBagConstraints.BOTH;
            egbc.gridwidth = 2;

            if (edgePosition == 0) {
                egbc.gridx = 0;
            } else {
                egbc.gridx = 2;
            }

            egbc.gridy = 1;
            egbc.anchor = GridBagConstraints.CENTER;

            if (nodePosition == 0 && edgePosition == 0) {
                egbc.gridwidth = 4;
                measuresPanel.add(new JLabel("There are no Measures."), egbc);
            } else if (nodePosition == 0) {
                egbc.gridheight = edgePosition - 1;
                measuresPanel.add(new JLabel("No Node Measures."), egbc);
            } else {
                egbc.gridheight = nodePosition - 1;
                measuresPanel.add(new JLabel("No Edge Measures."), egbc);
            }
        }

        gbc.gridy = Math.max(nodePosition, edgePosition) + 2;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        measuresPanel.add(buttonsPanel, gbc);

        pack();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVisible(final boolean b) {
        if (b) {
            resetErrorState();
            fireEvent(EventType.measuresChanging, this);
            if (hadError()) {
                super.setVisible(false);
                return;
            }
        }
        super.setVisible(b);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<IMeasureType> getSelectedMeasures() {
        Collection<IMeasureType> selection = new ArrayList<IMeasureType>();
        for (IMeasureType val : measures.values()) {
            if (!measureDisplayTypeBoxes.get(val).getSelectedItem().toString().equals("none")) {
                selection.add(val);
            }
        }
        return selection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSelectedMeasures(final Collection<? extends IMeasureType> sels) {
        for (IMeasureType type : sels) {
            JComboBox cb = measureDisplayTypeBoxes.get(type);
            cb.setSelectedItem(measureDisplayTypes.get(type).toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IState getState() {
        MeasureConfigurationDialogState state = new MeasureConfigurationDialogState();
        Collection<IMeasureType> selectedMeasuresCollection = getSelectedMeasures();
        IMeasureType[] selectedMeasures = (IMeasureType[]) selectedMeasuresCollection
                .toArray(new IMeasureType[selectedMeasuresCollection.size()]);

        String[] types = new String[selectedMeasures.length];
        String[] sels = new String[selectedMeasures.length];

        for (int idx = 0; idx < selectedMeasures.length; idx++) {
            if (selectedMeasures[idx].getAssoc() == MeasureAssociation.edgeMeasure) {
                types[idx] = getEdgeDisplayType(selectedMeasures[idx]).toString();
            } else {
                types[idx] = getNodeDisplayType(selectedMeasures[idx]).toString();
            }
            sels[idx] = selectedMeasures[idx].getKey();
        }
        state.setSelectedMeasures(sels);
        state.setMeasureDisplayTypes(types);
        return state;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("BC_UNCONFIRMED_CAST")
    public void setState(final IState rawState) {
        forceEvent(EventType.measuresChanging, this);
        MeasureConfigurationDialogState state = (MeasureConfigurationDialogState) rawState;
        List<String> selections = Arrays.asList(state.getSelectedMeasures());
        List<String> types = Arrays.asList(state.getMeasureDisplayTypes());

        for (JComboBox cbox : measureDisplayTypeBoxes.values()) {
            cbox.setSelectedItem("none");
        }

        for (Map.Entry<String, IMeasureType> entr : measures.entrySet()) {
            int index = selections.indexOf(entr.getKey());
            if (index >= 0) {
                IMeasureType measure = (IMeasureType) entr.getValue();
                if (measure.getAssoc() == MeasureAssociation.nodeMeasure) {
                    selectNodeMeasureDisplayType(measure, Enum.valueOf(NodeMeasureDisplayType.class, types.get(index)));
                } else if (measure.getAssoc() == MeasureAssociation.edgeMeasure) {
                    selectEdgeMeasureDisplayType(measure, Enum.valueOf(EdgeMeasureDisplayType.class, types.get(index)));
                }
            }
        }
        saveMeasureDisplayTypes();
        forceEvent(EventType.measuresChanged, this);
    }

    /**
     * Return how to display the measure (an edge).
     * @param measure The measure in question.
     * @return The EdgeMeasureDisplayType for the measure in question.
     */
    public EdgeMeasureDisplayType getEdgeDisplayType(final IMeasureType measure) {
        return (EdgeMeasureDisplayType) measureDisplayTypes.get(measure);
    }

    /**
     * Return how to display the measure (a node).
     * @param measure The measure in question.
     * @return The NodeMeasureDisplayType for the measure in question.
     */
    public NodeMeasureDisplayType getNodeDisplayType(final IMeasureType measure) {
        return (NodeMeasureDisplayType) measureDisplayTypes.get(measure);
    }

    void saveMeasureDisplayTypes() {
        measureDisplayTypes.clear();
        for (Entry<IMeasureType, JComboBox> entry : measureDisplayTypeBoxes.entrySet()) {
            Object dType;
            if (entry.getKey().getAssoc() == MeasureAssociation.edgeMeasure) {
                dType = Enum.valueOf(EdgeMeasureDisplayType.class, entry.getValue().getSelectedItem().toString());
            } else {
                dType = Enum.valueOf(NodeMeasureDisplayType.class, entry.getValue().getSelectedItem().toString());
            }
            measureDisplayTypes.put(entry.getKey(), dType);
        }
        displayTypesKept = true;
    }

    void selectNodeMeasureDisplayType(final IMeasureType measure, final NodeMeasureDisplayType type) {
        measureDisplayTypeBoxes.get(measure).setSelectedItem(type.toString());
    }

    void selectEdgeMeasureDisplayType(final IMeasureType measure, final EdgeMeasureDisplayType type) {
        measureDisplayTypeBoxes.get(measure).setSelectedItem(type.toString());
    }

    /**
     * Forget all about the measure display types.
     */
    public void getFresh() {
        measureDisplayTypes.clear();
        displayTypesKept = false;
    }

    /**
     * Bring the MCD down as elegantly as possible.
     */
    void shutdown() {
        suspendEvents();
        setVisible(false);
    }
}
