package ru.sbmpei.serik.raspviewer.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import ru.sbmpei.serik.raspviewer.model.Group;
import ru.sbmpei.serik.raspviewer.model.Subject;

/**
 *
 * @author SLakeev
 */
public class RaspService implements Service {

    private final List<? extends Group> groups;
    private final LocalDate semesterStartDate;

    public RaspService(List<? extends Group> groups, LocalDate semesterStartDate) {
        this.groups = groups;
        this.semesterStartDate = semesterStartDate;
    }

    @Override
    public List<? extends Group> groupList() {
        return groups;
    }

    @Override
    public List<Subject> subjectsOfGroupForDays(List<LocalDate> days, String groupName) {
        return days.stream().map(day -> subjectsForOneDay(day, groupName))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public int currentWeek(LocalDate date) {
        return (int) ChronoUnit.WEEKS.between(semesterStartDate, date) + 1;
    }

    private List<Subject> subjectsForOneDay(LocalDate date, String groupName) {
        DayOfWeek day = date.getDayOfWeek();
        Group group = groups.stream().filter(it -> it.getName().equals(groupName)).findFirst().get();
        int week = currentWeek(date);
        return group.getSubjects().stream().sorted()
                .filter(it -> it.getDay() == day)
                .filter(it -> weekTypeFilter(it, week))
                .filter(it -> containsWeek(it, week))
                .collect(Collectors.toList());
    }

    private boolean weekTypeFilter(Subject s, int week) {
        return (week % 2 == 0)
                ? (s.isDenominator() || s.isEven())
                : (s.isNumerator() || s.isOdd());
    }

    private boolean containsWeek(Subject s, int week) {
        List<Integer> weeks = s.getWeeks();
        return weeks.isEmpty() ? true : weeks.contains(week);
    }

}
