import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.List;

public class WikipediaClient {

    private final HttpClient client;

    /*
     * IMPORTANT:
     * Use a meaningful User-Agent.
     */
    private static final String USER_AGENT =
            "BharatSmartSearch/1.0 (KLH University student project)";

    /*
     * Number of times we retry after HTTP 429.
     */
    private static final int MAX_RETRIES = 3;


    public WikipediaClient() {

        client =
                HttpClient.newBuilder()
                        .followRedirects(
                                HttpClient.Redirect.NORMAL
                        )
                        .build();
    }


    // ==========================================
    // SEARCH WIKIPEDIA
    // ==========================================

    public List<WikipediaResult> search(
            String query,
            String language)
            throws Exception {

        List<WikipediaResult> results =
                new ArrayList<>();

        if (query == null ||
                query.trim().isEmpty()) {

            return results;
        }

        if (language == null ||
                language.trim().isEmpty()) {

            language = "en";
        }

        /*
         * First search using the exact query.
         */
        results =
                performSearch(
                        query,
                        language
                );

        /*
         * If the query starts with a title such as
         * Dr., Prof., Mr., Mrs., or Ms.,
         * perform another search without the title.
         *
         * Example:
         * "Dr. Sambaiah Mydam"
         *
         * becomes:
         * "Sambaiah Mydam"
         */
        String simplifiedQuery =
                removeTitlePrefix(query);

        if (!simplifiedQuery.equals(query)
                &&
                results.size() < 10) {

            List<WikipediaResult> extraResults =
                    performSearch(
                            simplifiedQuery,
                            language
                    );

            /*
             * Add only results that are not already present.
             */
            for (
                    WikipediaResult extra
                    : extraResults
            ) {

                boolean alreadyExists = false;

                for (
                        WikipediaResult existing
                        : results
                ) {

                    if (
                            existing.getTitle()
                                    .equalsIgnoreCase(
                                            extra.getTitle()
                                    )
                    ) {

                        alreadyExists = true;
                        break;
                    }
                }

                if (!alreadyExists) {

                    results.add(extra);
                }

                if (results.size() >= 10) {
                    break;
                }
            }
        }

        /*
         * Re-number IDs.
         */
        List<WikipediaResult> finalResults =
                new ArrayList<>();

        int id = 1;

        for (
                WikipediaResult result
                : results
        ) {

            finalResults.add(
                    new WikipediaResult(
                            id++,
                            result.getTitle(),
                            result.getSnippet(),
                            result.getUrl(),
                            result.getWordCount()
                    )
            );
        }

        return finalResults;
    }


    // ==========================================
    // ACTUAL WIKIPEDIA SEARCH REQUEST
    // ==========================================

    private List<WikipediaResult>
    performSearch(
            String query,
            String language)
            throws Exception {

        List<WikipediaResult> results =
                new ArrayList<>();

        String encodedQuery =
                URLEncoder.encode(
                        query,
                        StandardCharsets.UTF_8
                );

        String apiUrl =
                "https://"
                + language
                + ".wikipedia.org/w/api.php"
                + "?action=query"
                + "&list=search"
                + "&srsearch="
                + encodedQuery
                + "&srlimit=10"
                + "&srprop=snippet%7Cwordcount"
                + "&format=json"
                + "&utf8=1";


        int attempt = 0;

        while (attempt < MAX_RETRIES) {

            attempt++;

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(apiUrl)
                            )
                            .header(
                                    "User-Agent",
                                    USER_AGENT
                            )
                            .header(
                                    "Accept",
                                    "application/json"
                            )
                            .GET()
                            .build();


            HttpResponse<String> response =
                    client.send(
                            request,
                            HttpResponse.BodyHandlers
                                    .ofString(
                                            StandardCharsets.UTF_8
                                    )
                    );


            int status =
                    response.statusCode();


            // ==================================
            // SUCCESS
            // ==================================

            if (status == 200) {

                return parseSearchResults(
                        response.body(),
                        language
                );
            }


            // ==================================
            // RATE LIMIT - 429
            // ==================================

            if (status == 429) {

                System.out.println(
                        "Wikipedia API rate limit reached (429)."
                );

                /*
                 * Try to read Retry-After.
                 */
                String retryAfter =
                        response.headers()
                                .firstValue(
                                        "Retry-After"
                                )
                                .orElse("5");

                int waitSeconds = 5;

                try {

                    waitSeconds =
                            Integer.parseInt(
                                    retryAfter
                            );

                } catch (Exception e) {

                    waitSeconds = 5;
                }


                /*
                 * Prevent an extremely large wait.
                 */
                if (waitSeconds > 30) {
                    waitSeconds = 30;
                }

                System.out.println(
                        "Waiting "
                        + waitSeconds
                        + " seconds before retry..."
                );


                Thread.sleep(
                        waitSeconds * 1000L
                );

                continue;
            }


            // ==================================
            // OTHER HTTP ERROR
            // ==================================

            throw new RuntimeException(
                    "Wikipedia API error: "
                    + status
            );
        }


        /*
         * All retries failed.
         */
        throw new RuntimeException(
                "Wikipedia API is temporarily rate-limited. "
                + "Please wait a little and try again."
        );
    }


    // ==========================================
    // REMOVE TITLE PREFIX
    // ==========================================

    private String removeTitlePrefix(
            String query) {

        String trimmed =
                query.trim();

        String lower =
                trimmed.toLowerCase();


        String[] prefixes = {
                "dr. ",
                "dr ",
                "prof. ",
                "prof ",
                "mr. ",
                "mr ",
                "mrs. ",
                "mrs ",
                "ms. ",
                "ms "
        };


        for (String prefix : prefixes) {

            if (lower.startsWith(prefix)) {

                return trimmed.substring(
                        prefix.length()
                ).trim();
            }
        }


        return trimmed;
    }


    // ==========================================
    // LIVE SUGGESTIONS
    // ==========================================

    public List<String> suggestions(
            String prefix,
            String language)
            throws Exception {

        List<String> suggestions =
                new ArrayList<>();


        if (prefix == null ||
                prefix.trim().isEmpty()) {

            return suggestions;
        }


        if (language == null ||
                language.trim().isEmpty()) {

            language = "en";
        }


        String encodedPrefix =
                URLEncoder.encode(
                        prefix,
                        StandardCharsets.UTF_8
                );


        String apiUrl =
                "https://"
                + language
                + ".wikipedia.org/w/api.php"
                + "?action=query"
                + "&list=prefixsearch"
                + "&pssearch="
                + encodedPrefix
                + "&pslimit=6"
                + "&format=json"
                + "&utf8=1";


        int attempt = 0;


        while (attempt < MAX_RETRIES) {

            attempt++;


            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(apiUrl)
                            )
                            .header(
                                    "User-Agent",
                                    USER_AGENT
                            )
                            .header(
                                    "Accept",
                                    "application/json"
                            )
                            .GET()
                            .build();


            HttpResponse<String> response =
                    client.send(
                            request,
                            HttpResponse.BodyHandlers
                                    .ofString(
                                            StandardCharsets.UTF_8
                                    )
                    );


            int status =
                    response.statusCode();


            if (status == 200) {

                return parseSuggestions(
                        response.body()
                );
            }


            if (status == 429) {

                System.out.println(
                        "Wikipedia suggestions rate limit reached (429)."
                );


                String retryAfter =
                        response.headers()
                                .firstValue(
                                        "Retry-After"
                                )
                                .orElse("5");


                int waitSeconds = 5;


                try {

                    waitSeconds =
                            Integer.parseInt(
                                    retryAfter
                            );

                } catch (Exception e) {

                    waitSeconds = 5;
                }


                if (waitSeconds > 30) {
                    waitSeconds = 30;
                }


                System.out.println(
                        "Waiting "
                        + waitSeconds
                        + " seconds..."
                );


                Thread.sleep(
                        waitSeconds * 1000L
                );


                continue;
            }


            /*
             * For suggestion failures, simply
             * return an empty list.
             */
            return suggestions;
        }


        return suggestions;
    }


    // ==========================================
    // PARSE SUGGESTIONS
    // ==========================================

    private List<String> parseSuggestions(
            String json) {

        List<String> suggestions =
                new ArrayList<>();


        int position = 0;


        while (true) {

            int titlePosition =
                    json.indexOf(
                            "\"title\"",
                            position
                    );


            if (titlePosition == -1) {
                break;
            }


            String title =
                    extractJsonString(
                            json,
                            titlePosition
                    );


            if (!title.isEmpty()
                    &&
                    !suggestions.contains(
                            title
                    )) {

                suggestions.add(title);
            }


            position =
                    titlePosition + 7;


            if (suggestions.size() >= 6) {
                break;
            }
        }


        return suggestions;
    }


    // ==========================================
    // PARSE SEARCH RESULTS
    // ==========================================

    private List<WikipediaResult>
    parseSearchResults(
            String json,
            String language) {

        List<WikipediaResult> results =
                new ArrayList<>();


        int position = 0;

        int id = 1;


        while (true) {

            int titlePosition =
                    json.indexOf(
                            "\"title\"",
                            position
                    );


            if (titlePosition == -1) {
                break;
            }


            String title =
                    extractJsonString(
                            json,
                            titlePosition
                    );


            int snippetPosition =
                    json.indexOf(
                            "\"snippet\"",
                            titlePosition
                    );


            if (snippetPosition == -1) {
                break;
            }


            String snippet =
                    extractJsonString(
                            json,
                            snippetPosition
                    );


            int wordCountPosition =
                    json.indexOf(
                            "\"wordcount\"",
                            snippetPosition
                    );


            int wordCount = 0;


            if (wordCountPosition != -1) {

                wordCount =
                        extractJsonInt(
                                json,
                                wordCountPosition
                        );
            }


            String pageUrl =
                    "https://"
                    + language
                    + ".wikipedia.org/wiki/"
                    + URLEncoder.encode(
                            title.replace(
                                    " ",
                                    "_"
                            ),
                            StandardCharsets.UTF_8
                    );


            snippet =
                    cleanHtml(
                            snippet
                    );


            results.add(
                    new WikipediaResult(
                            id++,
                            title,
                            snippet,
                            pageUrl,
                            wordCount
                    )
            );


            position =
                    snippetPosition + 9;


            if (results.size() >= 10) {
                break;
            }
        }


        return results;
    }


    // ==========================================
    // JSON STRING
    // ==========================================

    private String extractJsonString(
            String json,
            int keyPosition) {

        int colon =
                json.indexOf(
                        ':',
                        keyPosition
                );


        if (colon == -1) {
            return "";
        }


        int firstQuote =
                json.indexOf(
                        '"',
                        colon + 1
                );


        if (firstQuote == -1) {
            return "";
        }


        StringBuilder value =
                new StringBuilder();


        boolean escaped = false;


        for (
                int i = firstQuote + 1;
                i < json.length();
                i++
        ) {

            char ch =
                    json.charAt(i);


            if (escaped) {

                switch (ch) {

                    case '"':
                        value.append('"');
                        break;

                    case '\\':
                        value.append('\\');
                        break;

                    case '/':
                        value.append('/');
                        break;

                    case 'n':
                        value.append('\n');
                        break;

                    case 'r':
                        value.append('\r');
                        break;

                    case 't':
                        value.append('\t');
                        break;

                    case 'b':
                        value.append('\b');
                        break;

                    case 'f':
                        value.append('\f');
                        break;

                    case 'u':

                        if (
                                i + 4
                                <
                                json.length()
                        ) {

                            String hex =
                                    json.substring(
                                            i + 1,
                                            i + 5
                                    );


                            try {

                                value.append(
                                        (char)
                                        Integer.parseInt(
                                                hex,
                                                16
                                        )
                                );

                                i += 4;

                            } catch (
                                    Exception e
                            ) {

                                value.append(
                                        'u'
                                );
                            }
                        }

                        break;

                    default:
                        value.append(ch);
                }


                escaped = false;

            } else if (
                    ch == '\\'
            ) {

                escaped = true;

            } else if (
                    ch == '"'
            ) {

                break;

            } else {

                value.append(ch);
            }
        }


        return value.toString();
    }


    // ==========================================
    // JSON INTEGER
    // ==========================================

    private int extractJsonInt(
            String json,
            int keyPosition) {

        int colon =
                json.indexOf(
                        ':',
                        keyPosition
                );


        if (colon == -1) {
            return 0;
        }


        int start =
                colon + 1;


        while (
                start < json.length()
                &&
                Character.isWhitespace(
                        json.charAt(start)
                )
        ) {

            start++;
        }


        int end = start;


        while (
                end < json.length()
                &&
                Character.isDigit(
                        json.charAt(end)
                )
        ) {

            end++;
        }


        try {

            return Integer.parseInt(
                    json.substring(
                            start,
                            end
                    )
            );

        } catch (Exception e) {

            return 0;
        }
    }


    // ==========================================
    // REMOVE HTML
    // ==========================================

    private String cleanHtml(
            String text) {

        return text
                .replaceAll(
                        "<[^>]*>",
                        ""
                )
                .replace(
                        "&quot;",
                        "\""
                )
                .replace(
                        "&amp;",
                        "&"
                )
                .replace(
                        "&#39;",
                        "'"
                )
                .replace(
                        "&lt;",
                        "<"
                )
                .replace(
                        "&gt;",
                        ">"
                );
    }


    // ==========================================
    // RESULT CLASS
    // ==========================================

    public static class WikipediaResult {

        private int id;

        private String title;

        private String snippet;

        private String url;

        private int wordCount;


        public WikipediaResult(
                int id,
                String title,
                String snippet,
                String url,
                int wordCount) {

            this.id = id;

            this.title = title;

            this.snippet = snippet;

            this.url = url;

            this.wordCount = wordCount;
        }


        public int getId() {
            return id;
        }


        public String getTitle() {
            return title;
        }


        public String getSnippet() {
            return snippet;
        }


        public String getUrl() {
            return url;
        }


        public int getWordCount() {
            return wordCount;
        }
    }
}