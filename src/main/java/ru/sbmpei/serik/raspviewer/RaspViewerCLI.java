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

    private final String ADD_GROUP_CMD = "add";
    private final String WEEK_CMD = "week";

    private final Service service;

    private boolean quit;

    public RaspViewerCLI(Service service) {
        this.service = service;
    }

    private String currentGroup = null;

    @Override
    public void run() {
        while (!quit) {
            String prompt = StringUtils.isBlank(currentGroup) ? "RaspViewer_> " : "RaspViewer_" + currentGroup + "_> ";
            String cmd = IO.readln(prompt);
            switch (cmd) {
                case QUIT_CMD -> {
                    quit = true;
                }
                case HELP_CMD -> {
                    showMenu();
                }
                case GROUPS_CMD -> {
                    showGroups();
                }
                case SELECT_CMD -> {
                    String groupName = IO.readln("Group name: ");
                    selectGroup(groupName);
                }
                case SHOW_CMD -> {
                    showDay();
                }
                case ADD_GROUP_CMD -> {
                    addGroups();
                }
                case WEEK_CMD -> {
                    showCurrentWeek();
                }
                default -> {
                    IO.println("Not correct command");
                }
            }

        }

    }

    private void showMenu() {
        IO.println(GROUPS_CMD + " - Show groups list");
        IO.println(SELECT_CMD + " - Select group");
        IO.println(SHOW_CMD + " - Show stud day for date");
        IO.println(ADD_GROUP_CMD + " - Add a new groups from the files");
        IO.println(WEEK_CMD + " - Show the week for a current time");
        IO.println(QUIT_CMD + " - Quit");
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

    private void addGroups() {
        String input = IO.readln("File name(s): ");
        List.of(input.split(StringUtils.SPACE)).forEach(service::parseGroupFromFile);
    }

    private void showCurrentWeek() {
        System.out.printf("Идёт %d неделя\n", service.currentWeek(LocalDate.now()));
    }

}
