package ru.sbmpei.serik.raspviewer.parser.model;

import java.util.List;

/**
 *
 * @author SLakeev
 */
public record StudSubject(String title, List<String> info) {

    public static StudSubject EMPTY = new StudSubject("EMPTY", List.of());
}
