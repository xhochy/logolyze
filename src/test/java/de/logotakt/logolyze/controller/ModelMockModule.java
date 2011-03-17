package de.logotakt.logolyze.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doAnswer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import de.logotakt.logolyze.model.interfaces.BogusDbConnectionException;
import de.logotakt.logolyze.model.interfaces.DbConnectFailedException;
import de.logotakt.logolyze.model.interfaces.DbMalformedException;
import de.logotakt.logolyze.model.interfaces.DimensionType;
import de.logotakt.logolyze.model.interfaces.IConstraint;
import de.logotakt.logolyze.model.interfaces.ICube;
import de.logotakt.logolyze.model.interfaces.IDbStructure;
import de.logotakt.logolyze.model.interfaces.IDimension;
import de.logotakt.logolyze.model.interfaces.IHierarchy;
import de.logotakt.logolyze.model.interfaces.IHierarchyLevel;
import de.logotakt.logolyze.model.interfaces.IHierarchyLevelValue;
import de.logotakt.logolyze.model.interfaces.ILogolyzeModel;
import de.logotakt.logolyze.model.interfaces.IMeasureType;
import de.logotakt.logolyze.model.interfaces.IModelDataFactory;
import de.logotakt.logolyze.model.interfaces.IRequest;
import de.logotakt.logolyze.model.interfaces.IResponse;
import de.logotakt.logolyze.model.interfaces.MeasureAssociation;
import de.logotakt.logolyze.model.interfaces.MeasureClass;
import de.logotakt.logolyze.model.interfaces.RequestValidationFailedException;
import de.logotakt.logolyze.view.interfaces.EventArgs;
import de.logotakt.logolyze.view.interfaces.IAxisConfigurationView;

/**
 * Mock the Logolyze model.
 */
public class ModelMockModule extends AbstractModule {
    private IDimension mockTimesDimension() {
        // HierarchyLevelValues
        IHierarchyLevelValue v12 = mock(IHierarchyLevelValue.class);
        when(v12.getValue()).thenReturn(Constants.DBS_12);
        bind(IHierarchyLevelValue.class).annotatedWith(Names.named("hlv 2010-05-12")).toInstance(v12);

        IHierarchyLevelValue v13 = mock(IHierarchyLevelValue.class);
        when(v13.getValue()).thenReturn(Constants.DBS_13);
        bind(IHierarchyLevelValue.class).annotatedWith(Names.named("hlv 2010-05-13")).toInstance(v13);

        IHierarchyLevelValue v20 = mock(IHierarchyLevelValue.class);
        when(v20.getValue()).thenReturn(Constants.DBS_20);
        bind(IHierarchyLevelValue.class).annotatedWith(Names.named("hlv 2010-20")).toInstance(v20);

        IHierarchyLevelValue vSun = mock(IHierarchyLevelValue.class);
        when(vSun.getValue()).thenReturn(Constants.DBS_SUN);
        bind(IHierarchyLevelValue.class).annotatedWith(Names.named("hlv SUNDAY")).toInstance(vSun);

        // HierarchyLevels
        IHierarchyLevel lDay = mock(IHierarchyLevel.class);
        when(lDay.getName()).thenReturn(Constants.DBS_DAY);
        final List<IHierarchyLevelValue> dayValues = new ArrayList<IHierarchyLevelValue>();
        dayValues.add(v12);
        dayValues.add(v13);
        when(lDay.iterator()).thenAnswer(new IteratorAnswer<IHierarchyLevelValue>(dayValues));
        when(lDay.getValues()).thenReturn(dayValues);
        bind(IHierarchyLevel.class).annotatedWith(Names.named("hierarchylevel DAY")).toInstance(lDay);

        IHierarchyLevel lWeek = mock(IHierarchyLevel.class);
        when(lWeek.getName()).thenReturn(Constants.DBS_WEEK);
        final List<IHierarchyLevelValue> weekValues = new ArrayList<IHierarchyLevelValue>();
        weekValues.add(v20);
        when(lWeek.iterator()).thenAnswer(new IteratorAnswer<IHierarchyLevelValue>(weekValues));
        when(lWeek.getValues()).thenReturn(weekValues);
        bind(IHierarchyLevel.class).annotatedWith(Names.named("hierarchylevel WEEK")).toInstance(lWeek);

        IHierarchyLevel lWd = mock(IHierarchyLevel.class);
        when(lWd.getName()).thenReturn(Constants.DBS_WD);
        final List<IHierarchyLevelValue> wdValues = new ArrayList<IHierarchyLevelValue>();
        wdValues.add(vSun);
        when(lWd.iterator()).thenAnswer(new IteratorAnswer<IHierarchyLevelValue>(wdValues));
        when(lWd.getValues()).thenReturn(wdValues);
        bind(IHierarchyLevel.class).annotatedWith(Names.named("hierarchylevel WEEKDAY")).toInstance(lWd);

        // Hierarchies
        IHierarchy hTimW = mock(IHierarchy.class);
        when(hTimW.getName()).thenReturn(Constants.DBS_TIM_W);
        final List<IHierarchyLevel> timeWLevels = new ArrayList<IHierarchyLevel>();
        timeWLevels.add(lDay);
        timeWLevels.add(lWeek);
        when(hTimW.iterator()).thenAnswer(new IteratorAnswer<IHierarchyLevel>(timeWLevels));
        when(hTimW.getLevel(Constants.DBS_DAY)).thenReturn(lDay);
        when(hTimW.getLevel(Constants.DBS_WEEK)).thenReturn(lWeek);
        bind(IHierarchy.class).annotatedWith(Names.named("hierarchy TIMES_WEEK")).toInstance(hTimW);

        IHierarchy hTimWd = mock(IHierarchy.class);
        when(hTimWd.getName()).thenReturn(Constants.DBS_TIM_WD);
        final List<IHierarchyLevel> wdLevels = new ArrayList<IHierarchyLevel>();
        wdLevels.add(lWd);
        when(hTimWd.iterator()).thenAnswer(new IteratorAnswer<IHierarchyLevel>(wdLevels));
        when(hTimWd.getLevel(Constants.DBS_WD)).thenReturn(lWd);
        bind(IHierarchy.class).annotatedWith(Names.named("hierarchy TIMES_WEEKDAY")).toInstance(hTimWd);

        // Dimension
        IDimension dTimes = mock(IDimension.class);
        when(dTimes.getName()).thenReturn(Constants.DBS_TIM);
        when(dTimes.getType()).thenReturn(DimensionType.iDimension);
        final List<IHierarchy> timHierarchies = new ArrayList<IHierarchy>();
        timHierarchies.add(hTimW);
        timHierarchies.add(hTimWd);
        when(dTimes.iterator()).thenAnswer(new IteratorAnswer<IHierarchy>(timHierarchies));
        when(dTimes.getHierarchy(Constants.DBS_TIM_W)).thenReturn(hTimW);
        when(dTimes.getHierarchy(Constants.DBS_TIM_WD)).thenReturn(hTimWd);
        bind(IDimension.class).annotatedWith(Names.named("dimension TIMES")).toInstance(dTimes);

        // Returned the mocked dimension.
        return dTimes;
    }

    private boolean modelIsConnected;

    @Override
    protected void configure() {
        IDimension dTimes = mockTimesDimension();
        IDimension dLoc = mockLocationDimension();

        // Measures
        IMeasureType edgeMeasure = mock(IMeasureType.class);
        when(edgeMeasure.getAssoc()).thenReturn(MeasureAssociation.edgeMeasure);
        when(edgeMeasure.getMeasureClass()).thenReturn(MeasureClass.NumeralMeasure);
        when(edgeMeasure.getKey()).thenReturn(Constants.EDGE_MEASURE);
        bind(IMeasureType.class).annotatedWith(Names.named("edge measure")).toInstance(edgeMeasure);

        IMeasureType nodeMeasure = mock(IMeasureType.class);
        when(nodeMeasure.getAssoc()).thenReturn(MeasureAssociation.nodeMeasure);
        when(nodeMeasure.getMeasureClass()).thenReturn(MeasureClass.OtherMeasure);
        when(nodeMeasure.getKey()).thenReturn(Constants.NODE_MEASURE);
        bind(IMeasureType.class).annotatedWith(Names.named("node measure")).toInstance(nodeMeasure);

        // Cube
        ICube cube = mock(ICube.class);
        when(cube.getName()).thenReturn(Constants.SELECTED_CUBE);
        final List<IDimension> dimensions = new ArrayList<IDimension>();
        dimensions.add(dTimes);
        dimensions.add(dLoc);
        when(cube.iterator()).thenAnswer(new IteratorAnswer<IDimension>(dimensions));
        when(cube.getDimension(Constants.DBS_TIM)).thenReturn(dTimes);
        when(cube.getDimension(Constants.DBS_LOC)).thenReturn(dLoc);
        final List<IMeasureType> measures = new ArrayList<IMeasureType>();
        measures.add(edgeMeasure);
        measures.add(nodeMeasure);
        when(cube.getMeasureTypes()).thenReturn(measures);
        bind(new TypeLiteral<List<IMeasureType>>() {
        }).annotatedWith(Names.named("measures")).toInstance(measures);

        // DbStructure
        IDbStructure dbStructure = mock(IDbStructure.class);
        final List<ICube> cubes = new ArrayList<ICube>();
        cubes.add(cube);
        when(dbStructure.iterator()).thenAnswer(new IteratorAnswer<ICube>(cubes));
        when(dbStructure.getCube(Constants.SELECTED_CUBE)).thenReturn(cube);

        // Factory
        IModelDataFactory factory = mock(IModelDataFactory.class);
        when(
                factory.makeConstraint(any(IDimension.class), any(IHierarchy.class), any(IHierarchyLevel.class),
                        anyCollectionOf(IHierarchyLevelValue.class))).thenAnswer(new Answer<IConstraint>() {
            @Override
            public IConstraint answer(final InvocationOnMock invocation) throws Throwable {
                IConstraint constraint = mock(IConstraint.class);
                when(constraint.getDimension()).thenReturn((IDimension) invocation.getArguments()[0]);
                when(constraint.getHierarchy()).thenReturn((IHierarchy) invocation.getArguments()[1]);
                when(constraint.getHierarchyLevel()).thenReturn((IHierarchyLevel) invocation.getArguments()[2]);
                when(constraint.getValues()).thenAnswer(new Answer<Collection<IHierarchyLevelValue>>() {

                    @SuppressWarnings("unchecked")
                    @Override
                    public Collection<IHierarchyLevelValue> answer(final InvocationOnMock nestedInvocation)
                            throws Throwable {
                        return (Collection<IHierarchyLevelValue>) invocation.getArguments()[3];
                    }
                });
                return constraint;
            }
        });

        IResponse response = mock(IResponse.class);

        // For now, Requests made by the mocked factory are write-only dummies.
        // That's enough to make the simple tests work.
        IRequest dummy = mock(IRequest.class);
        when(factory.makeRequest(any(ICube.class))).thenReturn(dummy);

        // Model
        ILogolyzeModel model = mock(ILogolyzeModel.class);
        when(model.getDbStructure()).thenReturn(dbStructure);
        when(model.getDataFactory()).thenReturn(factory);
        try {
            when(model.handleRequest(any(IRequest.class))).thenReturn(response);
        } catch (BogusDbConnectionException e) {
            e.printStackTrace();
        } catch (DbMalformedException e) {
            e.printStackTrace();
        } catch (RequestValidationFailedException e) {
            e.printStackTrace();
        }

        try {
            doAnswer(new Answer<Void>() {
                @Override
                public Void answer(final InvocationOnMock invocation) throws Throwable {
                    modelIsConnected = true;

                    return null;
                }
            }).when(model).openDbConnection(anyString(), anyString());
        } catch (DbConnectFailedException e) {
            e.printStackTrace();
        } catch (BogusDbConnectionException e) {
            e.printStackTrace();
        } catch (DbMalformedException e) {
            e.printStackTrace();
        }

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable {
                modelIsConnected = false;
                return null;
            }
        }).when(model).shutdown();

        when(model.isConnected()).thenAnswer(new Answer<Boolean>() {
            public Boolean answer(final InvocationOnMock invocation) {
                return modelIsConnected;
            }
        });

        bind(ILogolyzeModel.class).annotatedWith(Names.named("model")).toInstance(model);
    }

    private IDimension mockLocationDimension() {
        // HierarchyLevelValue
        IHierarchyLevelValue vTop = mock(IHierarchyLevelValue.class);
        when(vTop.getValue()).thenReturn(Constants.DBS_TOP);
        bind(IHierarchyLevelValue.class).annotatedWith(Names.named("hlv TOPOLOGY")).toInstance(vTop);

        // HierarchyLevel
        IHierarchyLevel lRail = mock(IHierarchyLevel.class);
        when(lRail.getName()).thenReturn(Constants.DBS_RAIL);
        final List<IHierarchyLevelValue> railValues = new ArrayList<IHierarchyLevelValue>();
        railValues.add(vTop);
        when(lRail.iterator()).thenAnswer(new IteratorAnswer<IHierarchyLevelValue>(railValues));
        when(lRail.getValues()).thenReturn(railValues);
        bind(IHierarchyLevel.class).annotatedWith(Names.named("hierarchylevel RAIL")).toInstance(lRail);

        // Hierarchy
        IHierarchy hHub = mock(IHierarchy.class);
        when(hHub.getName()).thenReturn(Constants.DBS_HUB);
        final List<IHierarchyLevel> hubLevels = new ArrayList<IHierarchyLevel>();
        hubLevels.add(lRail);
        when(hHub.iterator()).thenAnswer(new IteratorAnswer<IHierarchyLevel>(hubLevels));
        when(hHub.getLevel(Constants.DBS_RAIL)).thenReturn(lRail);
        bind(IHierarchy.class).annotatedWith(Names.named("hierarchy HUB")).toInstance(hHub);

        // Dimension
        IDimension dLoc = mock(IDimension.class);
        when(dLoc.getName()).thenReturn(Constants.DBS_LOC);
        when(dLoc.getType()).thenReturn(DimensionType.tDimension);
        final List<IHierarchy> locHierarchies = new ArrayList<IHierarchy>();
        locHierarchies.add(hHub);
        when(dLoc.iterator()).thenAnswer(new IteratorAnswer<IHierarchy>(locHierarchies));
        when(dLoc.getHierarchy(Constants.DBS_HUB)).thenReturn(hHub);
        bind(IDimension.class).annotatedWith(Names.named("dimension LOCATION_TYPE")).toInstance(dLoc);

        // Return mocked dimension.
        return dLoc;
    }

}
