import java.util.HashMap;
import java.util.Set;

public class InvertedIndex {

    private HashMap<
            String,
            HashMap<
                    Integer,
                    Integer
                    >
            > index;


    public InvertedIndex() {

        index =
                new HashMap<>();
    }


    public void addDocument(
            Document document) {


        var words =
                TextPreprocessor.tokenize(
                        document.getText()
                );


        for (String word : words) {


            if (
                    !index.containsKey(
                            word
                    )
            ) {

                index.put(
                        word,
                        new HashMap<>()
                );
            }


            HashMap<Integer, Integer>
                    documents =
                    index.get(
                            word
                    );


            int currentFrequency =
                    documents.getOrDefault(
                            document.getId(),
                            0
                    );


            documents.put(
                    document.getId(),
                    currentFrequency + 1
            );
        }
    }


    public HashMap<Integer, Integer>
    searchWord(
            String word) {


        word =
                TextPreprocessor.normalize(
                        word
                );


        return index.getOrDefault(
                word,
                new HashMap<>()
        );
    }


    public Set<String> getAllWords() {

        return index.keySet();
    }
}