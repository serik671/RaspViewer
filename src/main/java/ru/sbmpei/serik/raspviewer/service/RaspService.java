package ru.sbmpei.serik.raspviewer.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.sbmpei.serik.raspviewer.mapper.RaspModelMapper;
import ru.sbmpei.serik.raspviewer.model.Group;
import ru.sbmpei.serik.raspviewer.model.Subject;
import ru.sbmpei.serik.raspviewer.parser.RaspParser;
import ru.sbmpei.serik.raspviewer.parser.model.StudGroup;

/**
 *
 * @author SLakeev
 */
public class RaspService implements Service {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String YES = "y";

    private final List<Group> groups = new ArrayList<>();
    private final LocalDate semesterStartDate;

    public RaspService(LocalDate semesterStartDate) {
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

    @Override
    public int currentWeek(LocalDate date) {
        return (int) ChronoUnit.WEEKS.between(semesterStartDate, date) + 1;
    }

    @Override
    public void parseGroupFromFile(String fileName) {
        Map<String, StudGroup> result;
        try {
            LOGGER.info("Промежуточная обработака файла '{}'...", fileName);
            result = new RaspParser(fileName).parse();
            LOGGER.info("Промежуточная модель успешно создана.");
        } catch (Exception e) {
            LOGGER.warn("Промежуточная обработака файла '{}' завершилась с ошибкой: {}", fileName, e);
            return;
        }
        LOGGER.info("Обработка промежуточной модели...");
        mergeGroups(RaspModelMapper.transformRaspModel(result));
        LOGGER.info("Рабочая модель успешно создана!");
    }

    private void mergeGroups(List<Group> groups) {
        LOGGER.info("Слияние списка групп с новым списком групп...");
        LOGGER.debug("Текущий список групп: {}", this.groups);
        LOGGER.debug("Входящий список групп: {}", groups);
        if (this.groups.isEmpty()) {
            this.groups.addAll(groups);
        } else {
            final List<Group> existsGroups = new ArrayList<>();
            groups.forEach(newGroup -> {
                if (groups.contains(newGroup)) {
                    existsGroups.add(newGroup);
                } else {
                    this.groups.add(newGroup);
                }
            });
            if (!existsGroups.isEmpty() && saveNewGroups(existsGroups.toString())) {
                this.groups.addAll(existsGroups);
                LOGGER.debug("Группы {} были перезаписаны", existsGroups);
            }
        }

        LOGGER.info("Слияние успешно выполнено!");
    }

    private boolean saveNewGroups(String groups) {
        String resp = IO.readln("Обнаружено совпадение имён групп: " + groups
                + "\nПерезаписать на новые группы? (y/N): ");
        return YES.equals(resp.toLowerCase());
    }

}
