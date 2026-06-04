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

    public List<Group> groupList();

    public List<Subject> subjectsOfGroupForDay(LocalDate day, String groupName);

    public List<Subject> subjectsOfGroupForDay(LocalDate day, String groupName, String subgroup);

    public int currentWeek(LocalDate date);

    public void parseGroupFromFile(String fileName);

    public List<String> subgroupList(String groupName);
}
