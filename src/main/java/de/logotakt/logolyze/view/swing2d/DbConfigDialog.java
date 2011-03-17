package de.logotakt.logolyze.view.swing2d;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.logotakt.logolyze.view.interfaces.EventArgs;
import de.logotakt.logolyze.view.interfaces.EventType;
import de.logotakt.logolyze.view.interfaces.IConnectionListView;
import de.logotakt.logolyze.view.interfaces.IEventHandler;

/**
 * The DbConfigDialog allows the User to manage all connections. The actions available to the User are connecting,
 * editing, adding and deleting. Additionally, the User may dismiss the Dialog with a Button.
 * @author s_paulss
 */

@SuppressWarnings("serial")
public class DbConfigDialog extends Swing2DEventedModalDialog implements IConnectionListView, IEventHandler {

    protected static final EventType[] TRIGGERED_EVENTS = new EventType[] {EventType.dbConfigSelected,
            EventType.dbConfigCreated, EventType.dbConfigChanging, EventType.connectionListShowing,
            EventType.dbConfigRemoved, EventType.dbConfigChanged };
    private JList connectionsList;
    private JButton editButton;
    private JButton deleteButton;
    private JButton connectButton;
    private DefaultListModel connectionsListModel;
    private DbConfigEditDialog myConfigEditDialog;

    /**
     * Create a DbConfigDialog with the given Names.
     * @param owner the parent of the dialog.
     */
    public DbConfigDialog(final JFrame owner) {
        super(owner, "Edit Connections - Logolyze");


        for (EventType type : TRIGGERED_EVENTS) {
            getListeners().put(type, new HashSet<IEventHandler>());
        }

        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWeights = new double[] {1.0 };
        gridBagLayout.rowWeights = new double[] {1.0, 0.0 };
        getContentPane().setLayout(gridBagLayout);

        JScrollPane connListScrollPane = new JScrollPane();
        GridBagConstraints gbcConnListScrollPane = new GridBagConstraints();
        gbcConnListScrollPane.fill = GridBagConstraints.BOTH;
        gbcConnListScrollPane.gridx = 0;
        gbcConnListScrollPane.gridy = 0;
        getContentPane().add(connListScrollPane, gbcConnListScrollPane);

        connectionsListModel = new DefaultListModel();
        connectionsList = new JList();
        connectionsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        connectionsList.setModel(connectionsListModel);
        connectionsList.setName("connectionsList");

        // only let the user click "edit", "delete" or "connect" when an entry is selected.
        connectionsList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(final ListSelectionEvent e) {
                if (connectionsList.getSelectedIndex() != -1) {
                    editButton.setEnabled(true);
                    deleteButton.setEnabled(true);
                    connectButton.setEnabled(true);
                } else {
                    editButton.setEnabled(false);
                    deleteButton.setEnabled(false);
                    connectButton.setEnabled(false);
                }
            }
        });
        connListScrollPane.setViewportView(connectionsList);

        JPanel buttonsPanel = new JPanel();
        GridBagConstraints gbcButtonsPanel = new GridBagConstraints();
        gbcButtonsPanel.gridx = 0;
        gbcButtonsPanel.gridy = 1;
        getContentPane().add(buttonsPanel, gbcButtonsPanel);
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        connectButton = new JButton("Connect");
        connectButton.setEnabled(false);
        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent arg0) {
                fireEvent(EventType.dbConfigSelected, (String) connectionsList.getSelectedValue());
                setVisible(false);
            }
        });
        connectButton.setName("connectButton");
        buttonsPanel.add(connectButton);

        JButton newButton = new JButton("New");
        newButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent arg0) {
                myConfigEditDialog.showNew();
            }
        });
        newButton.setName("newButton");
        buttonsPanel.add(newButton);

        editButton = new JButton("Edit");
        editButton.setEnabled(false);
        editButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent arg0) {
                myConfigEditDialog.showChanging((String) connectionsList.getSelectedValue());
            }
        });
        editButton.setName("editButton");
        buttonsPanel.add(editButton);

        deleteButton = new JButton("Delete");
        deleteButton.setEnabled(false);

        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent arg0) {
                fireEvent(EventType.dbConfigRemoved, (String) connectionsList.getSelectedValue());
                fireEvent(EventType.connectionListShowing, DbConfigDialog.this);
            }
        });
        deleteButton.setName("deleteButton");
        buttonsPanel.add(deleteButton);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent arg0) {
                setVisible(false);
            }
        });
        closeButton.setName("closeButton");
        buttonsPanel.add(closeButton);

        myConfigEditDialog = new DbConfigEditDialog(this);
        myConfigEditDialog.setName("editDialog");
        myConfigEditDialog.addEventListener(this, EventType.dbConfigChanging);
        myConfigEditDialog.addEventListener(this, EventType.dbConfigChanged);
        myConfigEditDialog.addEventListener(this, EventType.dbConfigCreated);
        pack();
    }

    /**
     * Set the List of Connections to show.
     * @param names The names to show to the user.
     */
    @Override
    public void setConnectionNames(final Collection<String> names) {
        connectionsListModel.clear();
        for (String name : names) {
            connectionsListModel.addElement(name);
        }
    }

    /**
     * Return the name of the selected connection.
     * @return the name of the selected connection or Null.
     */
    @Override
    public String getSelectedConnection() {
        Object selected = connectionsList.getSelectedValue();
        return selected.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVisible(final boolean b) {
        if (b) {
            fireEvent(EventType.connectionListShowing, this);
        }
        super.setVisible(b);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void event(final EventArgs e) {
        // Proxy the event to all known listeners.
        for (IEventHandler handler : getListeners().get(e.getType())) {
            handler.event(e);
        }

        // when a config changes, refresh the list.
        if (e.getType() == EventType.dbConfigChanged || e.getType() == EventType.dbConfigCreated) {
            fireEvent(EventType.connectionListShowing, this);
        }
    }
}
