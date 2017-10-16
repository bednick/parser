package main.java.ru.interior.bricks;

import com.sun.istack.internal.NotNull;

import java.util.*;

/**
 * Created by BODY on 15.10.2017.
 */

public class OrientedBipartiteGraph<PA, PB> {
    private Part<PA,PB> partA;
    private Part<PB,PA> partB;

    public OrientedBipartiteGraph() {
        partA = new Part<>();
        partB = new Part<>();
    }

    public void addA(@NotNull PA vertex, @NotNull Collection<PB> previous, @NotNull Collection<PB> next) {
        partA.addVertex(vertex);
        partA.addPreviousEdges(vertex, previous);
        partA.addNextEdges(vertex, next);

        for (PB p: previous) {
            if (!partB.contains(p)) {
                partB.addVertex(p);
            }
            partB.addNextEdges(p, vertex);
        }
        for (PB n: next) {
            if (!partB.contains(n)) {
                partB.addVertex(n);
            }
            partB.addPreviousEdges(n, vertex);
        }
    }

    public void addB(@NotNull PB vertex, @NotNull Collection<PA> previous, @NotNull Collection<PA> next) {
        partB.addVertex(vertex);
        partB.addPreviousEdges(vertex, previous);
        partB.addNextEdges(vertex, next);

        for (PA p: previous) {
            if (!partA.contains(p)) {
                partA.addVertex(p);
            }
            partA.addNextEdges(p, vertex);
        }
        for (PA n: next) {
            if (!partA.contains(n)) {
                partA.addVertex(n);
            }
            partA.addPreviousEdges(n, vertex);
        }
    }

    public Set<PB> getPreviousA(@NotNull PA vertex) {
        return partA.getPreviousEdges(vertex);
    }

    public Set<PA> getPreviousB(@NotNull PB vertex) {
        return partB.getPreviousEdges(vertex);
    }

    public Set<PB> getNextA(@NotNull PA vertex) {
        return partA.getNextEdges(vertex);
    }

    public Set<PA> getNextB(@NotNull PB vertex) {
        return partB.getNextEdges(vertex);
    }

    public Set<PA> getPartsA() {
        return partA.getVertices();
    }

    public Set<PB> getPartsB() {
        return partB.getVertices();
    }

    public OrientedBipartiteGraph<PA, PB> getSubgraphB(@NotNull Collection<PB> head) {
        OrientedBipartiteGraph<PA, PB> subgraph = new OrientedBipartiteGraph<PA, PB>();
        TreeSet<PB> verified = new TreeSet<PB>();
        Queue<PB> needToCheck = new LinkedList<PB>();
        needToCheck.addAll(head);

        PB vertex;
        while ((vertex = needToCheck.poll()) != null) {
            verified.add(vertex);
            Set<PA> sources = this.getPreviousB(vertex);
            for (PA s: sources) {
                Set<PB> previous = this.getPreviousA(s);
                Set<PB> next = this.getNextA(s);
                subgraph.addA(s, previous, next);
                for (PB p: previous) {
                    if (!verified.contains(p)) {
                        needToCheck.add(p);
                    }
                }
            }
        }

        return subgraph;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Oriented Bipartite Graph:").append(System.lineSeparator());

        for (Map.Entry<PA, Pair<TreeSet<PB>,TreeSet<PB>>> entry: partA.edge.entrySet()) {
            for (PB v: entry.getValue().getKey()) {
                builder.append(v).append(" ");
            }
            builder.append("-->").append(" ");
            builder.append(entry.getKey());
            builder.append(" ").append("-->").append(" ");
            for (PB v: entry.getValue().getValue()) {
                builder.append(v).append(" ");
            }
            builder.append(System.lineSeparator());
        }

        return builder.toString();
    }

    private class Part<I,O> {
        private Map<I,Pair<TreeSet<O>,TreeSet<O>>> edge;

        Part() {
            edge = new HashMap<>();
        }

        void addVertex(I vertex) {
            edge.put(vertex, new Pair<>(new TreeSet<>(), new TreeSet<>()));
        }

        void addNextEdges(I vertex, Collection<O> next) {
            if (!edge.containsKey(vertex)) {
                this.addVertex(vertex);
            }
            edge.get(vertex).getValue().addAll(next);
        }

        void addNextEdges(I vertex, O next) {
            this.addNextEdges(vertex, Collections.singletonList(next));
        }

        void addPreviousEdges(I vertex, Collection<O> previous) {
            if (!edge.containsKey(vertex)) {
                this.addVertex(vertex);
            }
            edge.get(vertex).getKey().addAll(previous);
        }

        void addPreviousEdges(I vertex, O previous) {
            this.addPreviousEdges(vertex, Collections.singletonList(previous));
        }

        Set<O> getNextEdges(I vertex) {
            return edge.get(vertex).getValue();
        }

        Set<O> getPreviousEdges(I vertex) {
            return edge.get(vertex).getKey();
        }

        Set<I> getVertices() {
            return edge.keySet();
        }

        boolean contains(I vertex) {
            return edge.containsKey(vertex);
        }
    }
}
