package ru.sbmpei.serik.raspviewer;

import java.util.regex.Pattern;

/**
 *
 * @author SLakeev
 */
public class RaspPatterns {

    public static final Pattern CLASSES_INFO_PATTERN = Pattern.compile("\\d\\sи\\s\\d\\sпара");
    public static final Pattern SUBJECT_FACTOR = Pattern.compile("(^|\\s)[а-я]{1,3}\\s[А-Я][а-я]{2,}");

    public static final String COURSE_GROUP_NAME = "courseNumber";
    public static final Pattern COURSE_NUMBER_PATTERN = Pattern.compile("(?<" + COURSE_GROUP_NAME + ">\\d+)\\sкурс");

    public static final String WEEK_GROUP_NAME = "weeks";
    public static final String WEEK_DELIMITER = Pattern.quote(",");
    public static final Pattern WEEK_NUMBERS_PATTERN = Pattern.compile("(?<" + WEEK_GROUP_NAME + ">(\\d+" + WEEK_DELIMITER + ")*\\d+)\\sн\\.");

    public static final String WEEK_FROM_GROUP_NAME = "fromWeek";
    public static final String WEEK_TO_GROUP_NAME = "toWeek";
    public static final Pattern WEEK_PERIOD_PATTERN = Pattern.compile("(с\\s?)?(?<" + WEEK_FROM_GROUP_NAME + ">\\d+)\\s?(по|-)\\s?(?<" + WEEK_TO_GROUP_NAME + ">\\d+)\\s?(нед\\.|н\\.)");

    public static final Pattern AUDIENCE_PATTERN = Pattern.compile("\\d{1,3}$|[A-Я]\\s\\d{1,3}$|[A-я]+$");
    public static final Pattern SUBGROUP_PATTERN = Pattern.compile("\\d+\\sпгр.");
    public static final Pattern TEACHER_PATTERN = Pattern.compile("([a-я]+\\.)+\\s[A-я]+\\s[A-Я]\\.[A-Я]\\.");

    public static final Pattern SUBGROUP_WEEKS_PATTERN = Pattern.compile(SUBGROUP_PATTERN.pattern() + "\\s" + WEEK_NUMBERS_PATTERN.pattern());

}
