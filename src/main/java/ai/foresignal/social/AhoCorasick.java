package ai.foresignal.social;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An Aho–Corasick automaton over UTF-16 characters (prompt 135): every occurrence of every pattern in one pass over a
 * text, in time linear in the text plus the matches. Patterns and texts are expected to be folded
 * ({@link TextFolding#fold}) by the caller; this class compares characters exactly.
 *
 * @param <T> what a pattern carries
 */
public final class AhoCorasick<T> {

    /**
     * One occurrence.
     *
     * @param start the start index in the text
     * @param end the end index (exclusive)
     * @param pattern the pattern
     * @param value what the pattern carries
     * @param <T> the value type
     */
    public record Hit<T>(int start, int end, String pattern, T value) {}

    private static final class Node<T> {
        final Map<Character, Node<T>> next = new HashMap<>();
        Node<T> fail;
        final List<Integer> outputs = new ArrayList<>();
    }

    private final Node<T> root = new Node<>();
    private final List<String> patterns = new ArrayList<>();
    private final List<T> values = new ArrayList<>();

    /**
     * Builds the automaton.
     *
     * @param entries pattern to value; blank patterns are ignored
     */
    public AhoCorasick(Map<String, T> entries) {
        for (Map.Entry<String, T> e : entries.entrySet()) {
            String pattern = e.getKey();
            if (pattern == null || pattern.isEmpty()) {
                continue;
            }
            Node<T> node = root;
            for (int i = 0; i < pattern.length(); i++) {
                node = node.next.computeIfAbsent(pattern.charAt(i), c -> new Node<>());
            }
            node.outputs.add(patterns.size());
            patterns.add(pattern);
            values.add(e.getValue());
        }
        ArrayDeque<Node<T>> queue = new ArrayDeque<>();
        root.fail = root;
        for (Node<T> child : root.next.values()) {
            child.fail = root;
            queue.add(child);
        }
        while (!queue.isEmpty()) {
            Node<T> node = queue.poll();
            for (Map.Entry<Character, Node<T>> e : node.next.entrySet()) {
                char c = e.getKey();
                Node<T> child = e.getValue();
                Node<T> f = node.fail;
                while (f != root && !f.next.containsKey(c)) {
                    f = f.fail;
                }
                Node<T> target = f.next.get(c);
                child.fail = target != null && target != child ? target : root;
                child.outputs.addAll(child.fail.outputs);
                queue.add(child);
            }
        }
    }

    /** The number of patterns. */
    public int size() {
        return patterns.size();
    }

    /**
     * Every occurrence of every pattern in {@code text}, in order of their end index.
     *
     * @param text the (folded) text
     * @return the occurrences
     */
    public List<Hit<T>> search(String text) {
        List<Hit<T>> out = new ArrayList<>();
        if (text == null || text.isEmpty() || patterns.isEmpty()) {
            return out;
        }
        Node<T> node = root;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            while (node != root && !node.next.containsKey(c)) {
                node = node.fail;
            }
            Node<T> next = node.next.get(c);
            node = next == null ? root : next;
            for (int index : node.outputs) {
                String pattern = patterns.get(index);
                out.add(new Hit<>(i + 1 - pattern.length(), i + 1, pattern, values.get(index)));
            }
        }
        return out;
    }
}
