public class SearchResult {

    private Document document;

    private int frequency;


    public SearchResult(
            Document document,
            int frequency) {

        this.document =
                document;

        this.frequency =
                frequency;
    }


    public Document getDocument() {

        return document;
    }


    public int getFrequency() {

        return frequency;
    }
}