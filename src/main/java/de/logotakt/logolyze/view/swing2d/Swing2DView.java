package de.logotakt.logolyze.view.swing2d;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.logotakt.logolyze.LogolyzeMain;
import de.logotakt.logolyze.model.interfaces.IOLAPGraph;
import de.logotakt.logolyze.utils.IOUtils;
import de.logotakt.logolyze.view.interfaces.EventArgs;
import de.logotakt.logolyze.view.interfaces.EventType;
import de.logotakt.logolyze.view.interfaces.IEventHandler;
import de.logotakt.logolyze.view.interfaces.ILogolyzeView;
import de.logotakt.logolyze.view.interfaces.IViewState;

/**
 * Implementation of a {@link LogolyzeView} using Swing.
 */
@SuppressWarnings("serial")
public class Swing2DView extends JFrame implements ILogolyzeView, IEventHandler {
    /**
     * This ActionListener sets a GraphLayout on all panels.
     */
    private static final class LayoutChangeActionListener implements ActionListener {
        private LayoutName name;
        private Swing2DView view;

        public LayoutChangeActionListener(final LayoutName name, final Swing2DView view) {
            this.name = name;
            this.view = view;
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            this.view.globalDisplayOptions.setLayout(this.name);
            this.view.graphGrid.updateGlobalOptions();
        }
    }

    /**
     * All listeners are registered to this view. Because we are storing the handlers only in this class/object, the
     * handlers are not registered at each Component (-> no duplicate storage).
     */
    private Map<EventType, Set<IEventHandler>> listeners;
    private GraphGrid2D graphGrid;
    private JMenu mnLastConections;
    private HierarchyTreeView hierarchyTree;
    private JFileChooser fileChooser = new JFileChooser();
    private JMenuItem mntmDisconnect;

    private JLabel responseTime;
    private DisplayOptions globalDisplayOptions;
    private JSpinner ySpinner;
    private JSpinner xSpinner;

    /**
     * Create a new real instance.
     */
    public Swing2DView() {
        setTitle("Logolyze");

        // Send an event when this window is closing.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                fireEvent(EventType.shutdownTriggered, Swing2DView.this);
            }
        });

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {0, 0 };
        gridBagLayout.rowHeights = new int[] {0, 0 };
        gridBagLayout.columnWeights = new double[] {1.0, Double.MIN_VALUE };
        gridBagLayout.rowWeights = new double[] {1.0, Double.MIN_VALUE };
        getContentPane().setLayout(gridBagLayout);

        JSplitPane splitPane = new JSplitPane();
        splitPane.setOneTouchExpandable(true);
        GridBagConstraints gbcSplitPane = new GridBagConstraints();
        gbcSplitPane.insets = new Insets(0, 0, 0, 5);
        gbcSplitPane.fill = GridBagConstraints.BOTH;
        gbcSplitPane.gridx = 0;
        gbcSplitPane.gridy = 0;
        getContentPane().add(splitPane, gbcSplitPane);

        hierarchyTree = new HierarchyTreeView();
        hierarchyTree.setBackground(getBackground());
        GridBagConstraints gbcHierarchyTree = new GridBagConstraints();
        gbcHierarchyTree.insets = new Insets(0, 0, 5, 5);
        gbcHierarchyTree.fill = GridBagConstraints.BOTH;
        gbcHierarchyTree.gridx = 0;
        gbcHierarchyTree.gridy = 0;
        splitPane.add(hierarchyTree, JSplitPane.LEFT);
        // getContentPane().add(hierarchyTree, gbcHierarchyTree);

        hierarchyTree.setName("hierarchyTree");
        for (EventType typ : HierarchyTreeView.TRIGGERED_EVENTS) {
            hierarchyTree.addEventListener(this, typ);
        }

        this.globalDisplayOptions = DisplayOptions.getDefault();
        graphGrid = new GraphGrid2D(this, this.globalDisplayOptions);
        graphGrid.setName("graphGrid");
        graphGrid.addEventListener(this, EventType.axisConfigChanged);
        graphGrid.addEventListener(this, EventType.axisConfigDone);
        graphGrid.addEventListener(this, EventType.axisConfigShowing);
        GridBagConstraints gbcGraphGrid = new GridBagConstraints();
        gbcGraphGrid.insets = new Insets(0, 0, 5, 0);
        gbcGraphGrid.fill = GridBagConstraints.BOTH;
        gbcGraphGrid.gridx = 1;
        gbcGraphGrid.gridy = 0;
        // getContentPane().add(graphGrid, gbcGraphGrid);
        splitPane.add(graphGrid, JSplitPane.RIGHT);

        for (EventType typ : GraphGrid2D.TRIGGERED_EVENTS) {
            graphGrid.addEventListener(this, typ);
        }

        initializeMenu();

        setSize(800, 600);

        listeners = new HashMap<EventType, Set<IEventHandler>>();
        for (EventType type : EventType.values()) {
            listeners.put(type, new HashSet<IEventHandler>());
        }

        JPanel statusbar = new JPanel();
        GridBagConstraints gbcStatusbar = new GridBagConstraints();
        gbcStatusbar.insets = new Insets(0, 0, 5, 0);
        gbcStatusbar.fill = GridBagConstraints.BOTH;
        gbcStatusbar.gridx = 0;
        gbcStatusbar.gridy = 3;
        gbcStatusbar.gridwidth = 2;
        statusbar.setLayout(new FlowLayout(FlowLayout.LEFT));
        getContentPane().add(statusbar, gbcStatusbar);

        responseTime = new JLabel();
        responseTime.setText("Haven't played with graphs yet.");
        statusbar.add(responseTime);
    }

    private void initializeMenu() {
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        initializeFileMenu(menuBar);
        initializeDatabaseMenu(menuBar);
        initializePrefMenu(menuBar);
        initializeHelpMenu(menuBar);

        menuBar.add(Box.createHorizontalGlue());

        JLabel labelX = new JLabel("X:");
        menuBar.add(labelX);

        xSpinner = new JSpinner();
        xSpinner.setModel(new SpinnerNumberModel(Double.valueOf(2.0), Double.valueOf(1.0), null, Double.valueOf(0.2)));
        menuBar.add(xSpinner);

        JLabel labelY = new JLabel("  Y:");
        menuBar.add(labelY);
        labelY.setSize(50, labelY.getHeight());

        ySpinner = new JSpinner();
        ySpinner.setModel(new SpinnerNumberModel(Double.valueOf(2), Double.valueOf(1.0), null, Double.valueOf(0.2)));
        menuBar.add(ySpinner);

        xSpinner.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(final ChangeEvent e) {
                Double val = (Double) ((JSpinner) e.getSource()).getValue();
                Swing2DView.this.graphGrid.setPanelDisplayX(val);
            }

        });

        ySpinner.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(final ChangeEvent e) {
                Double val = (Double) ((JSpinner) e.getSource()).getValue();
                Swing2DView.this.graphGrid.setPanelDisplayY(val);
            }

        });
    }

    private void initializeHelpMenu(final JMenuBar menuBar) {
        JMenu mnHelp = new JMenu("Help");
        menuBar.add(mnHelp);

        JMenuItem mntmUsage = new JMenuItem("Usage");
        mntmUsage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                try {
                    InputStream readmeStream = getClass().getResourceAsStream("/README.txt");
                    String docString = IOUtils.slurpInputStream(readmeStream);

                    JOptionPane.showMessageDialog((Component) e.getSource(), docString);

                    readmeStream.close();
                } catch (IOException err) {
                    JOptionPane.showMessageDialog((Component) e.getSource(),
                            "Error displaying help: " + err.getMessage());
                }
            }
        });
        mnHelp.add(mntmUsage);

        JMenuItem mntmAbout = new JMenuItem("About");
        mntmAbout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                JOptionPane.showMessageDialog((Component) e.getSource(), "LogoLyze version " + LogolyzeMain.VERSION);
            }
        });
        mnHelp.add(mntmAbout);
    }

    private void initializePrefMenu(final JMenuBar menuBar) {
        JMenu mnPrefs = new JMenu("Preferences");
        menuBar.add(mnPrefs);

        JCheckBoxMenuItem mntmLabels = new JCheckBoxMenuItem("Globally force displaying labels");
        mnPrefs.add(mntmLabels);
        mntmLabels.setSelected(globalDisplayOptions.isLabelDisplayForced());
        mntmLabels.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                boolean force = ((JCheckBoxMenuItem) e.getSource()).getState();

                Swing2DView.this.globalDisplayOptions.setForceLabels(force);
                Swing2DView.this.graphGrid.updateGlobalOptions();
            }

        });

        JMenuItem mntmColor = new JMenuItem("Node color");
        mnPrefs.add(mntmColor);
        mntmColor.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent arg0) {
                ColorChooserDialog cs;

                cs = new ColorChooserDialog(Swing2DView.this.globalDisplayOptions.getNodeColor());

                cs.addChangeListener(new ChangeListener() {

                    @Override
                    public void stateChanged(final ChangeEvent e) {
                        ColorChooserDialog cs;

                        cs = (ColorChooserDialog) e.getSource();
                        Swing2DView.this.globalDisplayOptions.setNodeColor(cs.getColor());
                        Swing2DView.this.graphGrid.updateGlobalOptions();
                    }
                });

                cs.setVisible(true);
            }

        });

        JCheckBoxMenuItem mntmRemoveIsolated = new JCheckBoxMenuItem("Remove isolated nodes");
        mntmRemoveIsolated.setSelected(globalDisplayOptions.isRemoveIsolatedNodesWanted());
        mnPrefs.add(mntmRemoveIsolated);
        mntmRemoveIsolated.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                boolean remove = ((JCheckBoxMenuItem) e.getSource()).getState();

                Swing2DView.this.globalDisplayOptions.setRemoveIsolated(remove);
                Swing2DView.this.graphGrid.updateGlobalOptions();
            }
        });

        JMenu mntmLayout = new JMenu("Graph Layout");
        mnPrefs.add(mntmLayout);

        JMenuItem mntmLayoutISOM = new JMenuItem("ISOM (default)");
        mntmLayoutISOM.addActionListener(new LayoutChangeActionListener(LayoutName.ISOMLayout, this));
        mntmLayout.add(mntmLayoutISOM);

        JMenuItem mntmLayoutTree = new JMenuItem("Spanning Tree");
        mntmLayoutTree.addActionListener(new LayoutChangeActionListener(LayoutName.TreeLayout, this));
        mntmLayout.add(mntmLayoutTree);

        JMenuItem mntmLayoutFR = new JMenuItem("Fruchterman-Reingold");
        mntmLayoutFR.addActionListener(new LayoutChangeActionListener(LayoutName.FRLayout, this));
        mntmLayout.add(mntmLayoutFR);

        JMenuItem mntmLayoutCircle = new JMenuItem("Circle");
        mntmLayoutCircle.addActionListener(new LayoutChangeActionListener(LayoutName.CircleLayout, this));
        mntmLayout.add(mntmLayoutCircle);

        JCheckBoxMenuItem mntmShowLegend = new JCheckBoxMenuItem("Show measures legend");
        mntmShowLegend.setSelected(globalDisplayOptions.isShowLegendWanted());
        mnPrefs.add(mntmShowLegend);
        mntmShowLegend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                boolean remove = ((JCheckBoxMenuItem) e.getSource()).getState();

                Swing2DView.this.globalDisplayOptions.setShowLegend(remove);
                Swing2DView.this.graphGrid.updateGlobalOptions();
            }
        });
    }

    private void initializeDatabaseMenu(final JMenuBar menuBar) {
        JMenu mnDatabase = new JMenu("Database");
        menuBar.add(mnDatabase);

        final DbConfigDialog dbConfigDialog = new DbConfigDialog(this);
        dbConfigDialog.setName("dbConfigDialog");
        for (EventType typ : DbConfigDialog.TRIGGERED_EVENTS) {
            dbConfigDialog.addEventListener(this, typ);
        }

        JMenuItem mntmConnect = new JMenuItem("Connect");
        mntmConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                dbConfigDialog.setVisible(true);
            }
        });
        mnDatabase.add(mntmConnect);

        mnLastConections = new JMenu("Last Connections");
        mnLastConections.setEnabled(false);
        mnDatabase.add(mnLastConections);

        mntmDisconnect = new JMenuItem("Disconnect");
        mntmDisconnect.setEnabled(false);
        mntmDisconnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                fireEvent(EventType.dbDisconnect, null);
            }
        });
        mnDatabase.add(mntmDisconnect);
    }

    private void initializeFileMenu(final JMenuBar menuBar) {
        JMenu mnFile = new JMenu("File");
        menuBar.add(mnFile);

        JMenuItem mntmLoadPerspective = new JMenuItem("Load Perspective");
        mntmLoadPerspective.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                int returnVal = fileChooser.showOpenDialog(Swing2DView.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    fireEvent(EventType.viewStateLoad, fileChooser.getSelectedFile().getAbsolutePath());
                }
            }
        });
        mnFile.add(mntmLoadPerspective);

        JMenuItem mntmSavePerspective = new JMenuItem("Save Perspective");
        mntmSavePerspective.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                int returnVal = fileChooser.showSaveDialog(Swing2DView.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    fireEvent(EventType.viewStateSaved, fileChooser.getSelectedFile().getAbsolutePath());
                }
            }
        });
        mnFile.add(mntmSavePerspective);

        JSeparator separator = new JSeparator();
        mnFile.add(separator);

        JMenuItem mntmQuit = new JMenuItem("Quit");
        mntmQuit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                fireEvent(EventType.shutdownTriggered, Swing2DView.this);
            }
        });
        mnFile.add(mntmQuit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addEventListener(final IEventHandler l, final EventType event) {
        listeners.get(event).add(l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeEventListener(final IEventHandler l, final EventType event) {
        listeners.get(event).remove(l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setGraphs(final Collection<? extends IOLAPGraph> graphs) {
        graphGrid.setGraphs(graphs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConnectionList(final List<String> names) {
        mnLastConections.setEnabled(false);
        mnLastConections.removeAll();
        for (final String name : names) {
            JMenuItem item = new JMenuItem(name);
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    fireEvent(EventType.dbConfigSelected, name);
                }
            });
            mnLastConections.add(item);
        }
        mnLastConections.setEnabled(true);
    }

    protected void fireEvent(final EventType type, final Object obj) {
        event(new EventArgs(this, type, obj));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setViewState(final IViewState rawState) {
        // Clean up the interface.
        initialize();

        Swing2DState state = (Swing2DState) rawState;
        xSpinner.setValue(state.getXSpinner());
        ySpinner.setValue(state.getYSpinner());
        globalDisplayOptions.updateFrom(state.getDisplayOptions());
        hierarchyTree.setSelectedCube(state.getCube());
        hierarchyTree.setState(state.getHierarchyTree());
        graphGrid.setState(state.getGraphGrid());

        // TODO is this necessary?
        graphGrid.updateGlobalOptions();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IViewState getViewState() {
        Swing2DState state = new Swing2DState();
        state.setGraphGrid(graphGrid.getState());
        state.setCube(hierarchyTree.getSelectedCube());
        state.setHierarchyTree(hierarchyTree.getState());
        state.setXSpinner((Double) xSpinner.getValue());
        state.setYSpinner((Double) ySpinner.getValue());
        state.setDisplayOptions(globalDisplayOptions);
        return state;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCubesList(final List<String> names) {
        graphGrid.clear();
        hierarchyTree.setCubesList(names);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConnected(final boolean connected) {
        if (connected) {
            mntmDisconnect.setEnabled(true);
        } else {
            mntmDisconnect.setEnabled(false);
            hierarchyTree.clear();
            hierarchyTree.setCubesList(new LinkedList<String>());
            graphGrid.clear();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        setVisible(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        hierarchyTree.shutdown();
        graphGrid.shutdown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void displayError(final String err) {
        JOptionPane.showMessageDialog(this, err, err, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void event(final EventArgs e) {
        // Proxy the event to all known listeners.
        for (IEventHandler handler : listeners.get(e.getType())) {
            handler.event(e);
        }
    }

    @Override
    public void setResponseTime(final String time) {
        responseTime.setText(time);
    }

    @Override
    public void getFresh() {
        hierarchyTree.getFresh();
        graphGrid.getFresh();
    }
}
