package ru.sbmpei.serik.raspviewer.mapper;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.sbmpei.serik.raspviewer.model.Group;
import ru.sbmpei.serik.raspviewer.model.StudGroup;
import ru.sbmpei.serik.raspviewer.model.StudSubject;
import ru.sbmpei.serik.raspviewer.model.Subject;
import ru.sbmpei.serik.raspviewer.parser.model.StudSubject.SubjectInfo;

/**
 *
 * @author SLakeev
 */
public class RaspModelMapper {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String COURSE_GROUP_NAME = "courseNumber";
    private static final Pattern COURSE_NUMBER_PATTERN = Pattern.compile("(?<" + COURSE_GROUP_NAME + ">\\d+)\\sкурс");

    private static final String WEEK_GROUP_NAME = "weeks";
    private static final String WEEK_DELIMITER = Pattern.quote(",");
    private static final Pattern WEEK_NUMBERS_PATTERN = Pattern.compile("(?<" + WEEK_GROUP_NAME + ">(\\d+" + WEEK_DELIMITER + ")*\\d+)\\sн\\.");

    private static final Pattern AUDIENCE_PATTERN = Pattern.compile("\\d{1,3}$|[A-Я]\\s\\d{1,3}$|[A-я]+$");
    private static final Pattern SUBGROUP_PATTERN = Pattern.compile("\\d+\\sпгр.");
    private static final Pattern PEDANTIC_TEACHER_PATTERN = Pattern.compile("([a-я]+\\.)+\\s[A-я]+\\s[A-Я]\\.[A-Я]\\.");
    private static final Pattern TEACHER_PATTERN = Pattern.compile("([a-я]+\\.)+\\s[A-Я].{1,20}\\s[A-Я]\\.[A-Я]\\.");

    private static final Pattern SUBGROUP_WEEKS_PATTERN = Pattern.compile(SUBGROUP_PATTERN.pattern() + "\\s" + WEEK_NUMBERS_PATTERN.pattern());

    public static List<Group> transformRaspModel(Map<String, ru.sbmpei.serik.raspviewer.parser.model.StudGroup> raspModel) {
        List<Group> groups = new ArrayList<>();
        raspModel.forEach((key, value) -> {
            Matcher matcher = COURSE_NUMBER_PATTERN.matcher(value.info());
            String courseString = matcher.find() ? matcher.group(COURSE_GROUP_NAME) : "0";
            int courseNumber = Integer.parseInt(courseString);
            StudGroup studGroup = new StudGroup(key, courseNumber);
            value.days().forEach((dayKey, dayValue) -> {
                dayValue.workSubjects().forEach((timeKey, subject) -> {
                    ru.sbmpei.serik.raspviewer.parser.model.StudSubject numeratorSubject = subject.getNumeratorSubject();
                    ru.sbmpei.serik.raspviewer.parser.model.StudSubject denominatorSubject = subject.getDenominatorSubject();
                    ru.sbmpei.serik.raspviewer.parser.model.StudSubject evenSubject = subject.getEvenSubject();
                    ru.sbmpei.serik.raspviewer.parser.model.StudSubject oddSubject = subject.getOddSubject();
                    if (!emptyOrNullParserStudSubject(numeratorSubject)) {
                        addStudSubjectToGroup(studGroup, numeratorSubject, timeKey, dayKey, StudSubject.Type.NUMERATOR);
                    }
                    if (!emptyOrNullParserStudSubject(denominatorSubject)) {
                        addStudSubjectToGroup(studGroup, denominatorSubject, timeKey, dayKey, StudSubject.Type.DENOMINATOR);
                    }
                    if (!emptyOrNullParserStudSubject(evenSubject)) {
                        addStudSubjectToGroup(studGroup, evenSubject, timeKey, dayKey, StudSubject.Type.EVEN);
                    }
                    if (!emptyOrNullParserStudSubject(oddSubject)) {
                        addStudSubjectToGroup(studGroup, oddSubject, timeKey, dayKey, StudSubject.Type.ODD);
                    }
                });
            });
            groups.add(studGroup);
        });
        return groups;
    }

    private static boolean emptyOrNullParserStudSubject(ru.sbmpei.serik.raspviewer.parser.model.StudSubject subject) {
        return subject == null || ru.sbmpei.serik.raspviewer.parser.model.StudSubject.EMPTY.equals(subject);
    }

    private static void addStudSubjectToGroup(StudGroup studGroup, ru.sbmpei.serik.raspviewer.parser.model.StudSubject studSubject, String timeKey, DayOfWeek dayKey, StudSubject.Type type) {
        String audience = StringUtils.firstNonBlank(
                audienceFromValue(studSubject.title().strip()),
                audienceFromSubjectInfo(studSubject.info())
        );

        List<Integer> weeks = ListUtils.union(
                weeksFromValue(studSubject.title()),
                weeksFromSubjectInfo(studSubject.info())
        );
        List<Subject.Subgroup> subgroups = ListUtils.union(
                subgroupsFromValue(studSubject.title()),
                subgroupsFromSubjectInfo(studSubject.info())
        );

        StudSubject subject = new StudSubject(subjectName(studSubject.title()),
                dayKey, timeKey, type, audience,
                weeks,
                teachersFromSubjectTitle(studSubject.title()),
                subgroups
        );
        studSubject.info().stream()
                .filter(info -> info.type() == SubjectInfo.Type.ANOTHER_TIME)
                .map(info -> subject.withTimeString(info.value()))
                .forEach(studGroup.getSubjects()::add);
        studGroup.getSubjects().add(subject);
    }

    private static List<Integer> weeksFromSubjectInfo(List<SubjectInfo> info) {
        Optional<SubjectInfo> subjectInfoOptional = info.stream()
                .filter(it -> it.type() == SubjectInfo.Type.CLASSES)
                .findFirst();

        if (subjectInfoOptional.isPresent()) {
            String value = subjectInfoOptional.get().value();
            return weeksFromValue(value);
        }

        return List.of();
    }

    private static List<Integer> weeksFromValue(String value) {
        Matcher matcher = WEEK_NUMBERS_PATTERN.matcher(value);
        List<Integer> weeksList = new ArrayList();
        while (matcher.find()) {
            String weeks = matcher.group(WEEK_GROUP_NAME);
            List<Integer> collectedWeeks = Stream.of(weeks.split(WEEK_DELIMITER))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            weeksList.addAll(collectedWeeks);
        }
        return Collections.unmodifiableList(weeksList);
    }

    private static List<String> teachersFromSubjectTitle(String title) {
        return TEACHER_PATTERN.matcher(title).results()
                .map(MatchResult::group)
                .map(RaspModelMapper::pedanticTeacherCheck)
                .collect(Collectors.toList());
    }

    private static String pedanticTeacherCheck(String teacher) {
        Matcher matcher = PEDANTIC_TEACHER_PATTERN.matcher(teacher);
        if (!matcher.find()) {
            String otherTeacher = IO.readln("!Возможно! преподаватель (" + teacher + ") указан некорректно."
                    + "\nВведите преподавателя корректно, или нажмите Enter: ");
            return StringUtils.isBlank(otherTeacher) ? teacher : otherTeacher;
        }
        if (!Objects.equals(matcher.group(), teacher)) {
            LOGGER.warn("The teacher '{}' parsed not correct", teacher);
        }
        return teacher;
    }

    private static String subjectName(String title) {
        Matcher matcher = TEACHER_PATTERN.matcher(title);
        if (matcher.find()) {
            return title.substring(0, matcher.start() - 1).strip();
        } else {
            throw new IllegalArgumentException("Don't find teacher in subject title: " + title);
        }
    }

    private static String audienceFromSubjectInfo(List<SubjectInfo> info) {
        for (SubjectInfo subjectInfo : info) {
            switch (subjectInfo.type()) {
                case SubjectInfo.Type.AUDIENCE -> {
                    return subjectInfo.value();
                }
                case SubjectInfo.Type.CLASSES -> {
                    String infoValue = subjectInfo.value().strip();
                    return Objects.requireNonNull(audienceFromValue(infoValue),
                            "Don't find audience for value: " + infoValue);
                }
            }
        }
        return null;
    }

    private static String audienceFromValue(String value) {
        Matcher matcher = AUDIENCE_PATTERN.matcher(value);
        if (matcher.find()) {
            return matcher.group();
        }

        return null;
    }

    private static List<Subject.Subgroup> subgroupsFromSubjectInfo(List<SubjectInfo> info) {
        for (SubjectInfo subjectInfo : info) {
            if (subjectInfo.type() == SubjectInfo.Type.CLASSES) {
                return subgroupsFromValue(subjectInfo.value());
            }
        }
        return List.of();
    }

    private static List<Subject.Subgroup> subgroupsFromValue(String value) {
        Matcher matcher = SUBGROUP_WEEKS_PATTERN.matcher(value);
        List<Subject.Subgroup> subgroups = new ArrayList();
        while (matcher.find()) {
            String subgroupWeeksValue = matcher.group();
            String subgroupName = Objects.requireNonNull(subgroupFromValue(subgroupWeeksValue),
                    "Don't find subgroup for value: " + value
                    + ". Subgroup weeks value is " + subgroupWeeksValue);

            List<Integer> weeksFromValue = weeksFromValue(subgroupWeeksValue);
            subgroups.add(StudSubject.StudSubgroup.of(subgroupName, weeksFromValue));
        }
        return Collections.unmodifiableList(subgroups);
    }

    private static String subgroupFromValue(String value) {
        Matcher matcher = SUBGROUP_PATTERN.matcher(value);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

}
