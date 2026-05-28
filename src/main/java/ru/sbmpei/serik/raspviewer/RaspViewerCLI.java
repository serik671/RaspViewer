package ru.sbmpei.serik.raspviewer;

import java.time.LocalDate;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import ru.sbmpei.serik.raspviewer.model.Group;
import ru.sbmpei.serik.raspviewer.service.Service;

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

    private final Service service;

    public RaspViewerCLI(Service service) {
        this.service = service;
    }

    private String currentGroup = null;

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
        service.groupList().stream().map(Group::getName).forEach(IO::println);
    }

    private void selectGroup(String title) {
        String groupName = service.groupList().stream().map(Group::getName)
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
            LocalDate date = LocalDate.parse(IO.readln("Date (yyyy-MM-dd): "));
            IO.println(date.getDayOfWeek() + " " + service.currentWeek(date) + " week");
            service.subjectsOfGroupForDays(List.of(date), currentGroup)
                    .forEach(subject -> {
                        IO.print(subject.getTimeString() + "|" + subject.getTitle() + "|" + subject.getAudience());
                        if (!subject.getTeachers().isEmpty()) {
                            IO.print("|" + subject.getTeachers());
                        }
                        subject.getSubgroups().forEach(sg -> {
                            IO.print("|" + sg.getName() + ": " + sg.getWeeks());
                        });
                        IO.println();
                    });
        } catch (Exception e) {
            IO.println(e.getMessage());
        }
    }

}
