package me.fmalyshev.ml;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

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
        Consumer<Iterable<Integer>> consumer;
        private Node state;

        public MutlimatchTree() {
            root = new Node();
            state = root;
        }

        public Node build(Node state, char symbol) {
            int index = Exons.valueOf(String.valueOf(symbol)).ordinal();
            Node next = state.next[index];
            if (next == null) {
                next = new Node();
                next.parent = state;
                next.ch = symbol;
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
            state = go(state, symbol);
            if (state.link == null)
                getLink(state);

            if (consumer != null && state.terminalTokens != null) {
                consumer.accept(state.terminalTokens);
            }

            return state;
        }

        public void setMatchesConsumer(Consumer<Iterable<Integer>> consumer) {
            this.consumer = consumer;
        }
    }

    private static class MutlimatchTreeBuilder {
        MutlimatchTree tree = new MutlimatchTree();
        int i = 0;

        public MutlimatchTreeBuilder feed(String token) {
            Node cur = tree.root;
            for(char c : token.toCharArray()) {
                cur = tree.build(cur, c);
            }
            if (cur.terminalTokens == null) {
                cur.terminalTokens = new ArrayList<>();
            }
            cur.terminalTokens.add(i++);
            return this;
        }

        public MutlimatchTreeBuilder withMatchesConsumer(Consumer<Iterable<Integer>> consumer) {
            tree.setMatchesConsumer(consumer);
            return this;
        }

        MutlimatchTree build() {
            return tree;
        }
    }

    private static Node root = new Node();
    private static ArrayList<Node> nodes = new ArrayList<>();

    static {
        nodes.add(root);
    }

    public static void main(String [] args) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        MutlimatchTreeBuilder builder = new MutlimatchTreeBuilder();
        String text = reader.readLine();
        int n = Integer.valueOf(reader.readLine());
        for (int i = 0; i < n; i++) {
            String word = reader.readLine();
            builder.feed(word);
        }

        final AtomicLong idx = new AtomicLong(0);
        builder.withMatchesConsumer( matches -> {
           for (int nmtch : matches) {
               System.out.println("#"+nmtch+ " ends at "+idx.get());
           }
        });

        MutlimatchTree tree = builder.build();

        for (char ch : text.toCharArray()) {
            tree.feed(ch);
            idx.incrementAndGet();
        }
    }
}
