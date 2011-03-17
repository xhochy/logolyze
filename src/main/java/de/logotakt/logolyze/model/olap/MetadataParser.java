package de.logotakt.logolyze.model.olap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import de.logotakt.logolyze.model.interfaces.BogusDbConnectionException;
import de.logotakt.logolyze.model.interfaces.DbMalformedException;
import de.logotakt.logolyze.model.interfaces.DimensionType;
import de.logotakt.logolyze.model.interfaces.MeasureAssociation;
import de.logotakt.logolyze.model.interfaces.MeasureClass;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * This (utility) class is resposible for parsing the metadata stored in a database and constructig a DbStructure out of
 * it.
 */
public final class MetadataParser {

    private static final Logger logger = Logger.getLogger(MetadataParser.class);

    private static final String COULD_NOT_CREATE_STATEMENT = "Could not create statement: ";
    private static final String CUBE_TBL = "META_CUBE";
    private static final String CUBE_ID_ATTR = "CUBE_ID";
    private static final String CUBE_NAME_ATTR = "CUBE_NAME";
    private static final String CUBE_DIRECTED_ATTR = "DIRECTED_GRAPH";

    private static final String MEASURE_TBL = "META_MEASURE";
    private static final String MEASURE_CUBE_FK = "FK_CUBE";
    private static final String MEASURE_FACT_ATTR = "FACT_TABLE";
    private static final String MEASURE_NAME_ATTR = "MEASURE_NAME";
    private static final String MEASURE_COLUMN_ATTR = "FACT_TABLE_COLUMN";
    private static final String MEASURE_FACT_EDGE = "EDGE";
    private static final String MEASURE_FACT_NODE = "NODE";

    private static final String DIMENSION_TBL = "META_DIMENSION";
    private static final String DIMENSION_ID_ATTR = "DIMENSION_ID";
    private static final String DIMENSION_CUBE_FK = "FK_CUBE";
    private static final String DIMENSION_NAME_ATTR = "DIMENSION_NAME";
    private static final String DIMENSION_TYPE_ATTR = "DIMENSION_TYPE";
    private static final String DIMENSION_TYPE_INFO = "informational";
    private static final String DIMENSION_TYPE_TOPO = "topological";

    private static final String HIERARCHY_TBL = "META_HIERARCHY";
    private static final String HIERARCHY_ID_ATTR = "HIERARCHY_ID";
    private static final String HIERARCHY_DIMENSION_FK = "FK_DIMENSION";
    private static final String HIERARCHY_NAME_ATTR = "HIERARCHY_NAME";
    private static final String HIERARCHY_TABLE_ATTR = "HIERARCHY_TABLE";
    private static final String HIERARCHY_GCOLUMN_ATTR = "GRAPH_COLUMN_HIERARCHY";
    private static final String HIERARCHY_GCOLUMNVALUE_ATTR = "GRAPH_COLUMN_HIERARCHYVALUE";

    private static final String HLEVEL_TBL = "META_HIERARCHYLEVEL";
    private static final String HLEVEL_ID_ATTR = "HIERARCHY_LEVEL";
    private static final String HLEVEL_HIERARCHY_FK = "FK_HIERARCHY";
    private static final String HLEVEL_NAME_ATTR = "HIERARCHYLEVEL_NAME";

    /**
     * A private constructor, because this is a utility class that should not be instantiated.
     */
    private MetadataParser() {
    }

    private static String whereFromMap(final Map<String, String> selection, final List<SQLArgument> arglist) {
        StringBuilder where = new StringBuilder(" WHERE ");
        Iterator<Entry<String, String>> it = selection.entrySet().iterator();
        boolean hasOne = false;

        while (it.hasNext()) {
            Entry<String, String> entry = it.next();

            hasOne = true;

            // %TODO we're not escaping the column names here. May they even contain "'"?
            where.append(" ").append(entry.getKey()).append(" = ? ");
            // where.append(" ? = ? ");
            // arglist.add(new SQLArgument(ArgumentType.strArg, entry.getKey()));
            arglist.add(new SQLArgument(ArgumentType.strArg, entry.getValue()));
            if (it.hasNext()) {
                where.append("AND ");
            }
        }

        if (hasOne) {
            return where.toString();
        } else {
            return "";
        }
    }

    @SuppressWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    private static void buildHLVTree(final HierarchyLevel root, final Connection conn, final String hTable,
            final HierarchyLevelValue parent, final Map<String, String> selection) throws DbMalformedException,
            SQLException, BogusDbConnectionException {
        /*
         * Since this is only the non-recursive call to buildHLVTree, we only have to care for the root level. That
         * means: Get the root values, add them to this hierarchylevel, and perform the recursive call on them.
         */
        PreparedStatement stmt;
        HierarchyLevelValue hlv;
        LinkedList<SQLArgument> arglist;
        int i;

        arglist = new LinkedList<SQLArgument>();

        // Update the filtering
        if (selection.put(parent.getLevel().getName(), parent.getValue()) != null) {
            throw new DbMalformedException("There seem to be two HierarchyLevels with the name "
                    + parent.getLevel().getName() + " inside the same Hierarchy.");
        }

        String whereString = whereFromMap(selection, arglist);

        String query = String.format("SELECT DISTINCT %s FROM %s %s ORDER BY %s ASC", root.getName(), hTable,
                whereString, root.getName());

        try {
            stmt = conn.prepareStatement(query);

            i = 1;
            for (SQLArgument arg : arglist) {
                switch (arg.getType()) {
                case strArg:
                    stmt.setString(i, arg.getVal());
                    break;
                case intArg:
                    stmt.setInt(i, Integer.parseInt(arg.getVal()));
                    break;
                }
                i++;
            }
        } catch (SQLException e) {
            throw new BogusDbConnectionException(COULD_NOT_CREATE_STATEMENT + e.toString(), e);
        }

        ResultSet rs = null;
        try {
            rs = stmt.executeQuery();

            while (rs.next()) {
                if (rs.getString(root.getName()) == null) {
                    // a null value is foobar.
                    continue;
                }

                hlv = new HierarchyLevelValue(rs.getString(root.getName()), parent, root);
                root.addValue(hlv);
                if (root.childLevel() != null) {
                    // There are children to this level. Build the tree of them
                    buildHLVTree((HierarchyLevel) root.childLevel(), conn, hTable, hlv, selection);
                }
            }

        } catch (SQLException e) {
            throw new DbMalformedException("Could not retrieve hierarchy level value: " + e.toString(), e);
        } finally {
            // Remove the additional filtering again
            selection.remove(parent.getLevel().getName());
            if (rs != null) {
                rs.close();
            }
            stmt.close();
        }
    }

    @SuppressWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    private static void buildHLVTree(final HierarchyLevel root, final Connection conn, final String hTable)
            throws DbMalformedException, SQLException, BogusDbConnectionException {
        /*
         * Since this is only the non-recursive call to buildHLVTree, we only have to care for the root level. That
         * means: Get the root values, add them to this hierarchylevel, and perform the recursive call on them.
         */
        PreparedStatement stmt;

        String query = String.format("SELECT DISTINCT %s FROM %s ORDER BY %s ASC", root.getName(), hTable,
                root.getName());

        try {
            stmt = conn.prepareStatement(query);
        } catch (SQLException e) {
            throw new BogusDbConnectionException(COULD_NOT_CREATE_STATEMENT + e.toString(), e);
        }

        ResultSet rs = null;
        try {
            rs = stmt.executeQuery();

            while (rs.next()) {
                if (rs.getString(root.getName()) == null) {
                    // a null value is foobar.
                    continue;
                }

                HierarchyLevelValue hlv = new HierarchyLevelValue(rs.getString(root.getName()), null, root);
                root.addValue(hlv);
                if (root.childLevel() != null) {
                    buildHLVTree((HierarchyLevel) root.childLevel(), conn, hTable, hlv, new HashMap<String, String>());
                }
            }

        } catch (SQLException e) {
            throw new DbMalformedException("Could not retrieve hierarchy level value: " + e.toString(), e);
        } finally {
            if (rs != null) {
                rs.close();
            }
            stmt.close();
        }
    }

    @SuppressWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    private static void fillHierarchy(final Hierarchy h, final int hierID, final Connection conn, final String table)
            throws BogusDbConnectionException, DbMalformedException, SQLException {
        PreparedStatement stmt;
        HierarchyLevel hl, parentHl = null, root = null;

        String query = String.format("SELECT * FROM %s WHERE %s = ? ORDER BY %s DESC", HLEVEL_TBL, HLEVEL_HIERARCHY_FK,
                HLEVEL_ID_ATTR);

        try {
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, hierID);
        } catch (SQLException e) {
            throw new BogusDbConnectionException(COULD_NOT_CREATE_STATEMENT + e.toString(), e);
        }

        ResultSet rs = null;
        try {
            rs = stmt.executeQuery();

            while (rs.next()) {
                logger.debug("Adding HL " + rs.getString(HLEVEL_NAME_ATTR));
                hl = new HierarchyLevel(rs.getString(HLEVEL_NAME_ATTR), parentHl);
                if (parentHl != null) {
                    parentHl.setChild(hl);
                } else {
                    root = hl;
                }

                h.addLevel(hl);
                parentHl = hl;
            }

            if (root == null) {
                throw new DbMalformedException("Hierarchy " + h.getName() + " is empty");
            }

            // finally, build the HLV tree
            buildHLVTree(root, conn, table);

        } catch (SQLException e) {
            throw new DbMalformedException("Could not retrieve hierarchy: " + e.toString(), e);
        } finally {
            if (rs != null) {
                rs.close();
            }
            stmt.close();
        }

    }

    @SuppressWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    private static void fillDimension(final Dimension d, final int dimID, final Connection conn)
            throws BogusDbConnectionException, DbMalformedException, SQLException {
        PreparedStatement stmt;

        String query = String.format("SELECT * FROM %s WHERE %s = ?", HIERARCHY_TBL, HIERARCHY_DIMENSION_FK);

        try {
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, dimID);
        } catch (SQLException e) {
            throw new BogusDbConnectionException(COULD_NOT_CREATE_STATEMENT + e.toString(), e);
        }

        ResultSet rs = null;
        try {
            rs = stmt.executeQuery();

            while (rs.next()) {
                logger.debug("Adding Hierarchy " + rs.getString(HIERARCHY_NAME_ATTR));
                Hierarchy h = new Hierarchy(rs.getString(HIERARCHY_NAME_ATTR), rs.getString(HIERARCHY_GCOLUMN_ATTR),
                        rs.getString(HIERARCHY_GCOLUMNVALUE_ATTR));

                fillHierarchy(h, rs.getInt(HIERARCHY_ID_ATTR), conn, rs.getString(HIERARCHY_TABLE_ATTR));
                d.addHierarchy(h);
            }
        } catch (SQLException e) {
            throw new DbMalformedException("Could not retrieve hierarchy: " + e.toString(), e);
        } finally {
            if (rs != null) {
                rs.close();
            }
            stmt.close();
        }

    }

    private static MeasureClass determineMeasureClass(final Connection conn, final MeasureAssociation ma,
            final String measureColumn) throws BogusDbConnectionException, DbMalformedException, SQLException {
        PreparedStatement stmt = null;

        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM ");
        if (ma == MeasureAssociation.edgeMeasure) {
            query.append(DbStructureStrings.EDGE_TABLE);
        } else if (ma == MeasureAssociation.nodeMeasure) {
            query.append(DbStructureStrings.NODE_TABLE);
        }
        // %TODO This makes me wanna hit my head against something hard. Isn't there an other way to do this?
        query.append(" WHERE 1 = 2");

        try {
            stmt = conn.prepareStatement(query.toString());
        } catch (SQLException e) {
            throw new BogusDbConnectionException("Could not create statement: " + e.toString(), e);
        }

        ResultSet rs = null;
        int mtype;
        try {
            rs = stmt.executeQuery();

            mtype = rs.getMetaData().getColumnType(rs.findColumn(measureColumn));
        } catch (SQLException e) {
            throw new DbMalformedException("Could not determine measure class: " + e.toString(), e);
        } finally {
            if (rs != null) {
                rs.close();
            }
            stmt.close();
        }

        if (isNumericJDBCType(mtype)) {
            return MeasureClass.NumeralMeasure;
        } else {
            return MeasureClass.OtherMeasure;
        }
    }

    /*
     * %TODO Aaaaaaargh! It hurts so much! Is this really the only way to get that information out of JDBC? Perhaps
     * casting stuff to int and seeing if that throws an exception?
     */
    private static boolean isNumericJDBCType(final int type) {
        return Types.BIT == type || Types.BIGINT == type || Types.DECIMAL == type || Types.DOUBLE == type
                || Types.FLOAT == type || Types.INTEGER == type || Types.NUMERIC == type || Types.REAL == type
                || Types.SMALLINT == type || Types.TINYINT == type;
    }

    @SuppressWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    private static void cubeAddMeasures(final Cube c, final int cubeID, final Connection conn)
            throws BogusDbConnectionException, DbMalformedException, SQLException {

        PreparedStatement stmt = null;

        String query = String.format("SELECT * FROM %s WHERE %s = ?", MEASURE_TBL, MEASURE_CUBE_FK);

        try {
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, cubeID);
        } catch (SQLException e) {
            throw new BogusDbConnectionException(COULD_NOT_CREATE_STATEMENT + e.toString(), e);
        }

        ResultSet rs = null;
        try {
            rs = stmt.executeQuery();

            while (rs.next()) {
                MeasureAssociation ma;
                if (rs.getString(MEASURE_FACT_ATTR).equals(MEASURE_FACT_EDGE)) {
                    ma = MeasureAssociation.edgeMeasure;
                } else if (rs.getString(MEASURE_FACT_ATTR).equals(MEASURE_FACT_NODE)) {
                    ma = MeasureAssociation.nodeMeasure;
                } else {
                    throw new DbMalformedException("Unknown measure association type "
                            + rs.getString(MEASURE_FACT_ATTR));
                }

                String measureColumn = rs.getString(MEASURE_COLUMN_ATTR);

                c.addMeasure(new MeasureType(rs.getString(MEASURE_NAME_ATTR), ma, determineMeasureClass(conn, ma,
                        measureColumn), measureColumn));
            }
        } catch (SQLException e) {
            throw new DbMalformedException("Could not retrieve measure: " + e.toString(), e);
        } finally {
            if (rs != null) {
                rs.close();
            }
            stmt.close();
        }
    }

    @SuppressWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    private static void fillCube(final Cube c, final int cubeID, final Connection conn)
            throws BogusDbConnectionException, DbMalformedException, SQLException {
        PreparedStatement stmt;

        String query = String.format("SELECT * FROM %s WHERE %s = ?", DIMENSION_TBL, DIMENSION_CUBE_FK);

        try {
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, cubeID);
        } catch (SQLException e) {
            throw new BogusDbConnectionException(COULD_NOT_CREATE_STATEMENT + e.toString(), e);
        }

        ResultSet rs = null;
        try {
            rs = stmt.executeQuery();

            while (rs.next()) {
                DimensionType dt;
                if (rs.getString(DIMENSION_TYPE_ATTR).equals(DIMENSION_TYPE_INFO)) {
                    dt = DimensionType.iDimension;
                } else if (rs.getString(DIMENSION_TYPE_ATTR).equals(DIMENSION_TYPE_TOPO)) {
                    dt = DimensionType.tDimension;
                } else {
                    throw new DbMalformedException("Unknown dimension type " + rs.getString(DIMENSION_TYPE_ATTR));
                }

                Dimension d = new Dimension(rs.getString(DIMENSION_NAME_ATTR), dt);
                fillDimension(d, rs.getInt(DIMENSION_ID_ATTR), conn);
                c.addDimension(d);
            }

            // Finally, we have to get the types of measures available for this cube
            cubeAddMeasures(c, cubeID, conn);
        } catch (SQLException e) {
            throw new DbMalformedException("Could not retrieve dimension: " + e.toString(), e);
        } finally {
            if (rs != null) {
                rs.close();
            }
            stmt.close();
        }

    }

    /**
     * Parses the metadata in a Logotakt database into a DbStructure.
     * @param conn The connection to the database
     * @return A DbStructure object representing the metadata in the database.
     * @throws BogusDbConnectionException In case the database connection behaves oddly.
     * @throws DbMalformedException In case the structure of the database doesn't comply with our expectations.
     */
    public static DbStructure parseMetadata(final Connection conn) throws BogusDbConnectionException,
            DbMalformedException {
        DbStructure res;
        PreparedStatement stmt;

        res = new DbStructure();

        /* Read all the cubes out of the database */
        try {
            stmt = conn.prepareStatement("SELECT * FROM " + CUBE_TBL);
        } catch (SQLException e) {
            throw new BogusDbConnectionException(COULD_NOT_CREATE_STATEMENT + e.toString(), e);
        }

        ResultSet rs = null;
        try {
            rs = stmt.executeQuery();

            while (rs.next()) {
                boolean isDirected;
                int directedInt;
                
                directedInt = rs.getInt(CUBE_DIRECTED_ATTR);
                if (directedInt == 0) {
                    isDirected = false;
                } else {
                    isDirected = true;
                }
                
                Cube c = new Cube(rs.getString(CUBE_NAME_ATTR), isDirected);
                fillCube(c, rs.getInt(CUBE_ID_ATTR), conn);
                res.addCube(c);
            }

        } catch (SQLException e) {
            throw new DbMalformedException("Could not retrieve cubes: " + e.toString(), e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                stmt.close();
            } catch (SQLException e) {
                logger.error("Could not close a statement.", e);
            }
        }

        return res;
    }
}
