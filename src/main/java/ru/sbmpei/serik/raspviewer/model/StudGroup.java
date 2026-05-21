package ru.sbmpei.serik.raspviewer.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author SLakeev
 */
public class StudGroup implements Group {

    private final String name;
    private final int courseNumber;
    private final List<StudSubject> subjects = new ArrayList<>();

    public StudGroup(String name, int courseNumber) {
        this.name = name;
        this.courseNumber = courseNumber;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getCourseNumber() {
        return courseNumber;
    }

    @Override
    public List<StudSubject> getSubjects() {
        return subjects;
    }

}
