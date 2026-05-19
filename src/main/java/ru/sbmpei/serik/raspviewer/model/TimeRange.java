package ru.sbmpei.serik.raspviewer.model;

import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author SLakeev
 */
public class TimeRange implements Comparable<TimeRange> {

    private static final String TIME_FROM_GROUP_NAME = "from";
    private static final String TIME_TO_GROUP_NAME = "to";
    private static final String TIME_DELIMETER = ".";
    private static final String PATTERN_STRING = "(?<" + TIME_FROM_GROUP_NAME + ">\\d{1,2}" + Pattern.quote(TIME_DELIMETER) + "\\d{2})\\s-\\s(?<" + TIME_TO_GROUP_NAME + ">\\d{1,2}" + Pattern.quote(TIME_DELIMETER) + "\\d{2})";

    private final LocalTime from;
    private final LocalTime to;

    /**
     * Correct time string is "8.30 - 11.45"
     */
    private static final Pattern TIME_STRING_PATTERN = Pattern.compile(PATTERN_STRING);

    private TimeRange(LocalTime from, LocalTime to) {
        this.from = from;
        this.to = to;
    }

    public static TimeRange of(String timeString) {

        Matcher matcher = TIME_STRING_PATTERN.matcher(timeString);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Time string: " + timeString + ". Don't match the pattern: " + TIME_STRING_PATTERN);
        }

        String[] fromSplit = matcher.group("from").split(Pattern.quote(TIME_DELIMETER));
        String[] toSplit = matcher.group("to").split(Pattern.quote(TIME_DELIMETER));

        LocalTime from = LocalTime.of(Integer.parseInt(fromSplit[0]), Integer.parseInt(fromSplit[1]));
        LocalTime to = LocalTime.of(Integer.parseInt(toSplit[0]), Integer.parseInt(toSplit[1]));

        return new TimeRange(from, to);
    }

    public LocalTime getFrom() {
        return from;
    }

    public LocalTime getTo() {
        return to;
    }

    @Override
    public int compareTo(TimeRange o) {
        return from.compareTo(o.from);
    }

}
