package ru.sbmpei.serik.raspviewer.model;

import java.util.List;

/**
 *
 * @author SLakeev
 */
public interface Group {

    public String getName();

    public int getCourseNumber();

    public List<? extends Subject> getSubjects();

}
