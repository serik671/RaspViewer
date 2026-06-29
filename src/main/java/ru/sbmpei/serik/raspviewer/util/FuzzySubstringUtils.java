package ru.sbmpei.serik.raspviewer.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author SLakeev
 */
public class FuzzySubstringUtils {

    private static final Logger LOGGER = LogManager.getLogger();

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

    private static List<Integer> substringMatches(String text, String query, int offset) {
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
                .filter(matches -> (((float) matches.size() / query.length()) > .5f)) // Если совпадений больше половины
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

    private static record MatchGroup(
            int queryStart,
            int textStart,
            int length
            ) {

    }

    private static double relevance(List<MatchGroup> groups, String query) {
        int matchedChars = 0;

        for (MatchGroup g : groups) {
            matchedChars += g.length;
        }

        double k = (double) matchedChars / query.length();
        return k > 1.0 ? 1.0 / k : k;
    }

    private static List<MatchGroup> findGroups(String query, String text) {
        List<MatchGroup> groups = new ArrayList<>();

        int queryLength = query.length();
        int textLength = text.length();

        boolean[][] matrix = new boolean[queryLength][textLength];

        // Матрица совпадений
        for (int i = 0; i < queryLength; i++) {
            for (int j = 0; j < textLength; j++) {
                matrix[i][j]
                        = Character.toLowerCase(query.charAt(i))
                        == Character.toLowerCase(text.charAt(j));
            }
        }

        // Поиск диагональных последовательностей
        for (int i = 0; i < queryLength; i++) {
            for (int j = 0; j < textLength; j++) {
                if (!matrix[i][j]) {
                    continue;
                }
                if (i > 0 && j > 0 && matrix[i - 1][j - 1]) {
                    continue;
                }
                int length = 0;
                int queryIndex = i;
                int textIndex = j;
                while (queryIndex < queryLength
                        && textIndex < textLength
                        && matrix[queryIndex][textIndex]) {
                    length++;
                    queryIndex++;
                    textIndex++;
                }
                if (length > 1) {
                    groups.add(new MatchGroup(i, j, length));
                }
            }
        }
        return groups;
    }

    private static List<MatchGroup> selectBestGroups(List<MatchGroup> groups) {

        groups.sort(Comparator.comparingInt(MatchGroup::length).reversed());

        List<MatchGroup> result = new ArrayList<>();

        boolean[] usedQuery = new boolean[Short.MAX_VALUE];
        boolean[] usedText = new boolean[Short.MAX_VALUE];

        for (MatchGroup g : groups) {
            boolean overlap = false;
            for (int i = 0; i < g.length; i++) {
                if (usedQuery[g.queryStart + i] || usedText[g.textStart + i]) {
                    overlap = true;
                    break;
                }
            }
            if (!overlap) {
                result.add(g);
                for (int i = 0; i < g.length; i++) {
                    usedQuery[g.queryStart + i] = true;
                    usedText[g.textStart + i] = true;
                }
            }
        }
        result.sort(Comparator.comparingInt(MatchGroup::queryStart));
        return result;
    }

    private static String extractMatchGroups(String text, List<MatchGroup> groups) {
        StringBuilder result = new StringBuilder();
        for (MatchGroup group : groups) {
            result.append(text.substring(group.textStart, group.textStart + group.length));
        }

        return result.toString();
    }

    public static String textRadarSubstring(String text, String query) {
        List<MatchGroup> findGroups = findGroups(query, text);
        List<MatchGroup> groups = selectBestGroups(findGroups);
        double relevance = relevance(groups, query);
        LOGGER.info("\nText: {}\nQuery: {}\nRelevance: {}", text, query, relevance);
        return extractMatchGroups(text, groups);
    }

    public static int substringBeginIndex(String text, String substring) {
        List<MatchGroup> findGroups = findGroups(substring, text);
        IO.println("Groups: ");
        for (MatchGroup g : findGroups) {
            LOGGER.info(g);
        }
        List<MatchGroup> groups = selectBestGroups(findGroups);
        IO.println("Best groups: ");
        for (MatchGroup g : groups) {
            LOGGER.info(g);
        }
        double relevance = relevance(findGroups, text);
        String radarText = extractMatchGroups(text, groups);
        LOGGER.info("\nText: {}\nSubstring: {}\nRelevance: {}\nRadarText: {}",
                text,
                substring,
                relevance,
                radarText);
        if (relevance < 0.5) {
            return FuzzySubstringUtils
                    .bestSubstringMatches(text, substring)
                    .stream().findFirst().orElse(-1);
        }
        return text.indexOf(radarText);
    }

}
