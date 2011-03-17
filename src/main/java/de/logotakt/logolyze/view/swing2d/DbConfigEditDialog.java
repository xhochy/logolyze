package de.logotakt.logolyze.view.swing2d;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.logotakt.logolyze.view.interfaces.EventType;
import de.logotakt.logolyze.view.interfaces.IConnectionEditView;
import de.logotakt.logolyze.view.interfaces.IEventHandler;

/**
 * The DbConfigEditDialog allows the user to create or edit a connection with its name, connection string and
 * initialization string.
 * @author s_paulss
 */
@SuppressWarnings("serial")
public class DbConfigEditDialog extends Swing2DEventedModalDialog implements IConnectionEditView {

    protected static final EventType[] TRIGGERED_EVENTS = new EventType[] {EventType.dbConfigChanged,
            EventType.dbConfigChanging, EventType.dbConfigCreated };

    private JTextField connectionStringInput;
    private JTextField connectionNameInput;
    private JTextField initStringInput;

    /**
     * Are we creating a new configuration, or are we editing an old one?
     */
    private boolean creativeMode;

    private JButton btnUpdate;

    /**
     * Create a DbConfigEditDialog.
     * @param owner the parent of the dialog.
     */
    public DbConfigEditDialog(final JDialog owner) {
        super(owner, "Edit Connection Details");

        constructInterface();
    }

    private void constructInterface() {
        for (EventType type : TRIGGERED_EVENTS) {
            getListeners().put(type, new HashSet<IEventHandler>());
        }

        constructLayout();

        constructInputs();

        constructButtons();
        setSize(370, 160);
    }

    private void constructButtons() {
        JPanel buttonsPanel = new JPanel();
        GridBagConstraints gbcButtonsPanel = new GridBagConstraints();
        gbcButtonsPanel.gridwidth = 2;
        gbcButtonsPanel.fill = GridBagConstraints.BOTH;
        gbcButtonsPanel.gridx = 0;
        gbcButtonsPanel.gridy = 3;
        getContentPane().add(buttonsPanel, gbcButtonsPanel);
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        btnUpdate = new JButton("Update");
        btnUpdate.setName("update");

        // depending on wether we're creating or editing, fire dbConfigCreated or dbConfigChanged
        btnUpdate.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent arg0) {
                if (creativeMode) {
                    fireEvent(EventType.dbConfigCreated, DbConfigEditDialog.this);
                } else {
                    fireEvent(EventType.dbConfigChanged, DbConfigEditDialog.this);
                }
                setVisible(false);
            }
        });
        buttonsPanel.add(btnUpdate);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setName("cancel");
        btnCancel.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent arg0) {
                setVisible(false);
            }
        });
        buttonsPanel.add(btnCancel);
    }

    private void constructInputs() {
        GridBagConstraints lblGbc = new GridBagConstraints();
        GridBagConstraints inputGbc = new GridBagConstraints();

        lblGbc.anchor = GridBagConstraints.EAST;
        lblGbc.insets = new Insets(0, 0, 5, 5);
        lblGbc.gridx = 0;
        lblGbc.gridy = 0;

        inputGbc.fill = GridBagConstraints.HORIZONTAL;
        inputGbc.insets = new Insets(0, 0, 5, 5);
        inputGbc.gridx = 1;
        inputGbc.gridy = 0;

        JLabel lblName = new JLabel("Name");
        getContentPane().add(lblName, lblGbc);

        connectionNameInput = new JTextField();
        connectionNameInput.setName("name");
        lblName.setLabelFor(connectionNameInput);
        getContentPane().add(connectionNameInput, inputGbc);
        connectionNameInput.setColumns(10);

        lblGbc.gridy += 1;
        inputGbc.gridy += 1;

        JLabel lblConnectionString = new JLabel("Connection String");
        getContentPane().add(lblConnectionString, lblGbc);

        connectionStringInput = new JTextField();
        connectionStringInput.setName("string");
        lblConnectionString.setLabelFor(connectionStringInput);
        getContentPane().add(connectionStringInput, inputGbc);
        connectionStringInput.setColumns(10);

        lblGbc.gridy += 1;
        inputGbc.gridy += 1;

        JLabel lblInitString = new JLabel("Initialization String");
        getContentPane().add(lblInitString, lblGbc);

        initStringInput = new JTextField();
        initStringInput.setName("initString");
        lblInitString.setLabelFor(initStringInput);
        getContentPane().add(initStringInput, inputGbc);
        initStringInput.setColumns(10);
    }

    private void constructLayout() {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {0, 0, 0 };
        gridBagLayout.rowHeights = new int[] {0, 0, 0, 269, 0 };
        gridBagLayout.columnWeights = new double[] {0.0, 1.0, Double.MIN_VALUE };
        gridBagLayout.rowWeights = new double[] {0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
        getContentPane().setLayout(gridBagLayout);
    }

    /**
     * Sets the content of the Name input field.
     * @param name The name to display.
     */
    @Override
    public void setConnectionName(final String name) {
        connectionNameInput.setText(name);
    }

    /**
     * Gets the content of the Name input field.
     * @return the content of the Name input field.
     */
    @Override
    public String getConnectionName() {
        return connectionNameInput.getText();
    }

    /**
     * Sets the content of the connection string input field.
     * @param cs the new connection string to set.
     */
    @Override
    public void setConnectionString(final String cs) {
        connectionStringInput.setText(cs);
    }

    /**
     * Display the edit dialog for changing a connection configuration.
     * @param name the name of the configuration to edit.
     */
    public void showChanging(final String name) {
        setConnectionName(name);
        fireEvent(EventType.dbConfigChanging, name);
        creativeMode = false;
        setTitle("Edit Connection Details");
        btnUpdate.setText("Update");
        setVisible(true);
    }
    /**
     * Display the edit dialog for creating a new connection configuration.
     */
    public void showNew() {
        setConnectionName("");
        setInitializationString("");
        setConnectionString("");
        creativeMode = true;
        setTitle("Create a Connection");
        btnUpdate.setText("Create");
        setVisible(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConnectionString() {
        return connectionStringInput.getText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setInitializationString(final String is) {
        initStringInput.setText(is);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInitializationString() {
        return initStringInput.getText();
    }
}
