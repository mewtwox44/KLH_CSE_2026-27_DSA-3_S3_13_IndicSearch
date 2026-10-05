import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(
            String[] args) {

        SearchEngine engine =
                new SearchEngine();


        // =========================
        // SAMPLE WIKIPEDIA ARTICLES
        // =========================

        engine.addDocument(
                new Document(
                        1,
                        "भारत",
                        "भारत दक्षिण एशिया में स्थित "
                                + "एक देश है। भारत की राजधानी "
                                + "नई दिल्ली है। भारत एक विविध "
                                + "संस्कृति वाला देश है।"
                )
        );


        engine.addDocument(
                new Document(
                        2,
                        "भारतीय क्रिकेट",
                        "क्रिकेट भारत में बहुत लोकप्रिय "
                                + "खेल है। भारतीय क्रिकेट टीम ने "
                                + "कई अंतरराष्ट्रीय मैच जीते हैं। "
                                + "भारत में क्रिकेट के बहुत "
                                + "प्रशंसक हैं।"
                )
        );


        engine.addDocument(
                new Document(
                        3,
                        "हैदराबाद",
                        "हैदराबाद भारत के तेलंगाना "
                                + "राज्य का एक प्रमुख शहर है। "
                                + "हैदराबाद अपनी संस्कृति और "
                                + "तकनीकी उद्योग के लिए "
                                + "प्रसिद्ध है।"
                )
        );


        engine.addDocument(
                new Document(
                        4,
                        "तेलंगाना",
                        "तेलंगाना भारत का एक राज्य है। "
                                + "हैदराबाद तेलंगाना की राजधानी "
                                + "है। तेलंगाना अपनी संस्कृति "
                                + "और इतिहास के लिए जाना जाता है।"
                )
        );


        engine.addDocument(
                new Document(
                        5,
                        "भारतीय संस्कृति",
                        "भारतीय संस्कृति बहुत प्राचीन "
                                + "और विविध है। भारत में "
                                + "विभिन्न भाषाएं, परंपराएं "
                                + "और त्योहार मनाए जाते हैं।"
                )
        );


        Scanner scanner =
                new Scanner(System.in);


        System.out.println(
                "=========================================="
        );

        System.out.println(
                "       🇮🇳 BHARAT SMART SEARCH"
        );

        System.out.println(
                "   Indian Wikipedia Text Search Engine"
        );

        System.out.println(
                "=========================================="
        );


        while (true) {

            System.out.print(
                    "\n🔍 Enter keyword "
                            + "(type exit to stop): "
            );

            String query =
                    scanner.nextLine();


            if (
                    query.equalsIgnoreCase(
                            "exit"
                    )
            ) {

                break;
            }


            if (query.trim().isEmpty()) {

                System.out.println(
                        "Please enter a keyword."
                );

                continue;
            }


            List<SearchResult> results =
                    engine.search(query);


            System.out.println(
                    "\n========== SEARCH RESULTS =========="
            );


            if (results.isEmpty()) {

                System.out.println(
                        "❌ No matching documents found."
                );

            } else {

                int rank = 1;

                for (
                        SearchResult result
                        : results
                ) {

                    System.out.println(
                            "\n" + rank
                                    + ". 📄 "
                                    + result
                                    .getDocument()
                                    .getTitle()
                    );

                    System.out.println(
                            "   Occurrences: "
                                    + result
                                    .getFrequency()
                    );

                    rank++;
                }
            }


            System.out.println(
                    "\n========== SUGGESTIONS =========="
            );

            List<String> suggestions =
                    engine.suggestions(query);

            if (suggestions.isEmpty()) {

                System.out.println(
                        "No suggestions."
                );

            } else {

                for (String suggestion :
                        suggestions) {

                    System.out.println(
                            "→ " + suggestion
                    );
                }
            }
        }


        scanner.close();

        System.out.println(
                "\nSearch Engine Closed."
        );
    }
}