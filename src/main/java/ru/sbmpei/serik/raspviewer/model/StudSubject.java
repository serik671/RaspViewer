package ru.sbmpei.serik.raspviewer.model;

import java.time.DayOfWeek;
import java.util.regex.Pattern;

/**
 *
 * @author SLakeev
 */
public class StudSubject implements Comparable<StudSubject> {

    private final String title;
    private final String timeString;
    private final DayOfWeek day;

    private final Type type;

    public static enum Type {
        NUMERATOR, DENOMINATOR, EVEN, ODD
    }

    public StudSubject(String title, String timeString, DayOfWeek day, Type type) {
        this.title = title;
        this.timeString = timeString;
        this.day = day;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public String getTimeString() {
        return timeString;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public Type getType() {
        return type;
    }

    @Override
    public int compareTo(StudSubject o) {
        if (day == o.day) {
            return TimeRange.of(timeString).compareTo(TimeRange.of(o.timeString));
        } else {
            return day.compareTo(o.day);
        }
    }

}
