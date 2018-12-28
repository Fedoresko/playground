package me.fmalyshev.ml;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Bioinfo {
    private enum Exons {
        A, C, G, T
    }

    private static class Node {
        List<Integer> terminalTokens = null;
        Node [] next = new Node[4];
        Node link = null;
        Node parent;
        char ch;
    }

    private static class MutlimatchTree {
        private final Node root;
        private Consumer<Collection<Integer>> consumer;
        private Node state;
        private int nNodes = 1;

        public MutlimatchTree() {
            root = new Node();
            state = root;
            nodes.add(root);
        }

        public int getSize() {
            return nNodes;
        }

        public Node build(Node state, char symbol) {
            int index = Exons.valueOf(String.valueOf(symbol)).ordinal();
            Node next = state.next[index];
            if (next == null) {
                next = new Node();
//                next.index = nNodes++;
                next.parent = state;
                next.ch = symbol;
                nodes.add(next);
                state.next[index] = next;
            }
            state = next;
            return state;
        }

        private Node getLink(Node node) {
            if (node.link == null) {
                node.link = (node == root || node.parent == root) ? root : go(getLink(node.parent), node.ch);
                if (node.link.terminalTokens != null) {
                    if (node.terminalTokens == null) {
                        node.terminalTokens = new ArrayList<>();
                    }
                    node.terminalTokens.addAll(node.link.terminalTokens);
                }
            }
            return node.link;
        }

        private Node go(Node state, char symbol) {
            int index = Exons.valueOf(String.valueOf(symbol)).ordinal();
            Node next = state.next[index];
            if (next == null) {
                if (state == root) {
                    next = root;
                } else {
                    next = go(getLink(state), symbol);
                }
            }

            state = next;
            return state;
        }


        public Node feed(char symbol) {
            state = feed(state, symbol);
            return state;
        }

        public Node feed(Node pstate, char symbol) {
            Node state = go(pstate, symbol);
            if (state.link == null)
                getLink(state);

            if (consumer != null && state.terminalTokens != null) {
                consumer.accept(state.terminalTokens);
            }

            return state;
        }

        public void setMatchesConsumer(Consumer<Collection<Integer>> consumer) {
            this.consumer = consumer;
        }
    }

    private static class MutlimatchTreeBuilder {
        MutlimatchTree tree = new MutlimatchTree();

        public MutlimatchTreeBuilder feed(String token, int tIdx) {
            Node cur = tree.root;
            for(char c : token.toCharArray()) {
                cur = tree.build(cur, c);
            }
            if (cur.terminalTokens == null) {
                cur.terminalTokens = new ArrayList<>();
            }
            cur.terminalTokens.add(tIdx);
            return this;
        }

        public MutlimatchTreeBuilder withMatchesConsumer(Consumer<Collection<Integer>> consumer) {
            tree.setMatchesConsumer(consumer);
            return this;
        }

        MutlimatchTree build() {
            return tree;
        }
    }

    private static ArrayList<Node> nodes = new ArrayList<>();

    static class Discover {
        public Discover(int nToken, int endIndex) {
            this.nToken = nToken;
            this.endIdx = endIndex;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Discover)
                return nToken == ((Discover) obj).nToken;
            return false;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(nToken);
        }

        int nToken;
        int endIdx;
    }

    static class DiscoverSub {
        BitSet discovers = new BitSet(nodes.size());
        String substring = "";
    }

    static String nodepath(Node nod) {
        return nod.parent == null ? ">" : nodepath(nod.parent) + nod.ch;
    }

    public static void main(String [] args) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        MutlimatchTreeBuilder builder = new MutlimatchTreeBuilder();
        String text = reader.readLine();
        int n = Integer.valueOf(reader.readLine());
        ArrayList<String> words = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            String word = reader.readLine();
            builder.feed(word, i);
            words.add(word);
        }

        final AtomicInteger idx = new AtomicInteger(0);
        BitSet dmatches = new BitSet(nodes.size());

        builder.withMatchesConsumer( matches -> {
            for (int nmtch : matches) {
                dmatches.set(nmtch);
            }
        });

        MutlimatchTree tree = builder.build();

        Map<Node, DiscoverSub> walkSet = new HashMap<>();
        walkSet.put(tree.root, new DiscoverSub());

        for (char ch : text.toCharArray()) {
            Map<Node, DiscoverSub> nextWalkSet = new HashMap<>();

            for (Node node : nodes) {
                dmatches.clear();
                Node nxNode = tree.feed(node, ch);

                DiscoverSub newSub = new DiscoverSub();
                DiscoverSub oldWalk = walkSet.get(node);
                if (oldWalk != null) {
                    newSub.discovers.or(oldWalk.discovers);
                    newSub.discovers.or(dmatches);
                    newSub.substring = oldWalk.substring + ch;

                    DiscoverSub newWalk = nextWalkSet.getOrDefault(nxNode, new DiscoverSub());
                    int cardinality = newWalk.discovers.cardinality();
                    if (cardinality
                            <= newSub.discovers.cardinality() || cardinality == 0) {
                        nextWalkSet.put(nxNode, newSub);
                    }
                }

                DiscoverSub newWalk = nextWalkSet.getOrDefault(nxNode, new DiscoverSub());
                int cardinality = newWalk.discovers.cardinality();
                if (oldWalk != null && (cardinality <= oldWalk.discovers.cardinality() || cardinality == 0)) {
                    nextWalkSet.put(node, oldWalk);
                }
            }

            walkSet = nextWalkSet;

            idx.incrementAndGet();
        }

        DiscoverSub best = new DiscoverSub();
        for (DiscoverSub s : walkSet.values()) {
            if (s.discovers.cardinality() > best.discovers.cardinality()) {
                best = s;
            }
        }

        System.out.println(best.substring);

        Map<Integer, Integer> disc = new HashMap<>();
        Node node = tree.root;
        idx.set(0);
        tree.setMatchesConsumer( e -> e.forEach(i->disc.putIfAbsent(i, idx.get() - words.get(i).length() + 2 )));

        for (char ch : best.substring.toCharArray()) {
            node = tree.feed(node, ch);
            idx.incrementAndGet();
        }

        for (int i = 0; i < words.size(); i++) {
            System.out.println(disc.containsKey(i) ? disc.get(i) : -1);
        }
     }
}

