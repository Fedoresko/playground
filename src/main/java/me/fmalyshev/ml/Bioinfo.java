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
        int index;
        char ch;
    }

    private static class MutlimatchTree {
        private final Node root;
        Consumer<Collection<Integer>> consumer;
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
                next.index = nNodes++;
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
        Set<Discover> discovers = new HashSet<>();
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
        Set<Discover> dmatches = new HashSet<>();

        builder.withMatchesConsumer( matches -> {
            for (int nmtch : matches) {
                dmatches.add(new Discover(nmtch, idx.get()));
           }
        });

        MutlimatchTree tree = builder.build();

        Map<Node, DiscoverSub> dirM = new HashMap<>();
        Map<Node, DiscoverSub> walkM = new HashMap<>();
        dirM.put(tree.root, new DiscoverSub());

        for (char ch : text.toCharArray()) {
            Map<Node, DiscoverSub> dirN = new HashMap<>();
            Map<Node, DiscoverSub> walkN = new HashMap<>();

            for (Node node : nodes) {
                dmatches.clear();
                Node nxNode = tree.feed(node, ch);

                DiscoverSub newSub = new DiscoverSub();
                DiscoverSub oldWalk = walkM.get(node);
                if (oldWalk != null) {
                    newSub.discovers.addAll(oldWalk.discovers);
                    newSub.discovers.addAll(dmatches.stream().map(d->new Discover(d.nToken, oldWalk.substring.length())).collect(Collectors.toCollection(ArrayList::new)));
                    newSub.substring = oldWalk.substring + ch;

                    DiscoverSub newWalk = walkN.getOrDefault(nxNode, new DiscoverSub());
                    if (newWalk.discovers.size()
                            <= newSub.discovers.size()) {
                        walkN.put(nxNode, newSub);
                    }
                }

                newSub = new DiscoverSub();
                DiscoverSub oldDir = dirM.get(node);
                if (oldDir != null) {
                    newSub.discovers.addAll(oldDir.discovers);
                    newSub.discovers.addAll(dmatches.stream().map(d->new Discover(d.nToken, oldDir.substring.length())).collect(Collectors.toCollection(ArrayList::new)));
                    newSub.substring = oldDir.substring + ch;

                    DiscoverSub newWalk = walkN.getOrDefault(nxNode, new DiscoverSub());
                    if (newWalk.discovers.size()
                            <= newSub.discovers.size()) {
                        walkN.put(nxNode, newSub);
                    }
                }

                DiscoverSub newDir = dirN.getOrDefault(nxNode, new DiscoverSub());

                if (oldDir != null && newDir.discovers.size() <= oldDir.discovers.size()) {
                    dirN.put(node, oldDir);
                    newDir = oldDir;
                }

                if (oldWalk != null && newDir.discovers.size() <= oldWalk.discovers.size()) {
                    dirN.put(node, oldWalk);
                }

            }

            dirM = dirN;
            walkM = walkN;

            final Map<Node, DiscoverSub> dirMt = dirM;
            final Map<Node, DiscoverSub> walkMt = walkM;

            idx.incrementAndGet();
        }

        DiscoverSub best = new DiscoverSub();
        for (DiscoverSub s : walkM.values()) {
            if (s.discovers.size() > best.discovers.size()) {
                best = s;
            }
        }
        for (DiscoverSub s : dirM.values()) {
            if (s.discovers.size() > best.discovers.size()) {
                best = s;
            }
        }

        System.out.println(best.substring);
        Map<Integer, Integer> disc = best.discovers.stream().collect(Collectors.toMap( e->e.nToken, e-> e.endIdx));
        for (int i = 0; i < words.size(); i++) {
            System.out.println(disc.containsKey(i) ? disc.get(i) - words.get(i).length() + 2 : -1);
        }
     }
}

