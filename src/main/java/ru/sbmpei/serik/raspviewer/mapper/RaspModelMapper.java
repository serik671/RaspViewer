package ru.sbmpei.serik.raspviewer.mapper;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import ru.sbmpei.serik.raspviewer.model.Group;
import ru.sbmpei.serik.raspviewer.model.StudGroup;
import ru.sbmpei.serik.raspviewer.model.StudSubject;
import ru.sbmpei.serik.raspviewer.parser.model.StudSubject.SubjectInfo;

/**
 *
 * @author SLakeev
 */
public class RaspModelMapper {

    private static final String COURSE_GROUP_NAME = "courseNumber";
    private static final Pattern COURSE_NUMBER_PATTERN = Pattern.compile("(?<" + COURSE_GROUP_NAME + ">\\d+)\\sкурс");

    private static final String WEEK_GROUP_NAME = "weeks";
    private static final String WEEK_DELIMITER = Pattern.quote(",");
    private static final Pattern WEEK_NUMBERS_PATTERN = Pattern.compile("(?<" + WEEK_GROUP_NAME + ">(\\d+" + WEEK_DELIMITER + ")*\\d+)\\sн\\.");

    private static final Pattern AUDIENCE_PATTERN = Pattern.compile("[A-Я]\\s\\d{1,3}|\\d{3}");
    private static final Pattern SUBGROUP_PATTERN = Pattern.compile("\\d+\\sпгр.");
    private static final Pattern TEACHERS_PATTERN = Pattern.compile("([a-я]+\\.)+\\s[A-я]+\\s[A-Я]\\.[A-Я]\\.");

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
                audienceFromValue(studSubject.title()),
                audienceFromSubjectInfo(studSubject.info())
        );

        List<Integer> weeks = ListUtils.union(
                weeksFromValue(studSubject.title()),
                weeksFromSubjectInfo(studSubject.info())
        );

        StudSubject subject = new StudSubject(clearSubjectTitle(studSubject.title()),
                dayKey, timeKey, type, audience,
                weeks,
                teachersFromSubjectTitle(studSubject.title()),
                subgroupFromSubjectInfo(studSubject.info())
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
            weeksFromValue(value);
        }

        return List.of();
    }

    private static List<Integer> weeksFromValue(String value) {
        Matcher matcher = WEEK_NUMBERS_PATTERN.matcher(value);
        if (matcher.find()) {
            String weeks = matcher.group(WEEK_GROUP_NAME);
            return Stream.of(weeks.split(WEEK_DELIMITER))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    private static List<String> teachersFromSubjectTitle(String title) {
        return TEACHERS_PATTERN.matcher(title).results()
                .map(MatchResult::group)
                .collect(Collectors.toList());
    }

    private static String clearSubjectTitle(String title) {
        return cutFromText(title, TEACHERS_PATTERN,
                r1 -> cutFromText(r1, AUDIENCE_PATTERN,
                        r2 -> cutFromText(r2, WEEK_NUMBERS_PATTERN,
                                String::strip
                        )
                )
        );
    }

    private static String cutFromText(String text, Pattern p, Function<String, String> action) {
        return action.apply(p.matcher(text).replaceAll(StringUtils.EMPTY));
    }

    private static String audienceFromSubjectInfo(List<SubjectInfo> info) {
        for (SubjectInfo subjectInfo : info) {
            switch (subjectInfo.type()) {
                case SubjectInfo.Type.AUDIENCE -> {
                    return subjectInfo.value();
                }
                case SubjectInfo.Type.CLASSES -> {
                    return audienceFromValue(subjectInfo.value());
                }
            }
        }
        return null;
    }

    private static String subgroupFromSubjectInfo(List<SubjectInfo> info) {
        for (SubjectInfo subjectInfo : info) {
            if (subjectInfo.type() == SubjectInfo.Type.CLASSES) {
                Matcher matcher = SUBGROUP_PATTERN.matcher(subjectInfo.value());
                if (matcher.find()) {
                    return matcher.group();
                }
            }
        }
        return null;
    }

    private static String audienceFromValue(String value) {
        Matcher matcher = AUDIENCE_PATTERN.matcher(value);
        return matcher.find() ? matcher.group() : null;
    }

}
