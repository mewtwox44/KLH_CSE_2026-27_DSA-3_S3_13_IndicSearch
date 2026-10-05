import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Trie {

    private static class TrieNode {

        HashMap<Character, TrieNode>
                children =
                new HashMap<>();

        boolean isEnd = false;
    }

    private TrieNode root;

    public Trie() {

        root = new TrieNode();
    }

    public void insert(String word) {

        TrieNode current = root;

        for (char ch :
                word.toCharArray()) {

            if (!current.children
                    .containsKey(ch)) {

                current.children.put(
                        ch,
                        new TrieNode()
                );
            }

            current =
                    current.children.get(ch);
        }

        current.isEnd = true;
    }

    public List<String> getSuggestions(
            String prefix) {

        List<String> results =
                new ArrayList<>();

        TrieNode current = root;

        for (char ch :
                prefix.toCharArray()) {

            if (!current.children
                    .containsKey(ch)) {

                return results;
            }

            current =
                    current.children.get(ch);
        }

        collectWords(
                current,
                prefix,
                results
        );

        return results;
    }

    private void collectWords(
            TrieNode node,
            String word,
            List<String> results) {

        if (results.size() >= 5) {
            return;
        }

        if (node.isEnd) {
            results.add(word);
        }

        for (
                Map.Entry<
                        Character,
                        TrieNode
                        > entry
                : node.children.entrySet()
        ) {

            collectWords(
                    entry.getValue(),
                    word + entry.getKey(),
                    results
            );
        }
    }
}