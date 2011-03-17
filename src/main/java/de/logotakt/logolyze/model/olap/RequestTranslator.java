package de.logotakt.logolyze.model.olap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.logotakt.logolyze.model.interfaces.BogusDbConnectionException;
import de.logotakt.logolyze.model.interfaces.IConstraint;
import de.logotakt.logolyze.model.interfaces.IDimension;
import de.logotakt.logolyze.model.interfaces.IHierarchy;
import de.logotakt.logolyze.model.interfaces.IHierarchyLevelValue;
import de.logotakt.logolyze.model.interfaces.MeasureAssociation;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * This is a utility class that will perform the task of generating SQL strings to retrieve the objects selected by a
 * Request object.
 */
public final class RequestTranslator {
    /**
     * This class is a utility class, i.e. should not be instantiated.
     */
    private RequestTranslator() {
    }

    private static String generateWhere(final Request r, final List<SQLArgument> arglist) {
        /*
         * This HashSet stores in which GRAPH_COLUMN_HIERARCHY we selected a value (i.e. a hierarchy level). The
         * remaining ones have to be set to ALL
         */
        HashSet<String> columnSet = new HashSet<String>();
        /*
         * This is a two-level map, in which we will sort all the constraints by Hierarchy and HierarchyLevel
         */
        Map<Hierarchy, Map<HierarchyLevel, List<IConstraint>>> levelMap;
        /*
         * First, see in which hierarchies we have constraints, and in which of those we have more than one (i.e.
         * constraints in more than one HierarchyLevel).
         */
        levelMap = new HashMap<Hierarchy, Map<HierarchyLevel, List<IConstraint>>>();

        for (IConstraint c : r) {
            if (!levelMap.containsKey((c.getHierarchy()))) {
                levelMap.put((Hierarchy) c.getHierarchy(), new HashMap<HierarchyLevel, List<IConstraint>>());
            }

            if (!(levelMap.get((Hierarchy) c.getHierarchy()).containsKey((HierarchyLevel) c.getHierarchyLevel()))) {
                (levelMap.get((Hierarchy) c.getHierarchy())).put((HierarchyLevel) c.getHierarchyLevel(),
                        new LinkedList<IConstraint>());
            }

            (levelMap.get(c.getHierarchy())).get(c.getHierarchyLevel()).add(c);
        }

        /*
         * OK, that map is built. Now we loop over each of the hierarchies, and build the constraints for them. Hereby:
         * - multiple Dimensions / Hierarchies are ANDed - multiple HierarchyLevels / HierarchyLevelValues are ORed
         */

        Iterator<Hierarchy> hit = levelMap.keySet().iterator();
        StringBuilder ret;
        ret = new StringBuilder(100);
        while (hit.hasNext()) {
            Hierarchy h = hit.next();

            ret.append(" ( ");

            Iterator<HierarchyLevel> lit = levelMap.get(h).keySet().iterator();
            while (lit.hasNext()) {
                HierarchyLevel hl = lit.next();

                ret.append(" ( ");

                // Now, loop over the constraints in this HierarchyLevel
                Iterator<IConstraint> cit = levelMap.get(h).get(hl).iterator();
                while (cit.hasNext()) {

                    Constraint c = (Constraint) cit.next();

                    if (((Hierarchy) c.getHierarchy()).getGraphColumnValue() == null) {
                        /*
                         * This constraint is in one of the no-value-hierarchies. That means: only the HierarchyLevel
                         * should be selected. Select the right HierarchyLevel
                         */
                        ret.append("( ").append(DbStructureStrings.GRAPH_TABLE).append(".").append(h.getGraphColumn())
                                .append(" = ? )");
                        arglist.add(new SQLArgument(ArgumentType.strArg, hl.getName()));
                    } else {
                        // First, select the right HierarchyLevel
                        ret.append("( ").append(DbStructureStrings.GRAPH_TABLE).append(".").append(h.getGraphColumn())
                                .append(" = ? ) AND ( ");
                        arglist.add(new SQLArgument(ArgumentType.strArg, hl.getName()));

                        Iterator<? extends IHierarchyLevelValue> vit = c.getValues().iterator();
                        // Now, loop over the values and OR them
                        while (vit.hasNext()) {
                            HierarchyLevelValue hlv = (HierarchyLevelValue) vit.next();

                            ret.append(" ( ").append(DbStructureStrings.GRAPH_TABLE).append(".")
                                    .append(h.getGraphColumnValue()).append(" = ?");
                            arglist.add(new SQLArgument(ArgumentType.strArg, hlv.getValue()));

                            if (vit.hasNext()) {
                                ret.append(" ) OR ");
                            } else {
                                ret.append(" ) ");
                            }

                        }

                        ret.append(" ) ");
                    }
                }

                if (lit.hasNext()) {
                    ret.append(" ) OR ");
                } else {
                    ret.append(" ) ");
                }
            }

            // OK, this Hierarchy has been handled. Add its column to the columnSet
            columnSet.add(h.getGraphColumn());

            if (hit.hasNext()) {
                ret.append(" ) AND ");
            } else {
                ret.append(" ) ");
            }
        }

        /*
         * Finally, we have to set all the GRAPH_COLUMN_HIERARCHYs which have not been set. We can not just loop over
         * all the hierarchies and see if they have been set, because multiple hierarchies may use the same column in
         * the GRAPH view.
         */
        Iterator<IDimension> dit = r.getCube().iterator();
        // Looping over all dimensions
        while (dit.hasNext()) {
            IDimension d = dit.next();

            Iterator<IHierarchy> ihit = d.iterator();
            // Looping over all hierarchies
            while (ihit.hasNext()) {
                Hierarchy h = (Hierarchy) ihit.next();

                if (!columnSet.contains(h.getGraphColumn())) {
                    // This hierarchy's GRAPH_COLUMN_HIERARCHY was not handled!
                    if (ret.toString().trim().isEmpty()) {
                        ret.append(" ( ");
                    } else {
                        ret.append(" AND ( ");
                    }

                    ret.append(DbStructureStrings.GRAPH_TABLE).append(".").append(h.getGraphColumn()).append(" = ? )");
                    arglist.add(new SQLArgument(ArgumentType.strArg, DbStructureStrings.HIERARCHY_DEFAULT));
                }
            }
        }

        return ret.toString();
    }

    private static String generateFields(final Request r, final MeasureAssociation ma) {
        StringBuilder ret = new StringBuilder();
        Iterator<MeasureType> it;
        MeasureType mt;
        boolean inner = false;

        it = r.getMeasures().iterator();

        while (it.hasNext()) {
            mt = it.next();

            if (mt.getAssoc() != ma) {
                // measure for the wrong objects
                continue;
            }

            if (inner) {
            	ret.append(", ");
            }
            inner = true;

            switch (mt.getAssoc()) {
            case edgeMeasure:
                ret.append(DbStructureStrings.EDGE_TABLE).append(".");
                break;
            case nodeMeasure:
                ret.append(DbStructureStrings.NODE_TABLE).append(".");
                break;
            default:
                throw new IllegalArgumentException("Unknown measure associtaion");
            }

            ret.append(mt.getColumn());
        }

        return ret.toString();
    }

    /**
     * Renders the SQL to retrieve all nodes for a given topologyId.
     * @param r The request to be fulfilled.
     * @param c The used database connection.
     * @param graphTopoID The topology id of the fetched graph.
     * @return A SQL query to retrieve the requested data.
     * @throws BogusDbConnectionException A problem with the database connection has occured.
     */
    @SuppressWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    public static PreparedStatement renderToGetNodeSQL(final Connection c, final Request r, final int graphTopoID)
            throws BogusDbConnectionException {
        StringBuilder ret = new StringBuilder(50);
        String fields;
        PreparedStatement stmt;

        fields = RequestTranslator.generateFields(r, MeasureAssociation.nodeMeasure);

        if (fields.length() > 0) {
            fields = ", " + fields;
        }

        ret.append("SELECT ").append(DbStructureStrings.NODE_TABLE).append(".").append(DbStructureStrings.NODE_ID)
                .append(", ").append(DbStructureStrings.NODE_TABLE).append(".")
                .append(DbStructureStrings.NODE_LABEL_ATTR).append(fields).append(" FROM ")
                .append(DbStructureStrings.NODE_TABLE).append(" WHERE ").append(DbStructureStrings.NODE_TABLE)
                .append(".").append(DbStructureStrings.NODE_TOPO_FK).append(" = ?");

        try {
            stmt = c.prepareStatement(ret.toString());
            stmt.setInt(1, graphTopoID);
        } catch (SQLException e) {
            throw new BogusDbConnectionException("Could not prepare statement in renderToGetNodeSQL.", e);
        }

        return stmt;
    }

    /**
     * Renders the SQL to retrieve all edges for a given graphId.
     * @param r The request to be fulfilled.
     * @param c The used database connection.
     * @param graphID The graph to get nodes for.
     * @return A SQL query to retrieve the requested data.
     * @throws BogusDbConnectionException A problem with the database connection has occured.
     */
    @SuppressWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    public static PreparedStatement renderToGetEdgeSQL(final Connection c, final Request r, final String graphID)
            throws BogusDbConnectionException {
        StringBuilder ret = new StringBuilder(100);
        String fields;
        PreparedStatement stmt;

        fields = RequestTranslator.generateFields(r, MeasureAssociation.edgeMeasure);

        if (fields.length() > 0) {
            fields = ", " + fields;
        }

        ret.append("SELECT ").append(DbStructureStrings.EDGE_TABLE).append(".").append(DbStructureStrings.EDGE_ID)
                .append(", ").append(DbStructureStrings.EDGE_TABLE).append(".").append(DbStructureStrings.EDGE_N1_FK)
                .append(", ").append(DbStructureStrings.EDGE_TABLE).append(".").append(DbStructureStrings.EDGE_N2_FK)
                .append(fields).append(" FROM ").append(DbStructureStrings.EDGE_TABLE).append(" WHERE ")
                .append(DbStructureStrings.EDGE_TABLE).append(".").append(DbStructureStrings.EDGE_GRAPH_FK)
                .append(" = ?");

        try {
            stmt = c.prepareStatement(ret.toString());
        } catch (SQLException e) {
            throw new BogusDbConnectionException("Could not prepare statement in renderToGetEdgeSQL.", e);
        }

        try {
            stmt.setString(1, graphID);
        } catch (SQLException e) {
            throw new BogusDbConnectionException("Could not set String Nr. 1 in renderToGetEdgeSQL", e);
        }

        return stmt;
    }

    /**
     * Creates a SQL query to retrieve graphs matching the request.
     * @param r The request to find matching graphs for.
     * @param c The used database connection.
     * @return The request translated to a SQL query.
     * @throws BogusDbConnectionException A problem with the database connection has occured.
     */
    @SuppressWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    public static PreparedStatement renderToGetGraphSQL(final Connection c, final Request r)
            throws BogusDbConnectionException {
        StringBuilder ret = new StringBuilder(30);
        PreparedStatement stmt;
        List<SQLArgument> arglist;
        int i = 0;

        arglist = new LinkedList<SQLArgument>();

        // TODO select only fields that are needed here
        ret.append("SELECT * FROM ").append(DbStructureStrings.GRAPH_TABLE).append(" WHERE ");
        ret.append(RequestTranslator.generateWhere(r, arglist));

        try {
            stmt = c.prepareStatement(ret.toString());

        } catch (SQLException e) {
            throw new BogusDbConnectionException("Could not prepare statement in renderToGetGraphSQL.", e);
        }

        try {
            i = 1;
            for (SQLArgument arg : arglist) {
                switch (arg.getType()) {
                case intArg:
                    stmt.setInt(i, Integer.parseInt(arg.getVal()));
                    break;
                case strArg:
                    stmt.setString(i, arg.getVal());
                    break;
                default:
                    throw new IllegalArgumentException("That argument type does not exist!");
                }

                i++;
            }
        } catch (SQLException e) {
            throw new BogusDbConnectionException("Could not assign field Nr. " + String.valueOf(i) + ": "
                    + e.getMessage(), e);
        }

        return stmt;
    }
}
