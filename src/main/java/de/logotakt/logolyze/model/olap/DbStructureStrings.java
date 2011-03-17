package de.logotakt.logolyze.model.olap;

/**
 * This class is a utility class to just centrally store all the statically configured
 * strings regarding the database.
 */
public final class DbStructureStrings {
    static final String GRAPH_TABLE = "GRAPH";
    static final String GRAPH_ID = "G_ID";
    static final String GRAPH_TOPO_ATTR = "TOPOLOGY_ID";

    static final String EDGE_TABLE = "EDGE";
    static final String EDGE_GRAPH_FK = "G_ID";
    static final String EDGE_ID = "E_ID";
    static final String EDGE_N1_FK = "N1_ID";
    static final String EDGE_N2_FK = "N2_ID";

    static final String NODE_TABLE = "NODE";
    static final String NODE_TOPO_FK = "TOPOLOGY_ID";
    static final String NODE_ID = "N_ID";
    static final String NODE_LABEL_ATTR = "LABEL";

    static final String HIERARCHY_DEFAULT = "ALL";

    private DbStructureStrings() {
    }
}
