package ru.sbmpei.serik.raspviewer.model;

import java.time.DayOfWeek;
import java.util.List;

/**
 *
 * @author SLakeev
 */
public interface Subject {

    public String getTitle();

    public DayOfWeek getDay();

    public String getTimeString();

    public String getAudience();

    public List<Integer> getWeeks();

    public List<String> getTeachers();

    public String getSubgroup();

    public boolean isNumerator();

    public boolean isDenominator();

    public boolean isEven();

    public boolean isOdd();

}
