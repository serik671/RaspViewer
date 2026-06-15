package ru.sbmpei.serik.raspviewer.mapper;

import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;
import ru.sbmpei.serik.raspviewer.model.Group;
import ru.sbmpei.serik.raspviewer.model.Subject;
import ru.sbmpei.serik.raspviewer.parser.model.StudGroup;
import ru.sbmpei.serik.raspviewer.parser.model.StudSubject;
import ru.sbmpei.serik.raspviewer.parser.model.StudSubject.SubjectInfo;
import ru.sbmpei.serik.raspviewer.parser.model.WorkDay;
import ru.sbmpei.serik.raspviewer.parser.model.WorkSubject;

/**
 *
 * @author sl556
 */
public class RaspModelMapperTest {

    @Test
    public void numeratorSubjectTest() {
        Map<String, StudGroup> params = new HashMap<>();
        WorkSubject workSubject = new WorkSubject();
        workSubject.setNumeratorSubject(
                new StudSubject("лк Сопровождение программного обеспечения ст.пр. Гаврилов А.И.",
                        List.of(new SubjectInfo("421", SubjectInfo.Type.AUDIENCE)))
        );
        params.put("ТГ-01", new StudGroup(
                Map.of(DayOfWeek.MONDAY, new WorkDay(
                        Map.of("8.30-9.45", workSubject))),
                "1 курс"));

        List<Group> groups = RaspModelMapper.transformRaspModel(params);

        assertThat(groups, hasSize(1));

        Group group = groups.getFirst();
        assertThat(group.getCourseNumber(), is(1));
        assertThat(group.getName(), is("ТГ-01"));

        assertThat(group.getSubjects(), hasSize(1));
        Subject subject = group.getSubjects().getFirst();
        assertThat(subject.getTitle(), is("лк Сопровождение программного обеспечения"));
        assertThat(subject.isNumerator(), is(true));
        assertThat(subject.isDenominator(), is(false));
        assertThat(subject.isEven(), is(false));
        assertThat(subject.isOdd(), is(false));
        assertThat(subject.getDay(), is(DayOfWeek.MONDAY));
        assertThat(subject.getAudience(), is("421"));
        assertThat(subject.getTimeString(), is("8.30-9.45"));
        assertThat(subject.getWeeks(), empty());
        assertThat(subject.getTeachers(), hasSize(1));

        String teacher = subject.getTeachers().getFirst();
        assertThat(teacher, is("ст.пр. Гаврилов А.И."));

    }

}
