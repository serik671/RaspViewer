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

    @Test
    public void matchMoreThenHalfTest() {
        String text = "лк Железобетонные стаки и каменные произвольные конструкции         ст.пр. Патенч";
        String query = "ст.пр. Патенченкова М.А.";
        List<Integer> matches = FuzzySubstringUtils.bestSubstringMatches(text, query);
        IO.println(matches);
        String subjectTitle = text.substring(0, matches.getFirst()).strip();
        assertThat(subjectTitle, is("лк Железобетонные стаки и каменные произвольные конструкции"));
        assertThat(substring(text, matches), is("ст.пр. Патенч"));
    }

    @Test
    public void textRadarSubstringTest() {
        String text = "лк Железобетонные стаки и каменные произвольные конструкции         стпрПатенченкова";
        String query = "ст.пр. Патенченкова М.А.";
        String substring = FuzzySubstringUtils.textRadarSubstring(text, query);
        assertThat(substring, is("стпрПатенченкова"));
    }

    @Test
    public void substringBeginIndexTest() {
        String text = "кр Источники и приемники оптического излучения   доц. А стахов С.П. 2,6,10,14 н.";
        String query = "доц. Астахов С.П.";
        int substringBeginIndex = FuzzySubstringUtils.substringBeginIndex(text, query);
        assertThat(text.substring(0, substringBeginIndex).strip(), is("кр Источники и приемники оптического излучения"));
    }

    @Test
    public void substringBeginIndexTest2() {
        String text = "кр Источники и дом церковь. Оптического излучения   доц. А стахов С.П. 2,6,10,14 н.";
        String query = "доц. Астахов С.П.";
        int substringBeginIndex = FuzzySubstringUtils.substringBeginIndex(text, query);
        assertThat(text.substring(0, substringBeginIndex).strip(), is("кр Источники и дом церковь. Оптического излучения"));
    }

    @Test
    public void substringBeginIndexTest3() {
        String text = "кр Источники и дом церковь. Оптического излучения   доц. А стахов С.П";
        String query = "доц. Астахов С.П.";
        int substringBeginIndex = FuzzySubstringUtils.substringBeginIndex(text, query);
        assertThat(text.substring(0, substringBeginIndex).strip(), is("кр Источники и дом церковь. Оптического излучения"));
    }

    @Test
    public void substringBeginIndexTest4() {
        String text = "лк Железобетонные стаки и каменные произвольные конструкции         ст.пр. Патенч";
        String query = "ст.пр. Патенченкова М.А.";
        int substringBeginIndex = FuzzySubstringUtils.substringBeginIndex(text, query);
        assertThat(text.substring(0, substringBeginIndex).strip(), is("лк Железобетонные стаки и каменные произвольные конструкции"));
    }

    @Test
    public void substringBeginIndexTest5() {
        String query = "ст. пр. Иванова С. П.";
        String text
                = "лк Теория проектирования стпр. Иванова С. П";
        int substringBeginIndex = FuzzySubstringUtils.substringBeginIndex(text, query);
        assertThat(text.substring(0, substringBeginIndex).strip(), is("лк Теория проектирования"));
    }

    @Test
    public void substringBeginIndexTest6() {
        String query = "ст. пр. Иванова С. П.";
        String text
                = "лк Теория проектирования стпр. Иванова С. П";
        String substring = FuzzySubstringUtils.textRadarSubstring(text, query);
        assertThat(substring, is("стпр. Иванова С. П"));
    }

    @Test
    public void substringBeginIndexTest7() {
        String query = "доц. Блинов А.О.";
        String text
                = "лк Сопротивление материалов                                                                     доц Блинов А.О.";
        String substring = FuzzySubstringUtils.textRadarSubstring(text, query);
        assertThat(substring, is("доц Блинов А.О."));
        int index = FuzzySubstringUtils.substringBeginIndex(text, query);
        assertThat(text.substring(0, index).strip(), is("лк Сопротивление материалов"));
    }

    @Test
    public void substringBeginIndexNotFoundTest() {
        String query = "доц. Заводянская Е.А.";
        String text
                = "лк Теория проектирования стпр. Иванова С. П";
        int index = FuzzySubstringUtils.substringBeginIndex(text, query);
        assertThat(index, is(-1));
    }

    @Test
    public void substringBeginIndexNotFoundTest2() {
        String query = "доц. Заводянская Е.А.";
        String text
                = "у Иностранный язык                                                                       доц Макерова Н.В.";
        int index = FuzzySubstringUtils.substringBeginIndex(text, query);
        assertThat(index, is(-1));
    }

}
