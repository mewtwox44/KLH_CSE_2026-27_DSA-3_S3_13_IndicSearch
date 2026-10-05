import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchEngine {

    private HashMap<Integer, Document> documents;

    private InvertedIndex invertedIndex;

    private Trie trie;

    private double lastSearchTime;
    private int lastMatchedDocuments;
    private int lastKeywordCount;
    private String lastAlgorithm;

    public SearchEngine() {

        documents = new HashMap<>();

        invertedIndex = new InvertedIndex();

        trie = new Trie();

        lastSearchTime = 0.0;
        lastMatchedDocuments = 0;
        lastKeywordCount = 0;

        lastAlgorithm =
                "HashMap + Inverted Index + Merge Sort";
    }


    // ==========================================
    // ADD DOCUMENT
    // ==========================================

    public void addDocument(
            Document document) {

        documents.put(
                document.getId(),
                document
        );


        /*
         * IMPORTANT:
         *
         * Index BOTH the title and the text.
         *
         * Earlier we indexed only the snippet.
         * That caused searches for names such as
         * "Dr. Sambaiah Mydam" to fail when the
         * words were present in the title but
         * not completely present in the snippet.
         */

        String searchableText =
                document.getTitle()
                + " "
                + document.getText();


        Document searchableDocument =
                new Document(
                        document.getId(),
                        document.getTitle(),
                        searchableText,
                        document.getUrl()
                );


        invertedIndex.addDocument(
                searchableDocument
        );


        /*
         * Add title + text words to Trie.
         */
        List<String> words =
                TextPreprocessor.tokenize(
                        searchableText
                );


        for (String word : words) {

            trie.insert(word);
        }
    }


    // ==========================================
    // SEARCH
    // ==========================================

    public List<SearchResult> search(
            String query) {

        long startTime =
                System.nanoTime();


        String normalizedQuery =
                TextPreprocessor.normalize(
                        query
                );


        if (normalizedQuery.isEmpty()) {

            lastSearchTime = 0.0;
            lastMatchedDocuments = 0;
            lastKeywordCount = 0;

            return new ArrayList<>();
        }


        String[] keywords =
                normalizedQuery.split(
                        "\\s+"
                );


        lastKeywordCount =
                keywords.length;


        /*
         * Store document scores.
         */
        HashMap<Integer, Integer> scores =
                new HashMap<>();


        /*
         * Search every keyword in
         * the Inverted Index.
         */
        for (String keyword : keywords) {

            HashMap<Integer, Integer> matches =
                    invertedIndex.searchWord(
                            keyword
                    );


            for (
                    Map.Entry<Integer, Integer> entry
                    : matches.entrySet()
            ) {

                int documentId =
                        entry.getKey();


                int frequency =
                        entry.getValue();


                scores.put(
                        documentId,
                        scores.getOrDefault(
                                documentId,
                                0
                        ) + frequency
                );
            }
        }


        /*
         * Convert scores to SearchResult.
         */
        List<SearchResult> results =
                new ArrayList<>();


        for (
                Map.Entry<Integer, Integer> entry
                : scores.entrySet()
        ) {

            Document document =
                    documents.get(
                            entry.getKey()
                    );


            if (document != null) {

                results.add(
                        new SearchResult(
                                document,
                                entry.getValue()
                        )
                );
            }
        }


        /*
         * Rank using Merge Sort.
         */
        MergeSort.sort(
                results
        );


        lastMatchedDocuments =
                results.size();


        long endTime =
                System.nanoTime();


        lastSearchTime =
                (
                        endTime - startTime
                )
                / 1_000_000.0;


        lastAlgorithm =
                "HashMap + Inverted Index + Merge Sort";


        return results;
    }


    // ==========================================
    // SUGGESTIONS
    // ==========================================

    public List<String> suggestions(
            String prefix) {

        prefix =
                TextPreprocessor.normalize(
                        prefix
                );


        if (prefix.isEmpty()) {

            return new ArrayList<>();
        }


        return trie.getSuggestions(
                prefix
        );
    }


    // ==========================================
    // INFORMATION
    // ==========================================

    public int getDocumentCount() {

        return documents.size();
    }


    public int getKeywordCount() {

        return invertedIndex
                .getAllWords()
                .size();
    }


    public double getLastSearchTime() {

        return lastSearchTime;
    }


    public int getLastMatchedDocuments() {

        return lastMatchedDocuments;
    }


    public int getLastKeywordCount() {

        return lastKeywordCount;
    }


    public String getLastAlgorithm() {

        return lastAlgorithm;
    }


    public String getSuggestionAlgorithm() {

        return "Trie Prefix Search";
    }


    public String getRankingAlgorithm() {

        return "Merge Sort";
    }


    public String getIndexAlgorithm() {

        return "HashMap + Inverted Index";
    }
}