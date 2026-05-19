package ru.sbmpei.serik.raspviewer.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author SLakeev
 */
public class StudGroup {

    private final String name;
    private final int courseNumber;
    private final List<StudSubject> subjects = new ArrayList<>();

    public StudGroup(String name, int courseNumber) {
        this.name = name;
        this.courseNumber = courseNumber;
    }

    public String getName() {
        return name;
    }

    public int getCourseNumber() {
        return courseNumber;
    }

    public List<StudSubject> getSubjects() {
        return subjects;
    }

}
