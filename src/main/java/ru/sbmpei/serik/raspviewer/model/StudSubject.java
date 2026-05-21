package ru.sbmpei.serik.raspviewer.model;

import java.time.DayOfWeek;
import java.util.List;
import lombok.Data;

/**
 *
 * @author SLakeev
 */
@Data
public class StudSubject implements Subject, Comparable<StudSubject> {

    private final String title;
    private final DayOfWeek day;
    private final String timeString;
    private final Type type;
    private final String audience;
    private final List<Integer> weeks;
    private final List<String> teachers;
    private final String subgroup;

    public static enum Type {
        NUMERATOR, DENOMINATOR, EVEN, ODD
    }

    @Override
    public int compareTo(StudSubject o) {
        if (day == o.day) {
            return TimeRange.of(timeString).compareTo(TimeRange.of(o.timeString));
        } else {
            return day.compareTo(o.day);
        }
    }

    public StudSubject withTimeString(String timeString) {
        return new StudSubject(title, day, timeString, type, audience, weeks, teachers, subgroup);
    }

    @Override
    public boolean isNumerator() {
        return type == Type.NUMERATOR;
    }

    @Override
    public boolean isDenominator() {
        return type == Type.DENOMINATOR;
    }

    @Override
    public boolean isEven() {
        return type == Type.EVEN;
    }

    @Override
    public boolean isOdd() {
        return type == Type.ODD;
    }

}
