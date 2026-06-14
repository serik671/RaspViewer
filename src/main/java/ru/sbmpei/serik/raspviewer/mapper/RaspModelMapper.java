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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static ru.sbmpei.serik.raspviewer.RaspPatterns.*;
import ru.sbmpei.serik.raspviewer.model.Group;
import ru.sbmpei.serik.raspviewer.model.StudGroup;
import ru.sbmpei.serik.raspviewer.model.StudSubject;
import ru.sbmpei.serik.raspviewer.model.Subject;
import static ru.sbmpei.serik.raspviewer.parser.model.StudSubject.EMPTY;
import ru.sbmpei.serik.raspviewer.parser.model.StudSubject.SubjectInfo;

/**
 *
 * @author SLakeev
 */
public class RaspModelMapper {

    private static final Logger LOGGER = LogManager.getLogger();

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
        List<String> subjectTitleList = subjectTitleList(studSubject.title());
        for (String studSubjectTitle : subjectTitleList) {
            addOneStudSubjectToGroup(studGroup, studSubjectTitle, studSubject.info(), timeKey, dayKey, type);
        }
    }

    private static void addOneStudSubjectToGroup(StudGroup studGroup, String studSubjectTitle, List<SubjectInfo> studSubjectInfo, String timeKey, DayOfWeek dayKey, StudSubject.Type type) {
        String audience = StringUtils.firstNonBlank(
                audienceFromSubjectInfo(studSubjectInfo),
                audienceFromValue(studSubjectTitle.strip())
        );

        List<Integer> weeks = ListUtils.union(
                weeksFromSubjectInfo(studSubjectInfo),
                weeksFromValue(studSubjectTitle)
        );
        List<Subject.Subgroup> subgroups = ListUtils.union(
                subgroupsFromSubjectInfo(studSubjectInfo),
                subgroupsFromValue(studSubjectTitle)
        );

        StudSubject subject = new StudSubject();

        if (StringUtils.isBlank(audience) || EMPTY.title().equals(audience)) {
            subject.setTitle(studSubjectTitle);
        } else {
            try {
                subject.setTitle(subjectName(studSubjectTitle));
            } catch (Exception e) {
                LOGGER.warn("В строке '{}' не удалось распознать название предмета.", studSubjectTitle);
                subject.setTitle(IO.readln("Введите его вручную: "));
            }

            try {
                subject.setTeachers(teachersFromSubjectTitle(studSubjectTitle));
            } catch (Exception e) {
                LOGGER.warn("В строке '{}' не удалось распознать преподавателя.", studSubjectTitle);
                subject.setTitle(IO.readln("Введите его вручную: "));
            }
        }

        subject.setDay(dayKey);
        subject.setTimeString(timeKey);
        subject.setType(type);
        subject.setAudience(audience);
        subject.setWeeks(weeks);
        subject.setSubgroups(subgroups);
        studGroup.getSubjects().add(subject);

        studSubjectInfo.stream()
                .filter(info -> info.type() == SubjectInfo.Type.ANOTHER_TIME)
                .map(info -> subject.withTimeString(info.value()))
                .forEach(studGroup.getSubjects()::add);
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

    private static List<String> teachersFromSubjectTitle(String title) throws Exception {
        List<String> teachers = TEACHER_PATTERN.matcher(title).results()
                .map(MatchResult::group)
                .collect(Collectors.toList());
        if (!teachers.isEmpty()) {
            return teachers;
        } else {
            throw new IllegalArgumentException("Don't find teacher in subject title: " + title);
        }
    }

    private static String subjectName(String title) throws Exception {
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

    private static List<String> subjectTitleList(String title) {
        List<String> titleList = new ArrayList<>();
        Matcher matcher = SUBJECT_FACTOR.matcher(title);
        int startIndex = -1;
        while (matcher.find()) {
            if (startIndex != -1) {
                titleList.add(title.substring(startIndex, matcher.start()));
            }
            startIndex = matcher.start();
        }
        if (startIndex == -1) {
            titleList.add(title);
        } else {
            titleList.add(title.substring(startIndex));
        }
        return titleList.stream().map(String::strip).toList();
    }

}
