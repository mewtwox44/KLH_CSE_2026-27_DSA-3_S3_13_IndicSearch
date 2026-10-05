import java.util.ArrayList;
import java.util.List;

public class MergeSort {


    public static void sort(
            List<SearchResult> results) {


        if (
                results.size() <= 1
        ) {

            return;
        }


        int mid =
                results.size() / 2;


        List<SearchResult> left =
                new ArrayList<>(
                        results.subList(
                                0,
                                mid
                        )
                );


        List<SearchResult> right =
                new ArrayList<>(
                        results.subList(
                                mid,
                                results.size()
                        )
                );


        sort(left);

        sort(right);


        merge(
                results,
                left,
                right
        );
    }


    private static void merge(
            List<SearchResult> results,
            List<SearchResult> left,
            List<SearchResult> right) {


        int i = 0;

        int j = 0;

        int k = 0;


        while (
                i < left.size()
                &&
                j < right.size()
        ) {


            if (
                    left.get(i)
                            .getFrequency()
                    >=
                    right.get(j)
                            .getFrequency()
            ) {


                results.set(
                        k++,
                        left.get(i++)
                );


            } else {


                results.set(
                        k++,
                        right.get(j++)
                );
            }
        }


        while (
                i < left.size()
        ) {

            results.set(
                    k++,
                    left.get(i++)
            );
        }


        while (
                j < right.size()
        ) {

            results.set(
                    k++,
                    right.get(j++)
            );
        }
    }
}