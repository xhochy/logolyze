package de.logotakt.logolyze.model.olap;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.fest.assertions.Delta;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.xhochy.carameldb.CaramelFixture;
import com.xhochy.carameldb.CaramelRunner;

import de.logotakt.logolyze.model.interfaces.BogusDbConnectionException;
import de.logotakt.logolyze.model.interfaces.DbConnectFailedException;
import de.logotakt.logolyze.model.interfaces.DbMalformedException;
import de.logotakt.logolyze.model.interfaces.DimensionType;
import de.logotakt.logolyze.model.interfaces.IConstraint;
import de.logotakt.logolyze.model.interfaces.ICube;
import de.logotakt.logolyze.model.interfaces.IDbStructure;
import de.logotakt.logolyze.model.interfaces.IDimension;
import de.logotakt.logolyze.model.interfaces.IEdge;
import de.logotakt.logolyze.model.interfaces.IHierarchy;
import de.logotakt.logolyze.model.interfaces.IHierarchyLevel;
import de.logotakt.logolyze.model.interfaces.IHierarchyLevelValue;
import de.logotakt.logolyze.model.interfaces.IMeasureType;
import de.logotakt.logolyze.model.interfaces.IModelDataFactory;
import de.logotakt.logolyze.model.interfaces.INode;
import de.logotakt.logolyze.model.interfaces.IOLAPGraph;
import de.logotakt.logolyze.model.interfaces.IRequest;
import de.logotakt.logolyze.model.interfaces.IResponse;
import de.logotakt.logolyze.model.interfaces.MeasureAssociation;
import de.logotakt.logolyze.model.interfaces.MeasureClass;
import de.logotakt.logolyze.model.interfaces.RequestValidationFailedException;

/**
 * Test the functionality of a {@link OLAPEngine}.
 */
@RunWith(CaramelRunner.class)
public class OLAPEngineTest {
    @Inject
    @Named("testJDBCString")
    private String jdbcString;

    private OLAPEngine engine;

    private ICube cube;

    private IDimension dimTimes;

    private IHierarchy hiTimes;

    private IHierarchyLevel hlDay;

    private IDimension dimGeography;

    private IHierarchy hiGeography;

    private IHierarchyLevel hlZipcode;

    private IRequest request;

    private IHierarchyLevelValue hlv20100501;

    private IHierarchyLevel hlAddress;

    private IMeasureType measureStorageCapacity;

    private IMeasureType measureCargoMinimum;

    private IMeasureType measureCargoAverage;

    private IMeasureType measureCategory;

    private IMeasureType measureRemark;

    /**
     * Close the connection to the database.
     */
    private void disconnect() {
        engine.shutdown();
    }

    /**
     * Open the connection to the database.
     * @throws DbConnectFailedException Exception on a failed operation.
     * @throws BogusDbConnectionException Exception on a failed operation.
     * @throws DbMalformedException Exception on a failed operation.
     */
    private void connect() throws DbConnectFailedException, BogusDbConnectionException, DbMalformedException {
        engine = new OLAPEngine();
        engine.openDbConnection(jdbcString, "");
        assertTrue(engine.isConnected());
    }

    /**
     * Load the DbStructure out of the database and fill in some instance variables that are used in other tests.
     */
    private void loadDbStructure() {
        IDbStructure structure = engine.getDbStructure();
        assertThat(structure.iterator()).hasSize(1);
        cube = structure.iterator().next();
        assertThat(cube.getName()).isEqualTo("logolyze");
        assertThat(structure.getCube("logolyze")).isEqualTo(cube);
        assertThat(cube.iterator()).hasSize(2);
        dimGeography = cube.getDimension("GEOGRAPHY");
        assertThat(dimGeography.getName()).isEqualTo("GEOGRAPHY");
        assertThat(dimGeography.getType()).isEqualTo(DimensionType.tDimension);
        assertThat(dimGeography.toString()).isEqualTo("(T) GEOGRAPHY");
        assertThat(dimGeography.iterator()).hasSize(1);
        hiGeography = dimGeography.getHierarchy("GEOGRAPHY");
        assertThat(hiGeography.getName()).isEqualTo("GEOGRAPHY");
        assertThat(hiGeography.toString()).isEqualTo(hiGeography.getName());
        assertThat(hiGeography.iterator()).hasSize(2);
        hlZipcode = hiGeography.getLevel("ZIPCODE");
        assertThat(hlZipcode.getValues()).hasSize(2);
        assertThat(hlZipcode.toString()).isEqualTo("ZIPCODE");
        Iterator<IHierarchyLevelValue> it = hlZipcode.iterator();
        boolean read76131 = false;
        boolean read76133 = false;
        while (it.hasNext()) {
            IHierarchyLevelValue value = it.next();
            assertThat(value.getValue()).matches("7613(1|3)");
            assertThat(value.getLevel()).isSameAs(hlZipcode);
            if (value.getValue().equals("76131")) {
                read76131 = true;
            } else if (value.getValue().equals("76133")) {
                read76133 = true;
            }
        }
        assertTrue(read76131);
        assertTrue(read76133);
        hlAddress = hiGeography.getLevel("ADDRESS");
        assertThat(hlAddress.getValues()).hasSize(2);
        it = hlAddress.iterator();
        boolean readHadiko = false;
        boolean readEntropia = false;
        while (it.hasNext()) {
            IHierarchyLevelValue value = it.next();
            assertThat(value.getValue()).matches("(Entropia|HaDiKo)");
            if (value.getValue().equals("Entropia")) {
                assertThat(value.parentValue().getValue()).isEqualTo("76133");
                readEntropia = true;
            } else if (value.getValue().equals("HaDiKo")) {
                assertThat(value.parentValue().getValue()).isEqualTo("76131");
                readHadiko = true;
            }
        }
        assertTrue(readEntropia);
        assertTrue(readHadiko);

        dimTimes = cube.getDimension("TIMES");
        assertThat(dimTimes.getName()).isEqualTo("TIMES");
        assertThat(dimTimes.iterator()).hasSize(1);
        assertThat(dimTimes.getType()).isEqualTo(DimensionType.iDimension);
        hiTimes = dimTimes.getHierarchy("TIMES");
        assertThat(hiTimes.getName()).isEqualTo("TIMES");
        assertThat(hiTimes.iterator()).hasSize(2);
        hlDay = hiTimes.getLevel("DAY");
        assertThat(hlDay.getValues()).hasSize(2);
        it = hlDay.getValues().iterator();
        boolean read20100501 = false;
        boolean read20100602 = false;
        while (it.hasNext()) {
            IHierarchyLevelValue value = it.next();
            assertThat(value.getValue()).matches("2010-0(5-01|6-02)");
            if (value.getValue().equals("2010-06-02")) {
                assertThat(value.parentValue().getValue()).isEqualTo("2010-06");
                // IHierarchyLevelValue hlv20100602 = value;
                read20100602 = true;
            } else if (value.getValue().equals("2010-05-01")) {
                assertThat(value.parentValue().getValue()).isEqualTo("2010-05");
                hlv20100501 = value;
                read20100501 = true;
            }
        }
        assertTrue(read20100602);
        assertTrue(read20100501);
        // // MONTH
        IHierarchyLevel month = hiTimes.getLevel("MONTH");
        assertThat(month.getValues()).hasSize(2);
        it = month.getValues().iterator();
        boolean read201006 = false;
        boolean read201005 = false;
        while (it.hasNext()) {
            IHierarchyLevelValue value = it.next();
            assertThat(value.getValue()).matches("2010-0(6|5)");
            if (value.getValue().equals("2010-05")) {
                read201005 = true;
            } else if (value.getValue().equals("2010-06")) {
                read201006 = true;
            }
        }
        assertTrue(read201005);
        assertTrue(read201006);

        for (IMeasureType measureType : cube.getMeasureTypes()) {
            if (measureType.getKey().equals("CARGO_AVG")) {
                assertThat(measureType.getAssoc()).isEqualTo(MeasureAssociation.edgeMeasure);
                assertThat(measureType.getMeasureClass()).isEqualTo(MeasureClass.NumeralMeasure);
                measureCargoAverage = measureType;
            } else if (measureType.getKey().equals("CARGO_MIN")) {
                assertThat(measureType.getAssoc()).isEqualTo(MeasureAssociation.edgeMeasure);
                assertThat(measureType.getMeasureClass()).isEqualTo(MeasureClass.NumeralMeasure);
                measureCargoMinimum = measureType;
            } else if (measureType.getKey().equals("STORAGE_CAPACITY")) {
                assertThat(measureType.getAssoc()).isEqualTo(MeasureAssociation.nodeMeasure);
                assertThat(measureType.getMeasureClass()).isEqualTo(MeasureClass.NumeralMeasure);
                measureStorageCapacity = measureType;
            } else if (measureType.getKey().equals("CATEGORY")) {
                assertThat(measureType.getAssoc()).isEqualTo(MeasureAssociation.nodeMeasure);
                assertThat(measureType.getMeasureClass()).isEqualTo(MeasureClass.OtherMeasure);
                measureCategory = measureType;
            } else if (measureType.getKey().equals("REMARK")) {
                assertThat(measureType.getAssoc()).isEqualTo(MeasureAssociation.edgeMeasure);
                assertThat(measureType.getMeasureClass()).isEqualTo(MeasureClass.OtherMeasure);
                measureRemark = measureType;
            } else {
                // It should fail because of an unexpected measure.
                assertTrue(false);
            }
        }
    }

    /**
     * Build a request with a i- and a t-constraint.
     */
    private void buildRequest() {
        // Get the factory.
        IModelDataFactory factory = engine.getDataFactory();

        request = factory.makeRequest(cube);
        assertThat(request.getCube()).isSameAs(cube);

        // Constraint of a i dimension.
        List<IHierarchyLevelValue> values = new ArrayList<IHierarchyLevelValue>(1);
        values.add(hlv20100501);
        IConstraint iConstraint = factory.makeConstraint(dimTimes, hiTimes, hlDay, values);
        assertThat(iConstraint.getDimension()).isSameAs(dimTimes);
        assertThat(iConstraint.getHierarchy()).isSameAs(hiTimes);
        assertThat(iConstraint.getHierarchyLevel()).isSameAs(hlDay);
        assertThat(iConstraint.getValues()).containsOnly(hlv20100501);

        // Constraint of a t dimension.
        values = new ArrayList<IHierarchyLevelValue>();
        IConstraint tConstraint = factory.makeConstraint(dimGeography, hiGeography, hlAddress, values);
        assertThat(tConstraint.getDimension()).isSameAs(dimGeography);
        assertThat(tConstraint.getHierarchy()).isSameAs(hiGeography);
        assertThat(tConstraint.getHierarchyLevel()).isSameAs(hlAddress);
        assertThat(tConstraint.getValues()).isEmpty();

        // Add the constraints.
        request.addConstraint(tConstraint);
        request.addConstraint(iConstraint);

        // Add all measures.
        request.addMeasureType(measureCargoAverage);
        request.addMeasureType(measureCargoMinimum);
        request.addMeasureType(measureStorageCapacity);
        request.addMeasureType(measureRemark);
        request.addMeasureType(measureCategory);

        assertThat(request.iterator()).containsOnly(iConstraint, tConstraint);
    }

    /**
     * Disconnecting without a connection should be silent as shutdown is called to clean resources, not only for
     * disconnecting.
     */
    @Test
    @CaramelFixture("/db-fixtures/fullmetadatatree_measures.yml")
    public void uselessDisconnect() {
        OLAPEngine engine = new OLAPEngine();
        engine.shutdown();
    }

    /**
     * There should be an exception thrown if want to get the {@link IDbStructure} without connecting.
     */
    @Test(expected = Exception.class)
    @CaramelFixture("/db-fixtures/fullmetadatatree_measures.yml")
    public void failOnDbStructureNoConnection() {
        OLAPEngine engine = new OLAPEngine();
        engine.getDbStructure();
    }

    /**
     * Tests if connecting and disconnecting works.
     * @throws DbConnectFailedException Exception on a failed operation.
     * @throws BogusDbConnectionException Exception on a failed operation.
     * @throws DbMalformedException Exception on a failed operation.
     */
    @Test
    @CaramelFixture("/db-fixtures/fullmetadatatree_measures.yml")
    public void testConnectDisconnect() throws DbConnectFailedException, BogusDbConnectionException,
            DbMalformedException {
        // This test may seem useless but it is a good indicator in the list of tests if already the connection could
        // not be opened.
        connect();
        disconnect();
    }

    /**
     * Test the loading of the {@link IDbStructure}.
     * @throws DbMalformedException DbConnectFailedException
     * @throws BogusDbConnectionException DbConnectFailedException
     * @throws DbConnectFailedException DbConnectFailedException
     */
    @Test
    @CaramelFixture("/db-fixtures/fullmetadatatree_measures.yml")
    public void testDbStructure() throws DbConnectFailedException, BogusDbConnectionException, DbMalformedException {
        connect();
        loadDbStructure();
        disconnect();
    }

    /**
     * Tests the usage of {@link IModelDataFactory}.
     * @throws DbConnectFailedException Exception on a failed operation.
     * @throws BogusDbConnectionException Exception on a failed operation.
     * @throws DbMalformedException Exception on a failed operation.
     */
    @Test
    @CaramelFixture("/db-fixtures/fullmetadatatree_measures.yml")
    public void testDataFactory() throws DbConnectFailedException, BogusDbConnectionException, DbMalformedException {
        connect();
        loadDbStructure();
        buildRequest();
        disconnect();
    }

    /**
     * Tests the loading of some graphs.
     * @throws DbConnectFailedException Exception on a failed operation.
     * @throws BogusDbConnectionException Exception on a failed operation.
     * @throws DbMalformedException Exception on a failed operation.
     * @throws RequestValidationFailedException Exception on a failed operation.
     */
    @Test
    @CaramelFixture("/db-fixtures/fullmetadatatree_measures.yml")
    public void testLoadGraphs() throws DbConnectFailedException, BogusDbConnectionException, DbMalformedException,
            RequestValidationFailedException {
        connect();
        loadDbStructure();
        buildRequest();

        IResponse response = engine.handleRequest(request);
        assertThat(response.getRequestDuration()).isGreaterThanOrEqualTo(0);
        Collection<IOLAPGraph> graphs = response.getGraphs();
        assertThat(graphs).hasSize(1);
        for (IOLAPGraph iolapGraph : graphs) {
            assertThat(iolapGraph.getResultOf().getCube()).isSameAs(cube);
            for (IConstraint constraint : iolapGraph.getResultOf()) {
                if (constraint.getDimension().equals(dimGeography)) {
                    assertThat(constraint.getHierarchy()).isSameAs(hiGeography);
                    assertThat(constraint.getHierarchyLevel()).isSameAs(hlAddress);
                } else if (constraint.getDimension().equals(dimTimes)) {
                    assertThat(constraint.getHierarchy()).isSameAs(hiTimes);
                    assertThat(constraint.getHierarchyLevel()).isSameAs(hlDay);
                }
            }

            assertThat(iolapGraph.getEdgeCount()).isEqualTo(5);
            assertThat(iolapGraph.getVertexCount()).isEqualTo(4);

            // Test the nodes and thier measures.
            for (INode node : iolapGraph.getVertices()) {
                if (node.getLabel().equals("HaDiKo")) {
                    assertThat(node.getMeasures().get("STORAGE_CAPACITY").getNumber()).isEqualTo(100, Delta.delta(0.1));
                    assertThat(node.getMeasures().get("CATEGORY").getText()).isEqualTo("Menschenquelle");
                } else if (node.getLabel().equals("Infobau")) {
                    assertThat(node.getMeasures().get("STORAGE_CAPACITY").getNumber()).isEqualTo(200, Delta.delta(0.1));
                    assertThat(node.getMeasures().get("CATEGORY").getText()).isEqualTo("høhere Lähranßtalt");
                } else if (node.getLabel().equals("Mensa")) {
                    assertThat(node.getMeasures().get("STORAGE_CAPACITY").getNumber()).isEqualTo(300, Delta.delta(0.1));
                    assertThat(node.getMeasures().get("CATEGORY").getText()).isEqualTo("Omnomnom");
                } else if (node.getLabel().equals("Entropia")) {
                    assertThat(node.getMeasures().get("STORAGE_CAPACITY").getNumber()).isEqualTo(23, Delta.delta(0.1));
                    assertThat(node.getMeasures().get("CATEGORY").getText()).isEqualTo("Menschensenke");
                } else {
                    // Fail on an unexpected meausre
                    assertTrue(false);
                }
            }

            // Test the edges and their measures.
            for (IEdge edge : iolapGraph.getEdges()) {
                if (edge.getMeasures().get("REMARK").getText().equals("Good morning, Hadiko")) {
                    assertThat(edge.getMeasures().get("CARGO_AVG").getNumber()).isEqualTo(42, Delta.delta(0.1));
                    assertThat(Double.parseDouble(edge.getMeasures().get("CARGO_AVG").getText())).isEqualTo(42,
                            Delta.delta(0.1));
                    assertThat(edge.getMeasures().get("CARGO_MIN").getNumber()).isEqualTo(5, Delta.delta(0.1));
                } else if (edge.getMeasures().get("REMARK").getText().equals("Hungrige Nerds...")) {
                    assertThat(edge.getMeasures().get("CARGO_AVG").getNumber()).isEqualTo(196, Delta.delta(0.1));
                    assertThat(Double.parseDouble(edge.getMeasures().get("CARGO_AVG").getText())).isEqualTo(196,
                            Delta.delta(0.1));
                    assertThat(edge.getMeasures().get("CARGO_MIN").getNumber()).isEqualTo(27, Delta.delta(0.1));
                } else if (edge.getMeasures().get("REMARK").getText().equals("Fnordwärts!")) {
                    assertThat(edge.getMeasures().get("CARGO_AVG").getNumber()).isEqualTo(7, Delta.delta(0.1));
                    assertThat(Double.parseDouble(edge.getMeasures().get("CARGO_AVG").getText())).isEqualTo(7,
                            Delta.delta(0.1));
                    assertThat(edge.getMeasures().get("CARGO_MIN").getNumber()).isEqualTo(1, Delta.delta(0.1));
                } else if (edge.getMeasures().get("REMARK").getText()
                        .equals("Andi ist mit seinem Tut und auch sonst fertig")) {
                    assertThat(edge.getMeasures().get("CARGO_AVG").getNumber()).isEqualTo(64, Delta.delta(0.1));
                    assertThat(Double.parseDouble(edge.getMeasures().get("CARGO_AVG").getText())).isEqualTo(64,
                            Delta.delta(0.1));
                    assertThat(edge.getMeasures().get("CARGO_MIN").getNumber()).isEqualTo(32, Delta.delta(0.1));
                } else if (edge.getMeasures().get("REMARK").getText().equals("Nach dem Essen ins Bett")) {
                    assertThat(edge.getMeasures().get("CARGO_AVG").getNumber()).isEqualTo(74, Delta.delta(0.1));
                    assertThat(Double.parseDouble(edge.getMeasures().get("CARGO_AVG").getText())).isEqualTo(74,
                            Delta.delta(0.1));
                    assertThat(edge.getMeasures().get("CARGO_MIN").getNumber()).isEqualTo(18, Delta.delta(0.1));
                } else {
                    // Fail on an unexpected meausre
                    assertTrue(false);
                }
            }
        }

        disconnect();
    }

    /**
     * Test if we could handle a new connection while there is still a connection enabled.
     * @throws DbConnectFailedException Exception on a failed operation.
     * @throws BogusDbConnectionException Exception on a failed operation.
     * @throws DbMalformedException Exception on a failed operation.
     */
    @Test
    public void testReconnectWithoutDisconnecting() throws DbConnectFailedException, BogusDbConnectionException,
            DbMalformedException {
        engine = new OLAPEngine();
        engine.openDbConnection(jdbcString, "SELECT * FROM META_CUBE");
        assertTrue(engine.isConnected());
        engine.openDbConnection(jdbcString, "SELECT * FROM META_CUBE");
        assertTrue(engine.isConnected());
        disconnect();
    }

    /**
     * Test if we won't handle a request without a database connection.
     * @throws RequestValidationFailedException Exception on a failed operation.
     * @throws BogusDbConnectionException Exception on a failed operation.
     * @throws DbMalformedException Exception on a failed operation.
     */
    @Test(expected = IllegalStateException.class)
    public void testRequestWithoutConnection() throws BogusDbConnectionException, DbMalformedException,
            RequestValidationFailedException {
        engine = new OLAPEngine();
        Request req = new Request(new Cube("non existing cube", false), new ArrayList<Validator>());
        engine.handleRequest(req);
    }

}
