package de.logotakt.logolyze.model.olap;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.util.Iterator;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.xhochy.carameldb.CaramelFixture;
import com.xhochy.carameldb.CaramelRunner;

import de.logotakt.logolyze.model.interfaces.BogusDbConnectionException;
import de.logotakt.logolyze.model.interfaces.DbMalformedException;
import de.logotakt.logolyze.model.interfaces.DimensionType;
import de.logotakt.logolyze.model.interfaces.ICube;
import de.logotakt.logolyze.model.interfaces.IDimension;
import de.logotakt.logolyze.model.interfaces.IHierarchy;
import de.logotakt.logolyze.model.interfaces.IHierarchyLevel;
import de.logotakt.logolyze.model.interfaces.IHierarchyLevelValue;
import de.logotakt.logolyze.model.interfaces.IMeasureType;
import de.logotakt.logolyze.model.interfaces.MeasureAssociation;
import de.logotakt.logolyze.model.interfaces.MeasureClass;

/**
 * Testsuite to test the parsing of a metadata tree.
 */
@RunWith(CaramelRunner.class)
public class MetadataParserTest {

    @Inject
    @Named("testDB")
    private Connection connection;

    /**
     * Test the parsing of an empty database.
     * @throws BogusDbConnectionException Exception on a failed operation.
     * @throws DbMalformedException Exception on a failed operation.
     */
    @Test
    @CaramelFixture("/db-fixtures/empty.yml")
    public void testParsingEmptyDb() throws BogusDbConnectionException, DbMalformedException {
        DbStructure structure = MetadataParser.parseMetadata(connection);
        assertThat(structure.iterator()).hasSize(0);
    }

    /**
     * Test the parsing of a database with only cubes.
     * @throws DbMalformedException Exception on a failed operation.
     * @throws BogusDbConnectionException Exception on a failed operation.
     */
    @Test
    @CaramelFixture("/db-fixtures/cube-only.yml")
    public void testParsingCubeOnly() throws BogusDbConnectionException, DbMalformedException {
        DbStructure structure = MetadataParser.parseMetadata(connection);
        assertThat(structure.iterator()).hasSize(2);
        boolean foundLogolyze = false;
        boolean foundJimmy = false;
        for (ICube iCube : structure) {
            if (iCube.getName().equals("logolyze")) {
                foundLogolyze = true;
            } else if (iCube.getName().equals("jimmy's")) {
                foundJimmy = true;
            }
        }
        assertTrue(foundJimmy);
        assertTrue(foundLogolyze);
    }

    /**
     * Test the parsing of a database with empty dimensions.
     * @throws DbMalformedException Exception on a failed operation.
     * @throws BogusDbConnectionException Exception on a failed operation.
     */
    @Test
    @CaramelFixture("/db-fixtures/empty-dimensions.yml")
    public void testParsingEmptyDimensions() throws BogusDbConnectionException, DbMalformedException {
        DbStructure structure = MetadataParser.parseMetadata(connection);
        ICube cube = structure.iterator().next();
        assertThat(cube.iterator()).hasSize(2);
        IDimension geography = cube.getDimension("GEOGRAPHY");
        assertThat(geography.getName()).isEqualTo("GEOGRAPHY");
        assertThat(geography.getType()).isEqualTo(DimensionType.tDimension);
        assertThat(geography.iterator()).hasSize(0);
        IDimension times = cube.getDimension("TIMES");
        assertThat(times.getName()).isEqualTo("TIMES");
        assertThat(times.getType()).isEqualTo(DimensionType.iDimension);
        assertThat(times.iterator()).hasSize(0);
    }

    /**
     * Test the parsing of a not known dimension type.
     * @throws BogusDbConnectionException Exception on a failed operation.
     * @throws DbMalformedException Exception on a failed operation.
     */
    @Test(expected = DbMalformedException.class)
    @CaramelFixture("/db-fixtures/malformed-dimensions.yml")
    public void testParsingMalformedDimensions() throws BogusDbConnectionException, DbMalformedException {
        MetadataParser.parseMetadata(connection);
    }

    /**
     * Test the parsing of empty hierarchies.
     * @throws BogusDbConnectionException Exception on a failed operation.
     * @throws DbMalformedException Exception on a failed operation.
     */
    @Test(expected = DbMalformedException.class)
    @CaramelFixture("/db-fixtures/empty-hierarchies.yml")
    public void testParsingEmptyHierarchies() throws BogusDbConnectionException, DbMalformedException {
        MetadataParser.parseMetadata(connection);
    }

    /**
     * Test the parsing of empty hierarchylevels.
     * @throws BogusDbConnectionException Exception on a failed operation.
     * @throws DbMalformedException Exception on a failed operation.
     */
    @Test
    @CaramelFixture("/db-fixtures/empty-hierarchylevels.yml")
    public void testParsingEmptyHierarchyLevels() throws BogusDbConnectionException, DbMalformedException {
        DbStructure structure = MetadataParser.parseMetadata(connection);
        ICube cube = structure.iterator().next();

        // GEOGRAPHY
        IDimension geography = cube.getDimension("GEOGRAPHY");
        IHierarchy hGeography = geography.getHierarchy("GEOGRAPHY");
        assertThat(hGeography.iterator()).hasSize(2);
        IHierarchyLevel zipcode = hGeography.getLevel("ZIPCODE");
        assertThat(zipcode.getName()).isEqualTo("ZIPCODE");
        assertThat(zipcode.getValues()).hasSize(0);
        IHierarchyLevel address = hGeography.getLevel("ADDRESS");
        assertThat(address.getName()).isEqualTo("ADDRESS");
        assertThat(address.getValues()).hasSize(0);

        // TIMES
        IDimension times = cube.getDimension("TIMES");
        IHierarchy hTimes = times.getHierarchy("TIMES");
        assertThat(hTimes.iterator()).hasSize(2);
        IHierarchyLevel day = hTimes.getLevel("DAY");
        assertThat(day.getName()).isEqualTo("DAY");
        assertThat(day.getValues()).hasSize(0);
        IHierarchyLevel month = hTimes.getLevel("MONTH");
        assertThat(month.getName()).isEqualTo("MONTH");
        assertThat(month.getValues()).hasSize(0);
    }

    /**
     * Test the parsing of a filled database structure.
     * @throws BogusDbConnectionException Exception on a failed operation.
     * @throws DbMalformedException Exception on a failed operation.
     */
    @Test
    @CaramelFixture("/db-fixtures/fullmetadatatree.yml")
    public void testParsingFilledStructure() throws BogusDbConnectionException, DbMalformedException {
        DbStructure structure = MetadataParser.parseMetadata(connection);
        ICube cube = structure.iterator().next();

        // GEOGRAPHY
        IDimension geography = cube.getDimension("GEOGRAPHY");
        IHierarchy hGeography = geography.getHierarchy("GEOGRAPHY");
        // // ZIPCODE
        IHierarchyLevel zipcode = hGeography.getLevel("ZIPCODE");
        assertThat(zipcode.getValues()).hasSize(2);
        Iterator<IHierarchyLevelValue> it = zipcode.iterator();
        boolean read76131 = false;
        boolean read76133 = false;
        while (it.hasNext()) {
            IHierarchyLevelValue value = it.next();
            assertThat(value.getValue()).matches("7613(1|3)");
            assertThat(value.getLevel()).isSameAs(zipcode);
            if (value.getValue().equals("76131")) {
                read76131 = true;
            } else if (value.getValue().equals("76133")) {
                read76133 = true;
            }
        }
        assertTrue(read76131);
        assertTrue(read76133);
        // // ADDRESS
        IHierarchyLevel address = hGeography.getLevel("ADDRESS");
        assertThat(address.getValues()).hasSize(2);
        assertThat(address.parentLevel()).isEqualTo(zipcode);
        it = address.iterator();
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

        // TIMES
        IDimension times = cube.getDimension("TIMES");
        IHierarchy hTimes = times.getHierarchy("TIMES");
        // // DAY
        IHierarchyLevel day = hTimes.getLevel("DAY");
        assertThat(day.getValues()).hasSize(2);
        it = day.getValues().iterator();
        boolean read20100501 = false;
        boolean read20100602 = false;
        while (it.hasNext()) {
            IHierarchyLevelValue value = it.next();
            assertThat(value.getValue()).matches("2010-0(5-01|6-02)");
            if (value.getValue().equals("2010-06-02")) {
                assertThat(value.parentValue().getValue()).isEqualTo("2010-06");
                read20100602 = true;
            } else if (value.getValue().equals("2010-05-01")) {
                assertThat(value.parentValue().getValue()).isEqualTo("2010-05");
                read20100501 = true;
            }
        }
        assertTrue(read20100602);
        assertTrue(read20100501);
        // // MONTH
        IHierarchyLevel month = hTimes.getLevel("MONTH");
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
    }

    /**
     * Test the parsing of a filled database structure.
     * @throws BogusDbConnectionException Exception on a failed operation.
     * @throws DbMalformedException Exception on a failed operation.
     */
    @Test
    @CaramelFixture("/db-fixtures/fullmetadatatree_measures.yml")
    public void testParsingFilledMeasuresStructure() throws BogusDbConnectionException, DbMalformedException {
        DbStructure structure = MetadataParser.parseMetadata(connection);
        ICube cube = structure.iterator().next();
        for (IMeasureType measureType : cube.getMeasureTypes()) {
            if (measureType.getKey().equals("CARGO_AVG")) {
                assertThat(measureType.getAssoc()).isEqualTo(MeasureAssociation.edgeMeasure);
                assertThat(measureType.getMeasureClass()).isEqualTo(MeasureClass.NumeralMeasure);
            } else if (measureType.getKey().equals("CARGO_MIN")) {
                assertThat(measureType.getAssoc()).isEqualTo(MeasureAssociation.edgeMeasure);
                assertThat(measureType.getMeasureClass()).isEqualTo(MeasureClass.NumeralMeasure);
            } else if (measureType.getKey().equals("STORAGE_CAPACITY")) {
                assertThat(measureType.getAssoc()).isEqualTo(MeasureAssociation.nodeMeasure);
                assertThat(measureType.getMeasureClass()).isEqualTo(MeasureClass.NumeralMeasure);
            } else if (measureType.getKey().equals("CATEGORY")) {
                assertThat(measureType.getAssoc()).isEqualTo(MeasureAssociation.nodeMeasure);
                assertThat(measureType.getMeasureClass()).isEqualTo(MeasureClass.OtherMeasure);
            } else if (measureType.getKey().equals("REMARK")) {
                assertThat(measureType.getAssoc()).isEqualTo(MeasureAssociation.edgeMeasure);
                assertThat(measureType.getMeasureClass()).isEqualTo(MeasureClass.OtherMeasure);
            } else {
                // It should fail because of an unexpected measure.
                assertTrue(false);
            }
        }
    }
}
