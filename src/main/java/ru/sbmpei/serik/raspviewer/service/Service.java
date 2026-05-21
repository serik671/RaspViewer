package ru.sbmpei.serik.raspviewer.service;

import java.time.LocalDate;
import java.util.List;
import ru.sbmpei.serik.raspviewer.model.Group;
import ru.sbmpei.serik.raspviewer.model.Subject;

/**
 *
 * @author SLakeev
 */
public interface Service {

    public List<? extends Group> groupList();

    public List<Subject> subjectsOfGroupForDays(List<LocalDate> days, String groupName);

    public int currentWeek(LocalDate date);
}
