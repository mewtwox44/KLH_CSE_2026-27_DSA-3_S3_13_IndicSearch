public class Document {

    private int id;
    private String title;
    private String text;
    private String url;


    public Document(
            int id,
            String title,
            String text) {

        this(
                id,
                title,
                text,
                ""
        );
    }


    public Document(
            int id,
            String title,
            String text,
            String url) {

        this.id = id;

        this.title = title;

        this.text = text;

        this.url = url;
    }


    public int getId() {

        return id;
    }


    public String getTitle() {

        return title;
    }


    public String getText() {

        return text;
    }


    public String getUrl() {

        return url;
    }
}