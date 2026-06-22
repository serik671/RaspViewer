package ru.sbmpei.serik.raspviewer.util;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author SLakeev
 */
public class FuzzySubstringUtils {

    public static List<Integer> substringMatches(String text, String query) {
        List<Integer> matches = new ArrayList<>();

        for (int q = 0, i = 0; i < text.length() && q < query.length(); i++) {
            if (text.charAt(i) == query.charAt(q)) {
                matches.add(i);
                q++;
            }
        }

        return matches;
    }

    public static List<Integer> substringMatches(String text, String query, int offset) {
        List<Integer> substringMatches = substringMatches(text.substring(offset), query);
        return substringMatches.stream().map(it -> it + offset).toList();
    }

    public static List<Integer> bestSubstringMatches(String text, String query) {
        List<Integer> beginIndexList = new ArrayList<>();

        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == query.charAt(0)) {
                beginIndexList.add(i);
            }
        }

        return beginIndexList.stream()
                .map(index -> substringMatches(text, query, index))
                .sorted((a, b) -> {
                    return Float.compare(score(a), score(b));
                }).findFirst().orElse(List.of());
    }

    private static float score(List<Integer> values) {
        float score = 0.f;
        for (int i = 0; i < values.size() - 1; i++) {
            int d = values.get(i + 1) - values.get(i);
            score += Math.abs(d);
        }

        return values.isEmpty() ? score : score / values.size();
    }

}
