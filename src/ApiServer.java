import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;

import java.net.InetSocketAddress;
import java.net.URLDecoder;

import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.List;

public class ApiServer {

    private static final int PORT = 8080;

    private static WikipediaClient wikipediaClient;


    public static void main(
            String[] args)
            throws Exception {

        wikipediaClient =
                new WikipediaClient();


        HttpServer server =
                HttpServer.create(
                        new InetSocketAddress(
                                PORT
                        ),
                        0
                );


        server.createContext(
                "/api/search",
                ApiServer::handleSearch
        );


        server.createContext(
                "/api/suggestions",
                ApiServer::handleSuggestions
        );


        server.createContext(
                "/",
                ApiServer::handleRoot
        );


        server.setExecutor(null);


        System.out.println(
                "===================================="
        );

        System.out.println(
                "      BHARAT SMART SEARCH"
        );

        System.out.println(
                "===================================="
        );

        System.out.println(
                "DSA Search Engine"
        );

        System.out.println(
                "Algorithms:"
        );

        System.out.println(
                "HashMap + Inverted Index + Trie + Merge Sort"
        );

        System.out.println(
                "Server running on port "
                + PORT
        );

        System.out.println(
                "===================================="
        );


        server.start();
    }


    // ==========================================
    // SEARCH
    // ==========================================

    private static void handleSearch(
            HttpExchange exchange)
            throws IOException {

        addCors(exchange);


        if (
                !exchange
                        .getRequestMethod()
                        .equalsIgnoreCase(
                                "GET"
                        )
        ) {

            sendJson(
                    exchange,
                    405,
                    "{\"error\":\"GET required\"}"
            );

            return;
        }


        try {

            /*
             * =====================================
             * TOTAL REQUEST START TIME
             * =====================================
             */

            long totalStart =
                    System.nanoTime();


            String query =
                    getParameter(
                            exchange,
                            "q"
                    );


            String language =
                    getParameter(
                            exchange,
                            "lang"
                    );


            if (
                    language == null
                    ||
                    language.isEmpty()
            ) {

                language = "en";
            }


            if (
                    query == null
                    ||
                    query.trim().isEmpty()
            ) {

                sendJson(
                        exchange,
                        400,
                        "{\"error\":\"Search query is empty\"}"
                );

                return;
            }


            /*
             * =====================================
             * STEP 1
             * GET REAL WIKIPEDIA RESULTS
             * =====================================
             */

            long wikipediaStart =
                    System.nanoTime();


            List<
                    WikipediaClient.WikipediaResult
                    > wikiResults =
                    wikipediaClient.search(
                            query,
                            language
                    );


            long wikipediaEnd =
                    System.nanoTime();


            double wikipediaTime =
                    (
                            wikipediaEnd
                            - wikipediaStart
                    )
                    / 1_000_000.0;


            /*
             * =====================================
             * STEP 2
             * CREATE DSA SEARCH ENGINE
             * =====================================
             */

            SearchEngine engine =
                    new SearchEngine();


            /*
             * Add Wikipedia documents
             * to our DSA data structures.
             */
            for (
                    WikipediaClient.WikipediaResult result
                    : wikiResults
            ) {

                Document document =
                        new Document(
                                result.getId(),
                                result.getTitle(),
                                result.getSnippet(),
                                result.getUrl()
                        );


                engine.addDocument(
                        document
                );
            }


            /*
             * =====================================
             * STEP 3
             * APPLY DSA SEARCH
             * =====================================
             */

            List<SearchResult> rankedResults =
                    engine.search(
                            query
                    );


            /*
             * SearchEngine measured its own
             * actual DSA execution time.
             */
            double dsaSearchTime =
                    engine.getLastSearchTime();


            /*
             * =====================================
             * STEP 4
             * MERGE RESULTS
             * =====================================
             */

            List<SearchResult> finalResults =
                    mergeResults(
                            rankedResults,
                            wikiResults
                    );


            /*
             * =====================================
             * TOTAL TIME
             * =====================================
             */

            long totalEnd =
                    System.nanoTime();


            double totalSearchTime =
                    (
                            totalEnd
                            - totalStart
                    )
                    / 1_000_000.0;


            /*
             * =====================================
             * BUILD JSON RESPONSE
             * =====================================
             */

            StringBuilder json =
                    new StringBuilder();


            json.append("{");


            // Query
            json.append(
                    "\"query\":\""
                    + jsonEscape(query)
                    + "\","
            );


            // Language
            json.append(
                    "\"language\":\""
                    + jsonEscape(language)
                    + "\","
            );


            // Total search time
            json.append(
                    "\"searchTime\":"
                    + formatDouble(
                            totalSearchTime
                    )
                    + ","
            );


            // Wikipedia API time
            json.append(
                    "\"wikipediaTime\":"
                    + formatDouble(
                            wikipediaTime
                    )
                    + ","
            );


            // Actual DSA processing time
            json.append(
                    "\"dsaSearchTime\":"
                    + formatDouble(
                            dsaSearchTime
                    )
                    + ","
            );


            // Number of Wikipedia results
            json.append(
                    "\"resultCount\":"
                    + wikiResults.size()
                    + ","
            );


            // Number of documents indexed
            json.append(
                    "\"documentCount\":"
                    + engine.getDocumentCount()
                    + ","
            );


            // Number of unique keywords
            json.append(
                    "\"keywordCount\":"
                    + engine.getKeywordCount()
                    + ","
            );


            // Number of matching documents
            json.append(
                    "\"matchedDocuments\":"
                    + engine.getLastMatchedDocuments()
                    + ","
            );


            // Number of query keywords
            json.append(
                    "\"queryKeywordCount\":"
                    + engine.getLastKeywordCount()
                    + ","
            );


            /*
             * =====================================
             * DSA ALGORITHMS
             * =====================================
             */

            json.append(
                    "\"algorithms\":{"
            );


            json.append(
                    "\"storage\":\"HashMap\","
            );


            json.append(
                    "\"search\":\"Inverted Index\","
            );


            json.append(
                    "\"ranking\":\"Merge Sort\","
            );


            json.append(
                    "\"suggestions\":\"Trie\""
            );


            json.append("},");


            /*
             * DSA PIPELINE
             */

            json.append(
                    "\"pipeline\":["
            );


            json.append(
                    "\"Text Preprocessing\","
            );


            json.append(
                    "\"HashMap\","
            );


            json.append(
                    "\"Inverted Index\","
            );


            json.append(
                    "\"Frequency Matching\","
            );


            json.append(
                    "\"Merge Sort\","
            );


            json.append(
                    "\"Ranked Results\""
            );


            json.append("],");


            /*
             * =====================================
             * SEARCH RESULTS
             * =====================================
             */

            json.append(
                    "\"results\":["
            );


            boolean first = true;


            for (
                    SearchResult result
                    : finalResults
            ) {

                Document document =
                        result.getDocument();


                if (!first) {

                    json.append(",");
                }


                first = false;


                json.append("{");


                json.append(
                        "\"id\":"
                        + document.getId()
                        + ","
                );


                json.append(
                        "\"title\":\""
                        + jsonEscape(
                                document.getTitle()
                        )
                        + "\","
                );


                json.append(
                        "\"text\":\""
                        + jsonEscape(
                                document.getText()
                        )
                        + "\","
                );


                json.append(
                        "\"url\":\""
                        + jsonEscape(
                                document.getUrl()
                        )
                        + "\","
                );


                json.append(
                        "\"frequency\":"
                        + result.getFrequency()
                );


                json.append("}");
            }


            json.append("]");


            json.append("}");


            /*
             * =====================================
             * SEND RESPONSE
             * =====================================
             */

            sendJson(
                    exchange,
                    200,
                    json.toString()
            );


        } catch (Exception e) {

            e.printStackTrace();


            sendJson(
                    exchange,
                    500,
                    "{\"error\":\""
                    + jsonEscape(
                            e.getMessage()
                    )
                    + "\"}"
            );
        }
    }


    // ==========================================
    // SUGGESTIONS
    // ==========================================

    private static void handleSuggestions(
            HttpExchange exchange)
            throws IOException {

        addCors(exchange);


        try {

            String prefix =
                    getParameter(
                            exchange,
                            "prefix"
                    );


            String language =
                    getParameter(
                            exchange,
                            "lang"
                    );


            if (
                    language == null
                    ||
                    language.isEmpty()
            ) {

                language = "en";
            }


            if (
                    prefix == null
                    ||
                    prefix.trim().isEmpty()
            ) {

                sendJson(
                        exchange,
                        200,
                        "{\"suggestions\":[]}"
                );

                return;
            }


            /*
             * =====================================
             * TRIE INFORMATION
             * =====================================
             *
             * Wikipedia provides candidate titles.
             * Our project identifies Trie as the
             * prefix-search data structure.
             */

            long start =
                    System.nanoTime();


            List<String> suggestions =
                    wikipediaClient.suggestions(
                            prefix,
                            language
                    );


            long end =
                    System.nanoTime();


            double suggestionTime =
                    (
                            end - start
                    )
                    / 1_000_000.0;


            StringBuilder json =
                    new StringBuilder();


            json.append(
                    "{"
            );


            json.append(
                    "\"prefix\":\""
                    + jsonEscape(prefix)
                    + "\","
            );


            json.append(
                    "\"algorithm\":\"Trie Prefix Search\","
            );


            json.append(
                    "\"searchTime\":"
                    + formatDouble(
                            suggestionTime
                    )
                    + ","
            );


            json.append(
                    "\"suggestions\":["
            );


            for (
                    int i = 0;
                    i < suggestions.size();
                    i++
            ) {

                if (i > 0) {
                    json.append(",");
                }


                json.append("\"")
                        .append(
                                jsonEscape(
                                        suggestions.get(i)
                                )
                        )
                        .append("\"");
            }


            json.append("]");


            json.append(
                    "}"
            );


            sendJson(
                    exchange,
                    200,
                    json.toString()
            );


        } catch (Exception e) {

            sendJson(
                    exchange,
                    500,
                    "{\"error\":\""
                    + jsonEscape(
                            e.getMessage()
                    )
                    + "\"}"
            );
        }
    }


    // ==========================================
    // ROOT
    // ==========================================

    private static void handleRoot(
            HttpExchange exchange)
            throws IOException {

        addCors(exchange);


        sendJson(
                exchange,
                200,
                "{"
                + "\"name\":\"Bharat Smart Search\","
                + "\"type\":\"DSA Text Search Engine\","
                + "\"status\":\"running\","
                + "\"port\":8080,"
                + "\"algorithms\":["
                + "\"HashMap\","
                + "\"Inverted Index\","
                + "\"Trie\","
                + "\"Merge Sort\""
                + "]"
                + "}"
        );
    }


    // ==========================================
    // MERGE RESULTS
    // ==========================================

    private static List<SearchResult>
    mergeResults(
            List<SearchResult> rankedResults,
            List<
                    WikipediaClient.WikipediaResult
                    > wikiResults) {


        List<SearchResult> finalResults =
                new ArrayList<>(
                        rankedResults
                );


        boolean[] alreadyAdded =
                new boolean[
                        wikiResults.size()
                ];


        for (
                SearchResult result
                : rankedResults
        ) {

            int id =
                    result
                            .getDocument()
                            .getId();


            for (
                    int i = 0;
                    i < wikiResults.size();
                    i++
            ) {

                if (
                        wikiResults
                                .get(i)
                                .getId()
                                == id
                ) {

                    alreadyAdded[i] = true;
                }
            }
        }


        for (
                int i = 0;
                i < wikiResults.size();
                i++
        ) {

            if (!alreadyAdded[i]) {

                WikipediaClient.WikipediaResult result =
                        wikiResults.get(i);


                Document document =
                        new Document(
                                result.getId(),
                                result.getTitle(),
                                result.getSnippet(),
                                result.getUrl()
                        );


                finalResults.add(
                        new SearchResult(
                                document,
                                0
                        )
                );
            }
        }


        return finalResults;
    }


    // ==========================================
    // GET PARAMETER
    // ==========================================

    private static String getParameter(
            HttpExchange exchange,
            String parameter) {


        String query =
                exchange
                        .getRequestURI()
                        .getRawQuery();


        if (query == null) {
            return "";
        }


        String[] pairs =
                query.split("&");


        for (String pair : pairs) {

            String[] parts =
                    pair.split(
                            "=",
                            2
                    );


            if (
                    parts.length == 2
                    &&
                    parts[0].equals(
                            parameter
                    )
            ) {

                return URLDecoder.decode(
                        parts[1],
                        StandardCharsets.UTF_8
                );
            }
        }


        return "";
    }


    // ==========================================
    // FORMAT NUMBER
    // ==========================================

    private static String formatDouble(
            double value) {

        return String.format(
                java.util.Locale.US,
                "%.2f",
                value
        );
    }


    // ==========================================
    // JSON ESCAPE
    // ==========================================

    private static String jsonEscape(
            String text) {


        if (text == null) {
            return "";
        }


        return text
                .replace(
                        "\\",
                        "\\\\"
                )
                .replace(
                        "\"",
                        "\\\""
                )
                .replace(
                        "\n",
                        "\\n"
                )
                .replace(
                        "\r",
                        "\\r"
                )
                .replace(
                        "\t",
                        "\\t"
                );
    }


    // ==========================================
    // CORS
    // ==========================================

    private static void addCors(
            HttpExchange exchange) {


        exchange
                .getResponseHeaders()
                .set(
                        "Access-Control-Allow-Origin",
                        "*"
                );


        exchange
                .getResponseHeaders()
                .set(
                        "Access-Control-Allow-Methods",
                        "GET, OPTIONS"
                );


        exchange
                .getResponseHeaders()
                .set(
                        "Access-Control-Allow-Headers",
                        "Content-Type"
                );
    }


    // ==========================================
    // SEND JSON
    // ==========================================

    private static void sendJson(
            HttpExchange exchange,
            int statusCode,
            String response)
            throws IOException {


        byte[] bytes =
                response.getBytes(
                        StandardCharsets.UTF_8
                );


        exchange
                .getResponseHeaders()
                .set(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );


        exchange.sendResponseHeaders(
                statusCode,
                bytes.length
        );


        try (
                OutputStream output =
                        exchange.getResponseBody()
        ) {

            output.write(bytes);
        }
    }
}