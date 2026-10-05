import java.text.Normalizer;

import java.util.ArrayList;
import java.util.List;

public class TextPreprocessor {


    public static String normalize(
            String text) {


        if (text == null) {
            return "";
        }


        text =
                Normalizer.normalize(
                        text,
                        Normalizer.Form.NFC
                );


        text =
                text.toLowerCase();


        text =
                text.replaceAll(
                        "[\\p{P}\\p{S}]",
                        " "
                );


        text =
                text.replaceAll(
                        "\\s+",
                        " "
                );


        return text.trim();
    }


    public static List<String> tokenize(
            String text) {


        String normalized =
                normalize(text);


        List<String> tokens =
                new ArrayList<>();


        if (
                normalized.isEmpty()
        ) {

            return tokens;
        }


        String[] words =
                normalized.split(
                        "\\s+"
                );


        for (String word : words) {

            if (
                    !word.isEmpty()
            ) {

                tokens.add(
                        word
                );
            }
        }


        return tokens;
    }
}