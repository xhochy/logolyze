package de.logotakt.logolyze.view.swing2d;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.algorithms.filters.KNeighborhoodFilter;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.algorithms.shortestpath.MinimumSpanningForest;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * This class layouts a JUNG graph by constructing a spanning tree and layouting this with a TreeLayout.
 * @param <E> The type of edges in the graph to layout.
 * @param <V> The type of vertices in the graph to layout.
 */
class SpanningTreeLayout<V, E> implements Layout<V, E> {

    /**
     * This class implements a Map which maps any value to a given constant.
     * @param <K> The type of keys in this map
     * @param <V> The type of values in this map
     */
    class ConstantMap<K, NV> implements Map<K, NV> {
        private NV result;

        /**
         * Construct a ConstantMap from the result it should return for every query.
         * @param result The constant to return for every query.
         */
        public ConstantMap(final NV result) {
            this.result = result;
        }

        public void clear() {
        }

        /**
         * Will always claim to contain the given Key.
         * @param key The key to check for.
         */
        public boolean containsKey(final Object key) {
            return true;
        }

        /**
         * The Map contains the Value if it's equal to the constant passed.
         * @param value The value to check.
         */
        public boolean containsValue(final Object value) {
            return value == result;
        }

        public Set<Map.Entry<K, NV>> entrySet() {
            return Collections.emptySet();
        }

        public NV get(final Object key) {
            return result;
        }

        public boolean isEmpty() {
            return true;
        }

        public Set<K> keySet() {
            return Collections.emptySet();
        }

        public NV put(final K key, final NV value) {
            throw new UnsupportedOperationException();
        }

        public void putAll(final Map<? extends K, ? extends NV> m) {
            throw new UnsupportedOperationException();
        }

        public NV remove(final Object key) {
            throw new UnsupportedOperationException();
        }

        public int size() {
            return 1;
        }

        public Collection<NV> values() {
            ArrayList<NV> resultList = new ArrayList<NV>(1);
            resultList.add(result);
            return resultList;
        }
    }

    private TreeLayout<V, E> baseLayout;
    private Graph<V, E> graph;
    private int distX, distY;

    SpanningTreeLayout(final Graph<V, E> graph) {
        this(graph, TreeLayout.DEFAULT_DISTX, TreeLayout.DEFAULT_DISTY);
    }

    SpanningTreeLayout(final Graph<V, E> graph, final int distX, final int distY) {
        this.graph = graph;

        this.distX = distX;
        this.distY = distY;
        createSpanningTrees();
    }

    // private void createSpanningTrees() {
    // Forest<V, E> spanningTrees = new DelegateForest<V, E>();
    // MinimumSpanningForest<V, E> algorithm =
    // new MinimumSpanningForest<V, E>(graph, spanningTrees, null, new ConstantMap<E, Double>(1.0));
    // spanningTrees = algorithm.getForest();

    // baseLayout = new TreeLayout<V, E>(spanningTrees, distX, distY);
    // }

    /**
     * Add Graph g2 to Graph g1.
     */
    private void mergeInto(final Graph<V, E> g1, final Graph<V, E> g2) {
        for (V v : g2.getVertices()) {
            g1.addVertex(v);
        }

        for (E e : g2.getEdges()) {
            Pair<V> vertices = g2.getEndpoints(e);
            g1.addEdge(e, vertices.getFirst(), vertices.getSecond());
        }
    }

    private void createSpanningTrees() {
        // Split the graph into connected components
        WeakComponentClusterer<V, E> clusterer = new WeakComponentClusterer<V, E>();
        Set<Set<V>> connectedComponents = clusterer.transform(graph);

        // Get a node-induced subgraph for each component
        Collection<Graph<V, E>> componentGraphs = new ArrayList<Graph<V, E>>();
        for (Set<V> component : connectedComponents) {
            KNeighborhoodFilter<V, E> filter = new KNeighborhoodFilter<V, E>(component, 1,
                    KNeighborhoodFilter.EdgeType.IN_OUT);
            componentGraphs.add(filter.transform(graph));
        }

        // In each subgraph, find the node with the most connections.
        // Build an MST from this node.
        // TODO A plain old unweighted MST is probably not the best thing to do here.
        // Think about alternatives.
        Collection<Forest<V, E>> msts = new ArrayList<Forest<V, E>>();
        for (Graph<V, E> component : componentGraphs) {
            V centralNode = null;
            int bestDegree = 0;

            for (V n : component.getVertices()) {
                if (component.degree(n) > bestDegree) {
                    centralNode = n;
                    bestDegree = component.degree(centralNode);
                }
            }

            Forest<V, E> spanningTree = new DelegateForest<V, E>();
            MinimumSpanningForest<V, E> algorithm = new MinimumSpanningForest<V, E>(component, spanningTree,
                    centralNode, new ConstantMap<E, Double>(1.0));
            spanningTree = algorithm.getForest();
            msts.add(spanningTree);
        }

        // Put all the MSTs together.
        Forest<V, E> allMSTs = new DelegateForest<V, E>();
        for (Forest<V, E> mst : msts) {
            mergeInto(allMSTs, mst);
        }

        // Use them as the base layout
        baseLayout = new TreeLayout<V, E>(allMSTs, distX, distY);
    }

    public Graph<V, E> getGraph() {
        return graph;
    }

    public Dimension getSize() {
        return baseLayout.getSize();
    }

    public void initialize() {
        baseLayout.initialize();
    }

    public boolean isLocked(final V vert) {
        return baseLayout.isLocked(vert);
    }

    public void lock(final V vert, final boolean state) {
        baseLayout.lock(vert, state);
    }

    public void reset() {
        baseLayout.reset();
    }

    public void setGraph(final Graph<V, E> graph) {
        this.graph = graph;
        createSpanningTrees();
    }

    public void setInitializer(final Transformer<V, Point2D> init) {
        baseLayout.setInitializer(init);
    }

    public void setLocation(final V v, final Point2D loc) {
        baseLayout.setLocation(v, loc);
    }

    public void setSize(final Dimension d) {
        // FIXME what is wrong with this?
        // baseLayout.setSize(d);
    }

    public Point2D transform(final V v) {
        return baseLayout.transform(v);
    }
}
