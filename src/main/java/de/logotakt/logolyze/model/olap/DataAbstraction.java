package de.logotakt.logolyze.model.olap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.logotakt.logolyze.model.interfaces.BogusDbConnectionException;
import de.logotakt.logolyze.model.interfaces.DbMalformedException;
import de.logotakt.logolyze.model.interfaces.IConstraint;
import de.logotakt.logolyze.model.interfaces.IHierarchyLevelValue;
import de.logotakt.logolyze.model.interfaces.IOLAPGraph;
import de.logotakt.logolyze.model.interfaces.MeasureAssociation;
import de.logotakt.logolyze.model.interfaces.RequestValidationFailedException;

/**
 * The DataAbstraction handles the task to perform all actions needed for a certain request on the database, and return
 * the graphs for this request.
 */
public class DataAbstraction {

    private final Connection c;

    /**
     * Creates a new DataAbstraction for a given connection.
     * @param c The connection this DataAbstraction will operate on.
     */
    public DataAbstraction(final Connection c) {
        this.c = c;
    }

    private Request getGraphRequest(final ResultSet graphRS, final Request orig) throws BogusDbConnectionException {
        Request ret;
        Constraint newCons;
        HierarchyLevelValue hlv;
        HashSet<Hierarchy> hs;
        HashSet<Hierarchy> hierarchiesToHandle;
        String levelName;
        String levelValue;

        ret = new Request(orig.getCube(), orig.getValidators());

        hs = new HashSet<Hierarchy>();
        hierarchiesToHandle = new HashSet<Hierarchy>();

        Iterator<IConstraint> it = orig.iterator();

        while (it.hasNext()) {
            IConstraint origCons = it.next();

            if (hs.contains((Hierarchy) origCons.getHierarchy())) {
                // we already handled this Hierarchy
                continue;
            }

            /* Finding the correct HierarchyLevel */
            try {
                levelName = graphRS.getString(((Hierarchy) origCons.getHierarchy()).getGraphColumn());
            } catch (SQLException e) {
                throw new BogusDbConnectionException("Something went wrong. Sorry.", e);
            }

            if (!(origCons.getHierarchyLevel().getName().equals(levelName))) {
                // This Constraint is in the wrong HierarchyLevel. This Hierarchy
                // has to be handled later! To later check if this has happened,
                // keep track in a set
                hierarchiesToHandle.add((Hierarchy) origCons.getHierarchy());
            } else {
                // Hit it. Thus, this hierarchy is gonna be handled now, remove it
                // from the 'to do' list
                hierarchiesToHandle.remove((Hierarchy) origCons.getHierarchy());
                // This Hierarchy was handled now, add it to the "done" list.
                hs.add((Hierarchy) origCons.getHierarchy());

                if (((Hierarchy) origCons.getHierarchy()).getGraphColumnValue() == null) {
                    /*
                     * A HierarchyLevel without values was selected upon. Thus, see which HierarchyLevel this graph is
                     * in, and add a constraint with just this level, and no values.
                     */
                    newCons = new Constraint(origCons.getDimension(), origCons.getHierarchy(),
                            origCons.getHierarchyLevel(), new ArrayList<IHierarchyLevelValue>());
                } else {
                    /* Now we have to find out which value this graph has in the HierarchyLevel */
                    try {
                        levelValue = graphRS.getString(((Hierarchy) origCons.getHierarchy()).getGraphColumnValue());
                    } catch (SQLException e) {
                        throw new BogusDbConnectionException("Something went wrong, sorry.", e);
                    }

                    // Find the right HierarchyLevelValue object
                    hlv = ((HierarchyLevel) origCons.getHierarchyLevel()).valueByString(levelValue);

                    ArrayList<IHierarchyLevelValue> valueList = new ArrayList<IHierarchyLevelValue>();
                    valueList.add(hlv);

                    newCons = new Constraint(origCons.getDimension(), origCons.getHierarchy(),
                            origCons.getHierarchyLevel(), valueList);
                }
                ret.addConstraint(newCons);
            }
        }

        return ret;
    }

    private Map<Integer, Node> addNodes(final Request r, final Connection c, final OLAPGraph g,
            final PreparedStatement nodeStmt, final Map<String, MeasureSet> nodeSetMap) throws SQLException,
            DbMalformedException {
        HashMap<Integer, Node> nodeIDMap;
        Iterator<MeasureType> it;
        MeasureType mt;
        Measure m;

        nodeIDMap = new HashMap<Integer, Node>();

        nodeStmt.execute();
        ResultSet nodeRS = nodeStmt.getResultSet();

        while (nodeRS.next()) {

            Node n = new Node(nodeRS.getString(DbStructureStrings.NODE_LABEL_ATTR));
            nodeIDMap.put(nodeRS.getInt(DbStructureStrings.NODE_ID), n);

            it = r.getMeasures().iterator();

            while (it.hasNext()) {
                mt = it.next();
                MeasureSet ms;

                if (mt.getAssoc() != MeasureAssociation.nodeMeasure) {
                    // wrong type of measure
                    continue;
                }

                ms = nodeSetMap.get(mt.getKey());

                assert ms != null;

                switch (mt.getMeasureClass()) {
                case NumeralMeasure:
                    m = new DoubleMeasure(ms, nodeRS.getDouble(mt.getKey()));
                    break;
                case OtherMeasure:
			m = new StringMeasure(ms, nodeRS.getString(mt.getKey()));
                    break;
                default:
                    throw new IllegalArgumentException("There is a strange type of measure in the request");
                }

                n.addMeasure(m);
            }

            g.addVertex(n);
        }

        nodeRS.close();
        nodeStmt.close();

        return nodeIDMap;
    }

    private void addEdges(final Request r, final Connection c, final OLAPGraph g, final PreparedStatement stmt,
            final Map<Integer, Node> nodeIDMap, final Map<String, MeasureSet> edgeSetMap) throws SQLException,
            DbMalformedException {
        ResultSet edgeRS;
        Edge e;
        Iterator<MeasureType> it;
        MeasureType mt;
        Measure m;

        edgeRS = stmt.executeQuery();

        while (edgeRS.next()) {
            Node n1, n2;
            e = new Edge();

            n1 = nodeIDMap.get(edgeRS.getInt(DbStructureStrings.EDGE_N1_FK));
            if (n1 == null) {
                throw new DbMalformedException("Edge referring to nonexistant node "
                        + edgeRS.getInt(DbStructureStrings.EDGE_N1_FK) + "!");
            }

            n2 = nodeIDMap.get(edgeRS.getInt(DbStructureStrings.EDGE_N2_FK));
            if (n2 == null) {
                throw new DbMalformedException("Edge referring to nonexistant node "
                        + edgeRS.getInt(DbStructureStrings.EDGE_N2_FK) + "!");
            }

            it = r.getMeasures().iterator();

            while (it.hasNext()) {
                mt = it.next();
                MeasureSet ms;

                if (mt.getAssoc() != MeasureAssociation.edgeMeasure) {
                    // wrong type of measure
                    continue;
                }

                ms = edgeSetMap.get(mt.getKey());

                assert ms != null;

                switch (mt.getMeasureClass()) {
                case NumeralMeasure:
                    m = new DoubleMeasure(ms, edgeRS.getDouble(mt.getColumn()));
                    break;
                case OtherMeasure:
                    m = new StringMeasure(ms, edgeRS.getString(mt.getColumn()));
                    break;
                default:
                    throw new IllegalArgumentException("There is a strange type of measure in the request");
                }

                e.addMeasure(m);
            }

            g.addEdge(e, n1, n2);
        }

        edgeRS.close();
        stmt.close();
    }

    /**
     * Loads and returns the graphs asked for in the request passed.
     * @param r The request specifying which graphs to load
     * @return The graphs you asked for
     * @throws BogusDbConnectionException If anything with the db connections goes wrong
     * @throws DbMalformedException If the structure of data in the database is malformed
     * @throws RequestValidationFailedException This exception will be thrown with an error message if the request could
     *         not be validated by all validators
     */
    public Collection<IOLAPGraph> loadGraphs(final Request r) throws BogusDbConnectionException, DbMalformedException,
            RequestValidationFailedException {
        OLAPGraph g;
        HashMap<String, MeasureSet> nodeSetMap, edgeSetMap;
        Map<Integer, Node> nodeIDMap;
        Iterator<MeasureType> it;
        MeasureType mt;

        List<IOLAPGraph> ret = new LinkedList<IOLAPGraph>();

        // Before doing anything else, validate the request
        r.validate();

        PreparedStatement getGraphStmt = RequestTranslator.renderToGetGraphSQL(c, r);

        try {
            ResultSet graphRS = getGraphStmt.executeQuery();

            while (graphRS.next()) {
                Request graphRequest = this.getGraphRequest(graphRS, r);

                g = new OLAPGraph(graphRequest);

                // Attention: Assure that the next calls do not touch the row of the ResultSet
                // RequestTranslator and DataAbstraction somehow belong together.
                PreparedStatement getNodesStmt;
                PreparedStatement getEdgesStmt;
                getEdgesStmt = RequestTranslator.renderToGetEdgeSQL(c, r, graphRS.getString(DbStructureStrings.GRAPH_ID));
                getNodesStmt = RequestTranslator.renderToGetNodeSQL(c, r, graphRS.getInt(DbStructureStrings.GRAPH_TOPO_ATTR));

                nodeSetMap = new HashMap<String, MeasureSet>();
                edgeSetMap = new HashMap<String, MeasureSet>();
                it = r.getMeasures().iterator();

                while (it.hasNext()) {
                    HashMap<String, MeasureSet> curSetMap;

                    mt = it.next();

                    switch (mt.getAssoc()) {
                    case nodeMeasure:
                        curSetMap = nodeSetMap;
                        break;
                    case edgeMeasure:
                        curSetMap = edgeSetMap;
                        break;
                    default:
                        throw new IllegalArgumentException("There is a strange measure type in this request");
                    }

                    if (!(curSetMap.containsKey(mt.getKey()))) {
                        MeasureSet ms;

                        ms = new MeasureSet(mt);
                        curSetMap.put(mt.getKey(), ms);
                    } else {
                        throw new IllegalArgumentException("This request makes a duplicate request for a measure type");
                    }
                }

                try {
                    nodeIDMap = this.addNodes(r, c, g, getNodesStmt, nodeSetMap);
                } catch (SQLException exception) {
                    throw new BogusDbConnectionException("Something went wrong retrieving nodes: "
                            + exception.toString(), exception);
                }

                try {
                    this.addEdges(r, c, g, getEdgesStmt, nodeIDMap, edgeSetMap);
                } catch (SQLException exception) {
                    throw new BogusDbConnectionException("Something went wrong retrieving edges: "
                            + exception.toString(), exception);
                }

                ret.add(g);

            }
            getGraphStmt.close();
        } catch (SQLException exception) {
            throw new BogusDbConnectionException("Something with the database connection is wrong: "
                    + exception.toString(), exception);
        }

        return ret;
    }
}
