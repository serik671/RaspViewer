package ru.sbmpei.serik.raspviewer.util;

import ru.sbmpei.serik.raspviewer.model.TimeRange;
import ru.sbmpei.serik.raspviewer.parser.exception.TimeRangeCheckException;

/**
 *
 * @author SLakeev
 */
public class TimeRangeChecker implements Checker {

    @Override
    public void check(Object o) throws TimeRangeCheckException {
        try {
            TimeRange.of(String.valueOf(o));
        } catch (IllegalArgumentException e) {
            throw new TimeRangeCheckException(e.getMessage());
        }
    }

}
