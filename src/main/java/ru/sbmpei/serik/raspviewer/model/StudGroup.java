package ru.sbmpei.serik.raspviewer.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StudGroup other = (StudGroup) obj;
        return Objects.equals(this.name, other.name);
    }

    @Override
    public String toString() {
        return name;
    }

}
