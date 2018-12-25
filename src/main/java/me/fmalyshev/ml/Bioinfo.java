package me.fmalyshev.ml;

import java.util.*;
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
                node.link = (node == root || node.parent == null) ? root : go(getLink(node.parent), node.ch);
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
                    if (consumer != null && next.terminalTokens != null) {
                        consumer.accept(next.terminalTokens);
                    }
                }
            }
            state = next;
            return state;
        }

        public Node feed(char symbol) {
            state = go(state, symbol);

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
            cur.terminalTokens.add(i++);
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

    public static void main(String [] args) {

    }
}
