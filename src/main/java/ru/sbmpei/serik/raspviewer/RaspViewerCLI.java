package ru.sbmpei.serik.raspviewer;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import ru.sbmpei.serik.raspviewer.model.StudGroup;
import ru.sbmpei.serik.raspviewer.model.StudSubject;

/**
 *
 * @author SLakeev
 */
public class RaspViewerCLI implements Runnable {

    private final String HELP_CMD = "help";
    private final String QUIT_CMD = "quit";

    private final String GROUPS_CMD = "groups";
    private final String SELECT_CMD = "select";
    private final String SHOW_CMD = "show";

    private final List<StudGroup> groups;
    private final LocalDate beginDate;

    private String currentGroup = null;

    public RaspViewerCLI(List<StudGroup> groups, LocalDate beginDate) {
        this.groups = groups;
        this.beginDate = beginDate;
    }

    @Override
    public void run() {
        while (true) {
            String prompt = StringUtils.isBlank(currentGroup) ? "RaspViewer_> " : "RaspViewer_" + currentGroup + "_> ";
            String cmd = IO.readln(prompt);
            if (QUIT_CMD.equals(cmd)) {
                break;
            }
            if (HELP_CMD.equals(cmd)) {
                showMenu();
                continue;
            }
            if (GROUPS_CMD.equals(cmd)) {
                showGroups();
                continue;
            }
            if (SELECT_CMD.equals(cmd)) {
                String groupName = IO.readln("Group name: ");
                selectGroup(groupName);
                continue;
            }
            if (SHOW_CMD.equals(cmd)) {
                showDay();
                continue;
            }
            IO.println("Not correct command");
        }

    }

    private void showMenu() {
        IO.println(GROUPS_CMD + " - Show groups list");
        IO.println(SELECT_CMD + " (group name) - select group");
        IO.println(SHOW_CMD + "[date(yyyy-MM-dd)] - Show stud day for date");
    }

    private void showGroups() {
        groups.stream().map(StudGroup::getName).forEach(IO::println);
    }

    private void selectGroup(String title) {
        String groupName = groups.stream().map(StudGroup::getName)
                .filter(it -> it.equals(title)).findFirst()
                .orElse("");
        if (StringUtils.isBlank(groupName)) {
            IO.println("Don't found group: " + title);
        } else {
            currentGroup = groupName;
        }
    }

    private void showDay() {
        try {
            LocalDate date = LocalDate.parse(IO.readln("Date: "));
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            int week = week(date);
            IO.println(dayOfWeek + " " + week + " week");
            StudGroup studGroup = groups.stream().filter(it -> it.getName().equals(currentGroup)).findFirst().get();
            studGroup.getSubjects().stream().sorted()
                    .filter(it -> it.getDay() == dayOfWeek)
                    .filter(it -> {
                        if (week % 2 == 0) {
                            return it.getType() == StudSubject.Type.DENOMINATOR
                                    || it.getType() == StudSubject.Type.EVEN;
                        } else {
                            return it.getType() == StudSubject.Type.NUMERATOR
                                    || it.getType() == StudSubject.Type.ODD;
                        }
                    }).filter(it -> {
                if (it.getWeeks().isEmpty()) {
                    return true;
                } else {
                    return it.getWeeks().contains(week);
                }
            }).forEach(subject -> {
                IO.print(subject.getTimeString() + "|" + subject.getTitle() + "|" + subject.getAudience());
                if (StringUtils.isNotBlank(subject.getSubgroup())) {
                    IO.println("|" + "!!! " + subject.getSubgroup() + " !!!");
                }
                IO.println();
            });
        } catch (Exception e) {
            IO.println(e.getMessage());
        }
    }

    private int week(LocalDate date) {
        return (int) ChronoUnit.WEEKS.between(beginDate, date) + 1;
    }

}
