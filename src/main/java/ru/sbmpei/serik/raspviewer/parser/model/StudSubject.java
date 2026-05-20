package ru.sbmpei.serik.raspviewer.parser.model;

import java.util.List;

/**
 *
 * @author SLakeev
 */
public record StudSubject(String title, List<SubjectInfo> info) {

    public static StudSubject EMPTY = new StudSubject("EMPTY", List.of());

    public static record SubjectInfo(String value, Type type) {

        public static enum Type {
            AUDIENCE, ANOTHER_TIME, CLASSES
        }
    }
}
