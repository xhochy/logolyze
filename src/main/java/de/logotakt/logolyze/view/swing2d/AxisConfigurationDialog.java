package de.logotakt.logolyze.view.swing2d;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.logotakt.logolyze.view.interfaces.EventType;
import de.logotakt.logolyze.view.interfaces.IAxisConfigurationView;
import de.logotakt.logolyze.view.interfaces.IEventHandler;

/**
 * Used for changing the parameters of an axis.
 */
@SuppressWarnings("serial")
public class AxisConfigurationDialog extends Swing2DEventedModalDialog implements IAxisConfigurationView, IStateful {

    private JComboBox dimensionComboBox;
    private JComboBox hierarchyComboBox;
    private JComboBox levelComboBox;
    private JList valuesList;
    private DefaultListModel listModel;
    private String noneItem = "";
    private Axis axis;

    protected static final EventType[] TRIGGERED_EVENTS = new EventType[] {EventType.axisConfigChanged,
            EventType.axisConfigDone, EventType.axisConfigShowing };

    /**
     * Creates a new modal dialog to configure an axis.
     * @param owner The owner of this dialog.
     */
    public AxisConfigurationDialog(final JFrame owner) {
        super(owner, "Axis Configuration");

        for (EventType type : TRIGGERED_EVENTS) {
            getListeners().put(type, new HashSet<IEventHandler>());
        }

        // Init layout
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.rowWeights = new double[] {0.0, 0.0, 0.0, 1.0, 0.0, 0.0 };
        gridBagLayout.columnWeights = new double[] {0.0, 1.0 };
        getContentPane().setLayout(gridBagLayout);

        initDimensionsBox();
        initHierarchyBox();
        initLevelBox();
        initValuesList();
        initButtonPanel();
        pack();
    }

    /**
     * Inititialize Box with all available dimensions.
     */
    private void initDimensionsBox() {
        JLabel dimensionLabel = new JLabel("Dimension:");
        GridBagConstraints gbcDimensionLabel = new GridBagConstraints();
        gbcDimensionLabel.anchor = GridBagConstraints.EAST;
        gbcDimensionLabel.insets = new Insets(5, 5, 5, 5);
        gbcDimensionLabel.gridx = 0;
        gbcDimensionLabel.gridy = 0;
        getContentPane().add(dimensionLabel, gbcDimensionLabel);

        dimensionComboBox = new JComboBox();
        dimensionComboBox.setName("dimensionComboBox");
        dimensionComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (e.getItem() != noneItem && dimensionComboBox.getItemAt(0) == noneItem) {
                        dimensionComboBox.removeItemAt(0);
                    }
                    fireEvent(EventType.axisConfigChanged, AxisConfigurationDialog.this);
                }
            }
        });
        GridBagConstraints gbcDimensionComboBox = new GridBagConstraints();
        gbcDimensionComboBox.insets = new Insets(5, 3, 5, 0);
        gbcDimensionComboBox.fill = GridBagConstraints.HORIZONTAL;
        gbcDimensionComboBox.gridx = 1;
        gbcDimensionComboBox.gridy = 0;
        getContentPane().add(dimensionComboBox, gbcDimensionComboBox);
    }

    /**
     * Initialize Box with all hierarchies.
     */
    private void initHierarchyBox() {
        JLabel hierarchyLabel = new JLabel("Hierarchy:");
        GridBagConstraints gbcHierarchyLabel = new GridBagConstraints();
        gbcHierarchyLabel.anchor = GridBagConstraints.EAST;
        gbcHierarchyLabel.insets = new Insets(5, 5, 5, 5);
        gbcHierarchyLabel.gridx = 0;
        gbcHierarchyLabel.gridy = 1;
        getContentPane().add(hierarchyLabel, gbcHierarchyLabel);

        hierarchyComboBox = new JComboBox();
        hierarchyComboBox.setName("hierarchyComboBox");
        hierarchyComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (e.getItem() != noneItem && hierarchyComboBox.getItemAt(0) == noneItem) {
                        hierarchyComboBox.removeItemAt(0);
                    }
                    fireEvent(EventType.axisConfigChanged, AxisConfigurationDialog.this);
                }
            }
        });
        GridBagConstraints gbcHierarchyComboBox = new GridBagConstraints();
        gbcHierarchyComboBox.insets = new Insets(5, 3, 5, 0);
        gbcHierarchyComboBox.fill = GridBagConstraints.HORIZONTAL;
        gbcHierarchyComboBox.gridx = 1;
        gbcHierarchyComboBox.gridy = 1;
        getContentPane().add(hierarchyComboBox, gbcHierarchyComboBox);
    }

    /**
     * Initialize Box with the hierarchy level.
     */
    private void initLevelBox() {
        JLabel levelLabel = new JLabel("Level:");
        levelLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        GridBagConstraints gbcLevelLabel = new GridBagConstraints();
        gbcLevelLabel.anchor = GridBagConstraints.EAST;
        gbcLevelLabel.insets = new Insets(5, 0, 5, 5);
        gbcLevelLabel.gridx = 0;
        gbcLevelLabel.gridy = 2;
        getContentPane().add(levelLabel, gbcLevelLabel);

        levelComboBox = new JComboBox();
        levelComboBox.setName("levelComboBox");
        levelComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (e.getItem() != noneItem && levelComboBox.getItemAt(0) == noneItem) {
                        levelComboBox.removeItemAt(0);
                    }
                    fireEvent(EventType.axisConfigChanged, AxisConfigurationDialog.this);
                }
            }
        });
        GridBagConstraints gbcLevelComboBox = new GridBagConstraints();
        gbcLevelComboBox.insets = new Insets(5, 3, 5, 0);
        gbcLevelComboBox.fill = GridBagConstraints.HORIZONTAL;
        gbcLevelComboBox.gridx = 1;
        gbcLevelComboBox.gridy = 2;
        getContentPane().add(levelComboBox, gbcLevelComboBox);
    }

    /**
     * Initialize list with hierarchy values.
     */
    private void initValuesList() {
        JLabel valuesLabel = new JLabel("Values:");
        GridBagConstraints gbcValuesLabel = new GridBagConstraints();
        gbcValuesLabel.anchor = GridBagConstraints.NORTHEAST;
        gbcValuesLabel.insets = new Insets(5, 0, 5, 5);
        gbcValuesLabel.gridx = 0;
        gbcValuesLabel.gridy = 3;
        getContentPane().add(valuesLabel, gbcValuesLabel);

        JScrollPane scrollPane = new JScrollPane();
        GridBagConstraints gbcScrollPane = new GridBagConstraints();
        gbcScrollPane.gridheight = 2;
        gbcScrollPane.insets = new Insets(5, 3, 2, 2);
        gbcScrollPane.fill = GridBagConstraints.BOTH;
        gbcScrollPane.gridx = 1;
        gbcScrollPane.gridy = 3;
        getContentPane().add(scrollPane, gbcScrollPane);

        listModel = new DefaultListModel();
        valuesList = new JList(listModel);
        valuesList.setVisibleRowCount(5);
        valuesList.setName("valuesList");
        valuesList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(final ListSelectionEvent e) {
                // We do not want to fire an event if the user is still selecting things.
                if (e.getValueIsAdjusting()) {
                    return;
                }
                fireEvent(EventType.axisConfigChanged, AxisConfigurationDialog.this);
            }
        });
        valuesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        scrollPane.setViewportView(valuesList);
    }

    /**
     * Initialize the button panel at the bottom.
     */
    private void initButtonPanel() {
        JPanel panel = new JPanel();
        GridBagConstraints gbcPanel = new GridBagConstraints();
        gbcPanel.gridwidth = 2;
        gbcPanel.gridx = 0;
        gbcPanel.gridy = 5;
        getContentPane().add(panel, gbcPanel);
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        JButton updateButton = new JButton("Update");
        updateButton.setName("updateButton");
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                List<String> values = null;
                if (axis != null) {
                    values = axis.getValues();
                    axis.setValues(getSelectedValues());
                }
                resetErrorState();
                fireEvent(EventType.axisConfigDone, AxisConfigurationDialog.this);
                if (hadError()) {
                    if (axis != null) {
                        axis.setValues(values);
                    }
                } else {
                    setVisible(false);
                }
            }
        });
        panel.add(updateButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new HideOnActionListener(this));
        // in addition to hiding, we also have to make sure the state gets reset to whatever it was
        // before opening the dialog, so that perspective saving doesn't get erronous data.
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                fireEvent(EventType.axisConfigShowing, AxisConfigurationDialog.this);
            }
        });
        panel.add(cancelButton);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVisible(final boolean b) {
        if (b) {
            resetErrorState();
            fireEvent(EventType.axisConfigShowing, this);
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
    public void setDimensions(final List<String> dim) {
        dimensionComboBox.removeAllItems();
        suspendEvents();
        dimensionComboBox.addItem(noneItem);
        for (String string : dim) {
            dimensionComboBox.addItem(string);
        }
        resumeEvents();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSelectedDimension() {
        if (dimensionComboBox.getSelectedItem() == null || dimensionComboBox.getSelectedItem() == noneItem) {
            return null;
        }
        return dimensionComboBox.getSelectedItem().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHierarchies(final List<String> h) {
        hierarchyComboBox.removeAllItems();

        // If we should only clear, we're done.
        if (h == null) {
            return;
        }

        // Add new hierarchies.
        suspendEvents();
        hierarchyComboBox.addItem(noneItem);
        for (String string : h) {
            hierarchyComboBox.addItem(string);
        }
        resumeEvents();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSelectedHierarchy() {
        if (hierarchyComboBox.getSelectedItem() == null || hierarchyComboBox.getSelectedItem() == noneItem) {
            return null;
        }
        return hierarchyComboBox.getSelectedItem().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHierarchyLevels(final List<String> l) {
        levelComboBox.removeAllItems();

        // If we should only clear, we are done.
        if (l == null) {
            return;
        }

        // Add the new levels.
        suspendEvents();
        levelComboBox.addItem(noneItem);
        for (String string : l) {
            levelComboBox.addItem(string);
        }
        resumeEvents();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSelectedHierarchyLevel() {
        if (levelComboBox.getSelectedItem() == null || levelComboBox.getSelectedItem() == noneItem) {
            return null;
        }
        return levelComboBox.getSelectedItem().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValues(final List<String> v) {
        listModel.clear();

        // If we should only clear, we're done.
        if (v == null) {
            return;
        }

        // Add the new values.
        suspendEvents();
        for (String string : v) {
            listModel.addElement(string);
        }
        resumeEvents();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getSelectedValues() {
        Object[] selected = valuesList.getSelectedValues();
        ArrayList<String> result = new ArrayList<String>(selected.length);
        for (Object obj : selected) {
            result.add(obj.toString());
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSelectedValues(final List<String> v) {
        int[] indices = new int[v.size()];

        // We need to search the indices as it is not guaranteed that we will be given the same objects.
        for (int i = 0; i < v.size(); i++) {
            for (int j = 0; j < listModel.size(); j++) {
                if (listModel.get(j).equals(v.get(i))) {
                    indices[i] = j;
                }
            }
        }
        valuesList.setSelectedIndices(indices);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSelectedDimension(final String dimension) {
        suspendEvents();
        for (int i = 0; i < dimensionComboBox.getItemCount(); i++) {
            if (dimensionComboBox.getItemAt(i).equals(dimension)) {
                dimensionComboBox.setSelectedIndex(i);
                break;
            }
        }
        resumeEvents();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSelectedHierarchy(final String hierarchy) {
        suspendEvents();
        for (int i = 0; i < hierarchyComboBox.getItemCount(); i++) {
            if (hierarchyComboBox.getItemAt(i).equals(hierarchy)) {
                hierarchyComboBox.setSelectedIndex(i);
            }
        }
        resumeEvents();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSelectedHierarchyLevel(final String hierarchyLevel) {
        suspendEvents();
        for (int i = 0; i < levelComboBox.getItemCount(); i++) {
            if (levelComboBox.getItemAt(i).equals(hierarchyLevel)) {
                levelComboBox.setSelectedIndex(i);
            }
        }
        resumeEvents();
    }

    /**
     * Associate an axis with this dialog.
     * @param axis The associated axis.
     */
    public void setAxis(final Axis axis) {
        this.axis = axis;
    }

    /**
     * Retrieve the associated axis.
     * @return The associated axis.
     */
    public Axis getAxis() {
        return axis;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IState getState() {
        AxisConfigurationDialogState state = new AxisConfigurationDialogState();
        state.setSelectedDimension(getSelectedDimension());
        state.setSelectedHierarchy(getSelectedHierarchy());
        state.setSelectedHierarchyLevel(getSelectedHierarchyLevel());
        state.setSelectedHierarchyValues(getSelectedValues().toArray(new String[getSelectedValues().size()]));
        return state;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("BC_UNCONFIRMED_CAST")
    public void setState(final IState stateInfo) {
        AxisConfigurationDialogState state = (AxisConfigurationDialogState) stateInfo;
        forceEvent(EventType.axisConfigShowing, this);
        setSelectedDimension(state.getSelectedDimension());
        forceEvent(EventType.axisConfigChanged, this);
        setSelectedHierarchy(state.getSelectedHierarchy());
        forceEvent(EventType.axisConfigChanged, this);
        setSelectedHierarchyLevel(state.getSelectedHierarchyLevel());
        forceEvent(EventType.axisConfigChanged, this);
        setSelectedValues(Arrays.asList(state.getSelectedHierarchyValues()));
        forceEvent(EventType.axisConfigChanged, this);
        axis.setValues(getSelectedValues());
        forceEvent(EventType.axisConfigDone, this);
    }
}
