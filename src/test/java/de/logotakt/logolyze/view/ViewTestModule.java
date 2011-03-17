package de.logotakt.logolyze.view;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import de.logotakt.logolyze.model.config.ConnectionConfig;
import de.logotakt.logolyze.model.interfaces.IConstraint;
import de.logotakt.logolyze.model.interfaces.ICube;
import de.logotakt.logolyze.model.interfaces.IDimension;
import de.logotakt.logolyze.model.interfaces.IEdge;
import de.logotakt.logolyze.model.interfaces.IHierarchy;
import de.logotakt.logolyze.model.interfaces.IHierarchyLevel;
import de.logotakt.logolyze.model.interfaces.IHierarchyLevelValue;
import de.logotakt.logolyze.model.interfaces.IMeasureType;
import de.logotakt.logolyze.model.interfaces.INode;
import de.logotakt.logolyze.model.interfaces.IOLAPGraph;
import de.logotakt.logolyze.model.interfaces.IRequest;
import de.logotakt.logolyze.model.interfaces.MeasureAssociation;
import de.logotakt.logolyze.model.interfaces.MeasureClass;
import de.logotakt.logolyze.model.olap.MeasureType;
import de.logotakt.logolyze.view.interfaces.EventArgs;
import de.logotakt.logolyze.view.interfaces.EventType;
import de.logotakt.logolyze.view.interfaces.IAxisConfigurationView;
import de.logotakt.logolyze.view.interfaces.IConnectionEditView;
import de.logotakt.logolyze.view.interfaces.IConnectionListView;
import de.logotakt.logolyze.view.interfaces.IEventHandler;
import de.logotakt.logolyze.view.interfaces.IHierarchyTreeView;
import de.logotakt.logolyze.view.interfaces.ILogolyzeView;
import de.logotakt.logolyze.view.interfaces.IMeasureConfigurationView;

/**
 * Module which injects all needed mocked instances.
 */
public class ViewTestModule extends AbstractModule {

    /**
     * Provides an {@link Answer} to a dbConfigSelected event.
     */
    private final class DbConfigSelectedAnswer implements Answer<Void> {
        @Override
        public Void answer(final InvocationOnMock invocation) throws Throwable {
            EventArgs args = (EventArgs) invocation.getArguments()[0];
            // IConnectionListView view = (IConnectionListView) args.getDetails();

            if (args.getCaller() instanceof ILogolyzeView) {
                List<String> cubeslist = new ArrayList<String>();
                cubeslist.add("null");
                cubeslist.add("eins");
                cubeslist.add("unendlich");
                ((ILogolyzeView) args.getCaller()).setCubesList(cubeslist);
                ((ILogolyzeView) args.getCaller()).setConnected(true);
            }

            return null;
        }
    }

    /**
     * Provides a {@link Answer} to a treeLoad event.
     */
    private final class TreeLoadAnswer implements Answer<Void> {
        @Override
        public Void answer(final InvocationOnMock invocation) throws Throwable {
            EventArgs args = (EventArgs) invocation.getArguments()[0];
            IHierarchyTreeView tree = (IHierarchyTreeView) args.getDetails();
            tree.addNode(null, "foo", false, false);
            tree.addNode("foo", "bar", false, false);
            tree.addNode("foo", "floink", false, false);
            tree.addNode("floink", "meh", true, false);
            tree.addNode("floink", "yeah", true, false);

            tree.addNode("bar", "baz", true, false);
            tree.addNode("bar", "quux", true, false);

            return null;
        }
    }

    private List<String> selectedValuesTimes;
    private List<String> selectedValuesTimesWeekday;
    private IDimension dimension;
    private IHierarchy xHierarchy;
    private IHierarchy yHierarchy;
    private IHierarchyLevel xHierarchyLevel;
    private IHierarchyLevel yHierarchyLevel;
    private List<String> hierarchies;
    private List<String> hierarchyLevelsTimes;
    private List<String> valuesTimes;
    private List<String> emptyList = new ArrayList<String>();
    private List<String> dimensions;
    private List<String> databaseConnectionNames;
    private String activeEditedConnection;

    private IMeasureType msr1;
    private IMeasureType msr2;
    private IMeasureType msr3;
    private IMeasureType msr4;

    @Override
    protected void configure() {
        configureDatabaseConnections();
        configureHierarchyTreeStrings();
        configureGraphs();
        configureController();
    }

    private void configureController() {
        IEventHandler controller = mock(IEventHandler.class);
        final List<ConnectionConfig> connections = new LinkedList<ConnectionConfig>();

        // Handle axisConfigShowing
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable {
                EventArgs args = (EventArgs) invocation.getArguments()[0];
                IAxisConfigurationView view = (IAxisConfigurationView) args.getDetails();
                // Remark: We will not store any state!
                view.setDimensions(dimensions);
                view.setHierarchies(emptyList);
                view.setHierarchyLevels(emptyList);
                view.setValues(emptyList);
                return null;
            }
        }).when(controller).event(EventArgsEventMatcher.any(EventType.axisConfigShowing));

        doAnswer(new Answer<Void>() {
            private String lastSelectedDimension;
            private String lastSelectedHierarchy;
            private String lastSelectedHierarchyLevel;

            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable {
                EventArgs args = (EventArgs) invocation.getArguments()[0];
                IAxisConfigurationView view = (IAxisConfigurationView) args.getDetails();

                // Is dimension changed?
                if (view.getSelectedDimension() == null) {
                    lastSelectedDimension = null;
                    lastSelectedHierarchy = null;
                    lastSelectedHierarchyLevel = null;
                    view.setHierarchies(emptyList);
                    view.setHierarchyLevels(emptyList);
                    view.setValues(emptyList);
                } else if (!view.getSelectedDimension().equals(lastSelectedDimension)) {
                    lastSelectedDimension = view.getSelectedDimension();
                    lastSelectedHierarchy = null;
                    lastSelectedHierarchyLevel = null;
                    view.setHierarchies(hierarchies);
                    view.setHierarchyLevels(emptyList);
                    view.setValues(emptyList);
                } else if (!view.getSelectedHierarchy().equals(lastSelectedHierarchy)) {
                    lastSelectedHierarchy = view.getSelectedHierarchy();
                    lastSelectedHierarchyLevel = null;
                    view.setHierarchyLevels(hierarchyLevelsTimes);
                    view.setValues(emptyList);
                } else if (!view.getSelectedHierarchyLevel().equals(lastSelectedHierarchyLevel)) {
                    lastSelectedHierarchyLevel = view.getSelectedHierarchyLevel();
                    view.setValues(valuesTimes);
                }
                return null;
            }
        }).when(controller).event(EventArgsEventMatcher.any(EventType.axisConfigChanged));

        // Handle connection selected
        doAnswer(new DbConfigSelectedAnswer()).when(controller).event(
                EventArgsEventMatcher.any(EventType.dbConfigSelected));

        // handle treeLoad
        doAnswer(new TreeLoadAnswer()).when(controller).event(EventArgsEventMatcher.any(EventType.treeLoad));

        // handle measures
        mockMeasures();

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable {
                EventArgs args = (EventArgs) invocation.getArguments()[0];
                IMeasureConfigurationView view = (IMeasureConfigurationView) args.getDetails();

                List<IMeasureType> msrs = Arrays.asList(new IMeasureType[] {msr1, msr2, msr3, msr4 });
                view.setMeasures(msrs);

                return null;
            }
        }).when(controller).event(EventArgsEventMatcher.any(EventType.measuresChanging));

        answerConnectionEvents(controller, connections);
        bind(IEventHandler.class).annotatedWith(Names.named("controller")).toInstance(controller);
    }

    private void answerConnectionEvents(final IEventHandler controller, final List<ConnectionConfig> connections) {
        doAnswer(new Answer<Void>() {
            public Void answer(final InvocationOnMock invocation) {
                EventArgs args = (EventArgs) invocation.getArguments()[0];
                IConnectionListView view = (IConnectionListView) args.getDetails();

                List<String> names = new LinkedList<String>();

                for (ConnectionConfig cfg : connections) {
                    names.add(cfg.getName());
                }

                view.setConnectionNames(names);
                return null;
            }
        }).when(controller).event(EventArgsEventMatcher.any(EventType.connectionListShowing));

        doAnswer(new Answer<Void>() {
            public Void answer(final InvocationOnMock invocation) {
                EventArgs args = (EventArgs) invocation.getArguments()[0];
                String connName = (String) args.getDetails();

                ConnectionConfig fcfg = null;

                for (ConnectionConfig cfg : connections) {
                    if (cfg.getName().equals(connName)) {
                        fcfg = cfg;
                    }
                }

                if (fcfg == null) {
                    throw new RuntimeException("Could not find specified config.");
                }

                connections.remove(fcfg);
                return null;
            }
        }).when(controller).event(EventArgsEventMatcher.any(EventType.dbConfigRemoved));

        doAnswer(new Answer<Void>() {
            public Void answer(final InvocationOnMock invocation) {
                EventArgs args = (EventArgs) invocation.getArguments()[0];
                IConnectionEditView view = (IConnectionEditView) args.getDetails();

                ConnectionConfig cfg = new ConnectionConfig(view.getConnectionString(), view.getInitializationString(),
                        view.getConnectionName());

                connections.add(cfg);
                return null;
            }
        }).when(controller).event(EventArgsEventMatcher.any(EventType.dbConfigCreated));

        doAnswer(new Answer<Void>() {
            public Void answer(final InvocationOnMock invocation) {
                EventArgs args = (EventArgs) invocation.getArguments()[0];
                IConnectionEditView edit = (IConnectionEditView) args.getCaller();

                activeEditedConnection = (String) args.getDetails();
                ConnectionConfig fcfg = null;

                for (ConnectionConfig cfg : connections) {
                    if (cfg.getName().equals(activeEditedConnection)) {
                        fcfg = cfg;
                    }
                }

                if (fcfg == null) {
                    throw new RuntimeException("Could not find specified config.");
                }

                edit.setConnectionName(fcfg.getName());
                edit.setConnectionString(fcfg.getConnectionString());
                edit.setInitializationString(fcfg.getInitString());

                return null;
            }
        }).when(controller).event(EventArgsEventMatcher.any(EventType.dbConfigChanging));

        doAnswer(new Answer<Void>() {
            public Void answer(final InvocationOnMock invocation) {
                EventArgs args = (EventArgs) invocation.getArguments()[0];
                IConnectionEditView edit = (IConnectionEditView) args.getDetails();

                ConnectionConfig fcfg = null;

                for (ConnectionConfig cfg : connections) {
                    if (cfg.getName().equals(activeEditedConnection)) {
                        fcfg = cfg;
                    }
                }

                if (fcfg == null) {
                    throw new RuntimeException("Could not find specified config.");
                }

                fcfg.setName(edit.getConnectionName());
                fcfg.setConnectionString(edit.getConnectionString());
                fcfg.setInitString(edit.getInitializationString());

                return null;
            }
        }).when(controller).event(EventArgsEventMatcher.any(EventType.dbConfigChanged));
    }

    private void mockMeasures() {
        msr1 = mock(MeasureType.class);
        msr2 = mock(MeasureType.class);
        msr3 = mock(MeasureType.class);
        msr4 = mock(MeasureType.class);

        when(msr1.getKey()).thenReturn("Measure 1");
        when(msr2.getKey()).thenReturn("Measure 2");
        when(msr3.getKey()).thenReturn("Measure 3");
        when(msr4.getKey()).thenReturn("Measure 4");

        when(msr1.getAssoc()).thenReturn(MeasureAssociation.edgeMeasure);
        when(msr2.getAssoc()).thenReturn(MeasureAssociation.edgeMeasure);
        when(msr3.getAssoc()).thenReturn(MeasureAssociation.nodeMeasure);
        when(msr4.getAssoc()).thenReturn(MeasureAssociation.nodeMeasure);

        when(msr1.getMeasureClass()).thenReturn(MeasureClass.NumeralMeasure);
        when(msr2.getMeasureClass()).thenReturn(MeasureClass.NumeralMeasure);
        when(msr3.getMeasureClass()).thenReturn(MeasureClass.NumeralMeasure);
        when(msr4.getMeasureClass()).thenReturn(MeasureClass.OtherMeasure);
    }

    private void configureDatabaseConnections() {
        databaseConnectionNames = new ArrayList<String>();
        databaseConnectionNames.add("Local Server");
        databaseConnectionNames.add("Company Server");
        bind(new TypeLiteral<List<String>>() {
        }).annotatedWith(Names.named("database connection names")).toInstance(databaseConnectionNames);
    }

    private void configureGraphs() {
        List<IOLAPGraph> graphs = new ArrayList<IOLAPGraph>(9);

        dimension = mock(IDimension.class);
        when(dimension.getName()).thenReturn(TestConstants.SELECTED_DIMENSION);
        when(dimension.toString()).thenReturn(TestConstants.SELECTED_DIMENSION);

        xHierarchy = mock(IHierarchy.class);
        when(xHierarchy.getName()).thenReturn(TestConstants.SELECTED_X_HIERARCHY);
        when(xHierarchy.toString()).thenReturn(TestConstants.SELECTED_X_HIERARCHY);

        yHierarchy = mock(IHierarchy.class);
        when(yHierarchy.getName()).thenReturn(TestConstants.SELECTED_Y_HIERARCHY);
        when(yHierarchy.toString()).thenReturn(TestConstants.SELECTED_Y_HIERARCHY);

        xHierarchyLevel = mock(IHierarchyLevel.class);
        when(xHierarchyLevel.getName()).thenReturn(TestConstants.SELECTED_X_HIERARCHYLEVEL);
        when(xHierarchyLevel.toString()).thenReturn(TestConstants.SELECTED_X_HIERARCHYLEVEL);

        yHierarchyLevel = mock(IHierarchyLevel.class);
        when(yHierarchyLevel.getName()).thenReturn(TestConstants.SELECTED_Y_HIERARCHYLEVEL);
        when(yHierarchyLevel.toString()).thenReturn(TestConstants.SELECTED_Y_HIERARCHYLEVEL);

        for (final String valueTimes : selectedValuesTimes) {
            for (final String valueTimesWeekday : selectedValuesTimesWeekday) {
                final List<IConstraint> constraints = new ArrayList<IConstraint>(2);
                constraints.add(makeXConstraint(valueTimes));
                constraints.add(makeYConstraint(valueTimesWeekday));

                // Request
                IRequest request = mock(IRequest.class);
                when(request.iterator()).thenAnswer(new Answer<Iterator<IConstraint>>() {
                    @Override
                    public Iterator<IConstraint> answer(final InvocationOnMock invocation) throws Throwable {
                        return constraints.iterator();
                    }
                });
		// Cube for the Request
		ICube cube = mock(ICube.class);
		when(cube.getName()).thenReturn("testcube");
		when(cube.isDirected()).thenReturn(true);
		when(request.getCube()).thenReturn(cube);

                graphs.add(makeGraph(request));
            }
        }
        bind(new TypeLiteral<List<IOLAPGraph>>() {
        }).annotatedWith(Names.named("graphs")).toInstance(graphs);
    }

    private IOLAPGraph makeGraph(final IRequest request) {
        IOLAPGraph realGraph = new MockedGraph();
        ((MockedGraph) realGraph).setRequest(request);
        INode node1 = mock(INode.class);
        when(node1.getLabel()).thenReturn("N1");
        realGraph.addVertex(node1);
        INode node2 = mock(INode.class);
        when(node2.getLabel()).thenReturn("N2");
        realGraph.addVertex(node2);
        INode node3 = mock(INode.class);
        when(node3.getLabel()).thenReturn("N3");
        realGraph.addVertex(node3);
        realGraph.addEdge(mock(IEdge.class), node1, node2);
        realGraph.addEdge(mock(IEdge.class), node2, node1);
        realGraph.addEdge(mock(IEdge.class), node2, node3);

        // We use spies instead of mocks as we don't want to mock JUNG's functionality.
        // IOLAPGraph graph = spy(realGraph);
        IOLAPGraph graph = realGraph;
        //when(graph.getResultOf()).thenReturn(request);
        return graph;
    }

    private IConstraint makeXConstraint(final String valueTimes) {
        // X-axis constraint
        IConstraint constraint = mock(IConstraint.class);
        when(constraint.getDimension()).thenReturn(dimension);
        when(constraint.getHierarchy()).thenReturn(xHierarchy);
        when(constraint.getHierarchyLevel()).thenReturn(xHierarchyLevel);
        when(constraint.getValues()).thenAnswer(new Answer<Collection<? extends IHierarchyLevelValue>>() {
            @Override
            public Collection<? extends IHierarchyLevelValue> answer(final InvocationOnMock invocation)
                    throws Throwable {
                ArrayList<IHierarchyLevelValue> values = new ArrayList<IHierarchyLevelValue>();
                IHierarchyLevelValue value = mock(IHierarchyLevelValue.class);
                when(value.getValue()).thenReturn(valueTimes);
                when(value.toString()).thenReturn(valueTimes);
                values.add(value);
                return values;
            }
        });
        return constraint;
    }

    private IConstraint makeYConstraint(final String valueTimesWeekday) {
        // Y-axis constraint
        IConstraint constraint = mock(IConstraint.class);
        when(constraint.getDimension()).thenReturn(dimension);
        when(constraint.getHierarchy()).thenReturn(yHierarchy);
        when(constraint.getHierarchyLevel()).thenReturn(yHierarchyLevel);
        when(constraint.getValues()).thenAnswer(new Answer<Collection<? extends IHierarchyLevelValue>>() {
            @Override
            public Collection<? extends IHierarchyLevelValue> answer(final InvocationOnMock invocation)
                    throws Throwable {
                ArrayList<IHierarchyLevelValue> values = new ArrayList<IHierarchyLevelValue>();
                IHierarchyLevelValue value = mock(IHierarchyLevelValue.class);
                when(value.getValue()).thenReturn(valueTimesWeekday);
                when(value.toString()).thenReturn(valueTimesWeekday);
                values.add(value);
                return values;
            }
        });
        return constraint;
    }

    /**
     * Creates lists of Strings that represent a simple DBStructure
     */
    private void configureHierarchyTreeStrings() {
        dimensions = new ArrayList<String>();
        dimensions.add(TestConstants.SELECTED_DIMENSION);
        dimensions.add(TestConstants.OTHER_DIMENSION);
        bind(new TypeLiteral<List<String>>() {
        }).annotatedWith(Names.named("dimensions")).toInstance(dimensions);

        hierarchies = new ArrayList<String>();
        hierarchies.add(TestConstants.SELECTED_X_HIERARCHY);
        hierarchies.add("TIMES_WEEK");
        hierarchies.add(TestConstants.SELECTED_Y_HIERARCHY);
        bind(new TypeLiteral<List<String>>() {
        }).annotatedWith(Names.named("hierarchies TIMES")).toInstance(hierarchies);

        hierarchyLevelsTimes = new ArrayList<String>();
        hierarchyLevelsTimes.add("YEAR");
        hierarchyLevelsTimes.add(TestConstants.SELECTED_X_HIERARCHYLEVEL);
        hierarchyLevelsTimes.add("DAY");
        bind(new TypeLiteral<List<String>>() {
        }).annotatedWith(Names.named("levels TIMES")).toInstance(hierarchyLevelsTimes);

        List<String> hierarchyLevelsTimesWeekday = new ArrayList<String>();
        hierarchyLevelsTimesWeekday.add("YEAR");
        hierarchyLevelsTimesWeekday.add(TestConstants.SELECTED_Y_HIERARCHYLEVEL);
        hierarchyLevelsTimesWeekday.add("DAY");
        bind(new TypeLiteral<List<String>>() {
        }).annotatedWith(Names.named("levels TIMES_WEEKDAY")).toInstance(hierarchyLevelsTimesWeekday);
        valuesTimes = new ArrayList<String>();
        valuesTimes.add("2010-01");
        valuesTimes.add("2010-02");
        valuesTimes.add("2010-03");
        valuesTimes.add("2010-04");
        valuesTimes.add("2010-05");
        valuesTimes.add("2010-06");
        bind(new TypeLiteral<List<String>>() {
        }).annotatedWith(Names.named("values TIMES")).toInstance(valuesTimes);

        List<String> valuesTimesWeekday = new ArrayList<String>();
        valuesTimesWeekday.add(TestConstants.SELECTED_Y_VALUE_1);
        valuesTimesWeekday.add("tuesday");
        valuesTimesWeekday.add(TestConstants.SELECTED_Y_VALUE_2);
        valuesTimesWeekday.add("thursday");
        valuesTimesWeekday.add("friday");
        valuesTimesWeekday.add(TestConstants.SELECTED_Y_VALUE_3);
        valuesTimesWeekday.add("sunday");
        bind(new TypeLiteral<List<String>>() {
        }).annotatedWith(Names.named("values TIMES_WEEKDAY")).toInstance(valuesTimesWeekday);

        selectedValuesTimes = new ArrayList<String>();
        selectedValuesTimes.add(TestConstants.SELECTED_X_VALUE_1);
        selectedValuesTimes.add(TestConstants.SELECTED_X_VALUE_2);
        selectedValuesTimes.add(TestConstants.SELECTED_X_VALUE_3);
        bind(new TypeLiteral<List<String>>() {
        }).annotatedWith(Names.named("selected values TIMES")).toInstance(selectedValuesTimes);

        selectedValuesTimesWeekday = new ArrayList<String>();
        selectedValuesTimesWeekday.add(TestConstants.SELECTED_Y_VALUE_1);
        selectedValuesTimesWeekday.add(TestConstants.SELECTED_Y_VALUE_2);
        selectedValuesTimesWeekday.add(TestConstants.SELECTED_Y_VALUE_3);
        bind(new TypeLiteral<List<String>>() {
        }).annotatedWith(Names.named("selected values TIMES_WEEKDAY")).toInstance(selectedValuesTimesWeekday);
    }

}
