package ru.sbmpei.serik.raspviewer.parser.model;

import java.time.DayOfWeek;
import java.util.Map;

/**
 *
 * @author SLakeev
 */
public record StudGroup(Map<DayOfWeek, WorkDay> days) {

}
