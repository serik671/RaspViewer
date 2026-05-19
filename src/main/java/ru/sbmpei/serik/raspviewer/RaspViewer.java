package ru.sbmpei.serik.raspviewer;

import java.time.DayOfWeek;
import java.util.Map;
import ru.sbmpei.serik.raspviewer.parser.RaspParser;
import ru.sbmpei.serik.raspviewer.parser.model.StudGroup;

/**
 *
 * @author SLakeev
 */
public class RaspViewer {

    public static void main(String[] args) {
        System.out.println("RaspViewer");
        Map<String, StudGroup> result = new RaspParser("rasp.xls").parse();
//        result.forEach((key, value) -> {
//            IO.println(key);
//            IO.println(value);
//        });
        IO.println("Э - 22 " + DayOfWeek.TUESDAY);
        result.get("Э - 22").days().get(DayOfWeek.TUESDAY).workSubjects().forEach((k, s) -> {
            IO.println(k);
            IO.println(s);
            IO.println();
        });

        IO.println("ЭМ - 22 " + DayOfWeek.TUESDAY);
        result.get("ЭМ - 22").days().get(DayOfWeek.TUESDAY).workSubjects().forEach((k, s) -> {
            IO.println(k);
            IO.println(s);
            IO.println();
        });
        IO.println("ЭМ - 22 " + DayOfWeek.MONDAY);
        result.get("ЭМ - 22").days().get(DayOfWeek.MONDAY).workSubjects().forEach((k, s) -> {
            IO.println(k);
            IO.println(s);
            IO.println();
        });

        IO.println("РТ - 22 " + DayOfWeek.MONDAY);
        result.get("РТ - 22").days().get(DayOfWeek.MONDAY).workSubjects().forEach((k, s) -> {
            IO.println(k);
            IO.println(s);
            IO.println();
        });
    }
}
