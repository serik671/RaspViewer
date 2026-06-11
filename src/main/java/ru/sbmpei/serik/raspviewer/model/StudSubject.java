package ru.sbmpei.serik.raspviewer.model;

import java.time.DayOfWeek;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author SLakeev
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudSubject implements Subject, Comparable<StudSubject> {

    private String title;
    private DayOfWeek day;
    private String timeString;
    private Type type;
    private String audience;
    private List<Integer> weeks;
    private List<String> teachers;
    private List<Subject.Subgroup> subgroups;

    public static enum Type {
        NUMERATOR, DENOMINATOR, EVEN, ODD
    }

    @Data
    public static class StudSubgroup implements Subject.Subgroup {

        private String name;
        private List<Integer> weeks;

        private StudSubgroup(String name, List<Integer> weeks) {
            this.name = name;
            this.weeks = weeks;
        }

        public static Subgroup of(String name, List<Integer> weeks) {
            return new StudSubgroup(name, weeks);
        }

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
        return new StudSubject(title, day, timeString, type, audience, weeks, teachers, subgroups);
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
