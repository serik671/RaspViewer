package ru.sbmpei.serik.raspviewer.util;

import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author SLakeev
 */
public class FuzzySubstringUtilsTest {

    private String substring(String text, List<Integer> indexList) {
        StringBuilder result = new StringBuilder();
        indexList.stream().map(text::charAt).forEach(result::append);
        return result.toString();
    }

    @Test
    public void substringMatchesTest() {
        String text = "кр Источники и приемники оптического излучения   доц. А стахов С.П. 2,6,10,14 н.";
        String query = "доц. Астахов С.П.";
        List<Integer> matches = FuzzySubstringUtils.substringMatches(text, query);
        IO.println(matches);
        assertThat(matches, contains(49, 50, 51, 52, 53, 54, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66));
        assertThat(substring(text, matches), is("доц. Астахов С.П."));
    }

    @Test
    public void multiSubstringMatchesTest() {
        String text = "кр Источники и дом церковь. Оптического излучения   доц. А стахов С.П. 2,6,10,14 н.";
        String query = "доц. Астахов С.П.";
        List<Integer> matches = FuzzySubstringUtils.substringMatches(text, query);
        IO.println(matches);
        assertThat(matches, contains(15, 16, 19, 26, 27, 57, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69));
        assertThat(substring(text, matches), is("доц. Астахов С.П."));
    }

    @Test
    public void bestSubstringMatchesTest() {
        String text = "кр Источники и приемники оптического излучения   доц. А стахов С.П. 2,6,10,14 н.";
        String query = "доц. Астахов С.П.";
        List<Integer> matches = FuzzySubstringUtils.bestSubstringMatches(text, query);
        IO.println(matches);
        assertThat(matches, contains(49, 50, 51, 52, 53, 54, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66));
        assertThat(substring(text, matches), is("доц. Астахов С.П."));
    }

    @Test
    public void bestMultiSubstringMatchesTest() {
        String text = "кр Источники и дом церковь. Оптического излучения   доц. А стахов С.П. 2,6,10,14 н.";
        String query = "доц. Астахов С.П.";
        List<Integer> matches = FuzzySubstringUtils.bestSubstringMatches(text, query);
        IO.println(matches);
        assertThat(matches, contains(52, 53, 54, 55, 56, 57, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69));
        assertThat(substring(text, matches), is("доц. Астахов С.П."));
    }

    @Test
    public void notFullMatchesTest() {
        String text = "кр Источники и дом церковь. Оптического излучения   доц. А стахов С.П";
        String query = "доц. Астахов С.П.";
        List<Integer> matches = FuzzySubstringUtils.bestSubstringMatches(text, query);
        IO.println(matches);
        assertThat(matches, contains(52, 53, 54, 55, 56, 57, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68));
        assertThat(substring(text, matches), is("доц. Астахов С.П"));
    }

}
