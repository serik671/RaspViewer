package ru.sbmpei.serik.raspviewer.view;

import java.util.List;

/**
 *
 * @author SLakeev
 */
public record DayView(String name, String date, List<SubjectView> subjects, int week) {

}
