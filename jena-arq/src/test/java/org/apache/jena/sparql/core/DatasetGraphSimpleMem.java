/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.sparql.core;

import java.util.* ;

import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.graph.impl.GraphBase ;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapStd;
import org.apache.jena.util.iterator.ExtendedIterator ;
import org.apache.jena.util.iterator.WrappedIterator ;

/** Very simple, non-scalable DatasetGraph implementation
 * of a triples+quads style for testing the {@link DatasetGraphTriplesQuads}
 * style implementation framework.
 */
public class DatasetGraphSimpleMem extends DatasetGraphTriplesQuads implements TransactionalNotSupportedMixin
{
    private MiniSet<Triple> triples = new MiniSet<>() ;
    private MiniSet<Quad> quads = new MiniSet<>() ;
    private PrefixMap prefixes = new PrefixMapStd();

    /** Simple abstraction of a Set */
    private static class MiniSet<T> implements Iterable<T> {
        final Collection<T> store;

        MiniSet(Collection<T> store) {
            this.store = store;
        }

        MiniSet() {
            this.store = new ArrayList<>();
        }

        void add(T t) {
            if ( !store.contains(t) )
                store.add(t);
        }

        void remove(T t) {
            store.remove(t);
        }

        @Override
        public Iterator<T> iterator() {
            return store.iterator();
        }

        boolean isEmpty() {
            return store.isEmpty();
        }

        int size() {
            return store.size();
        }
        
        boolean contains(T t) {
            return store.contains(t);
        }
    }

    public DatasetGraphSimpleMem() {}

    @Override
    public boolean supportsTransactions() {
        return false;
    }

    @Override
    public Iterator<Quad> findInDftGraph(Node s, Node p, Node o) {
        List<Quad> results = new ArrayList<>();
        for ( Triple t : triples )
            if ( matches(t, s, p, o) )
                // ?? Quad.defaultGraphNodeGenerated
                // Quad.defaultGraphIRI
                results.add(new Quad(Quad.defaultGraphIRI, t));
        return results.iterator();
    }

    @Override
    public Iterator<Quad> findInSpecificNamedGraph(Node g, Node s, Node p, Node o) {
        List<Quad> results = new ArrayList<>();
        for ( Quad q : quads )
            if ( matches(q, g, s, p, o) )
                results.add(q);
        return results.iterator();
    }

    @Override
    public Iterator<Quad> findInAnyNamedGraphs(Node s, Node p, Node o) {
        List<Quad> results = new ArrayList<>();
        for ( Quad q : quads )
            if ( matches(q, Node.ANY, s, p, o) )
                results.add(q);
        return results.iterator();
    }

    /** Convert null to Node.ANY */
    public static Node nullAsAny(Node x) { return nullAsDft(x, Node.ANY) ; }

    /** Convert null to some default Node */
    public static Node nullAsDft(Node x, Node dft) { return x==null ? dft : x ; }

    private boolean matches(Triple t, Node s, Node p, Node o) {
        s = nullAsAny(s);
        p = nullAsAny(p);
        o = nullAsAny(o);
        return t.matches(s, p, o);
    }

    private boolean matches(Quad q, Node g, Node s, Node p, Node o) {
        g = nullAsAny(g);
        s = nullAsAny(s);
        p = nullAsAny(p);
        o = nullAsAny(o);
        return q.matches(g, s, p, o);
    }

    @Override
    protected boolean addToDftGraph(Node s, Node p, Node o) {
        final Triple t = new Triple(s, p, o);
        final boolean f = triples.contains(t);
        triples.add(t);
        return !f;
    }

    @Override
    protected boolean addToNamedGraph(Node g, Node s, Node p, Node o) {
        final Quad q = new Quad(g, s, p, o);
        final boolean f = quads.contains(q);
        quads.add(q);
        return !f;
    }

    @Override
    protected boolean deleteFromDftGraph(Node s, Node p, Node o) {
        final Triple t = new Triple(s, p, o);
        final boolean f = triples.contains(t);
        triples.remove(t);
        return f;
    }

    @Override
    protected boolean deleteFromNamedGraph(Node g, Node s, Node p, Node o) {
        final Quad q = new Quad(g, s, p, o);
        final boolean f = quads.contains(q);
        quads.remove(q);
        return f;
    }

    class GraphDft extends GraphBase {
        @Override
        public boolean performAdd(Triple t) {
            final boolean f = triples.contains(t);
            triples.add(t);
            return !f;
        }

        @Override
        public boolean performDelete(Triple t) {
            final boolean f = triples.contains(t);
            triples.remove(t);
            return f;
        }

        @Override
        protected ExtendedIterator<Triple> graphBaseFind(Triple m) {
            List<Triple> results = new ArrayList<>();
            for ( Triple t : triples )
                if ( t.matches(m.getMatchSubject(), m.getMatchPredicate(), m.getMatchObject()) )
                    results.add(t);
            return WrappedIterator.create(results.iterator());
        }
    }

    class GraphNamed extends GraphBase {
        private final Node graphName;

        GraphNamed(Node gname) {
            this.graphName = gname;
        }

        @Override
        public boolean  performAdd(Triple t) {
            Quad q = new Quad(graphName, t);
            final boolean f = quads.contains(q);
            quads.add(q);
            return !f;
        }

        @Override
        public boolean performDelete(Triple t) {
            final Quad q = new Quad(graphName, t);
            final boolean f = quads.contains(q);
            quads.remove(q);
            return f;
        }

        @Override
        protected ExtendedIterator<Triple> graphBaseFind(Triple m) {
            List<Triple> results = new ArrayList<>();

            Iterator<Quad> iter = findNG(graphName, m.getMatchSubject(), m.getMatchPredicate(), m.getMatchObject());
            for ( ; iter.hasNext() ; )
                results.add(iter.next().asTriple());
            return WrappedIterator.create(results.iterator());
        }
    }

    @Override
    public Graph getDefaultGraph() {
        return new GraphDft();
    }

    @Override
    public Graph getGraph(Node graphNode) {
        return new GraphNamed(graphNode);
    }

    @Override
    public boolean containsGraph(Node graphNode) {
        return graphNodes().contains(graphNode);
    }

    @Override
    public Iterator<Node> listGraphNodes() {
        return graphNodes().iterator();
    }

    @Override
    public PrefixMap prefixes() {
        return prefixes;
    }

    private Set<Node> graphNodes() {
        Set<Node> x = new HashSet<>();
        for ( Quad q : quads )
            x.add(q.getGraph());
        return x;
    }

    @Override
    public void close() {}

    @Override
    public boolean supportsTransactionAbort() {
        return false;
    }
}
