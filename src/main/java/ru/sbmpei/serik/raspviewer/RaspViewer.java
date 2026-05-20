package ru.sbmpei.serik.raspviewer;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Map;
import ru.sbmpei.serik.raspviewer.mapper.RaspModelMapper;
import ru.sbmpei.serik.raspviewer.parser.RaspParser;
import ru.sbmpei.serik.raspviewer.parser.model.StudGroup;

/**
 *
 * @author SLakeev
 */
public class RaspViewer {

    private static String fileName = null;
    private static LocalDate beginDate = null;

    public static void main(String[] args) {
        System.out.println("RaspViewer");

        if (args.length > 0) {
            fileName = args[0];
            if (args.length > 1) {
                beginDate = LocalDate.parse(args[1]);
            } else {
                beginDate = LocalDate.parse(IO.readln("Введите дату начала семестра(гггг-мм-дд): "));
            }
        } else {
            fileName = IO.readln("Укажие файл расписания: ");
            beginDate = LocalDate.parse(IO.readln("Введите дату начала семестра(гггг-мм-дд): "));
        }

        IO.println("Промежуточная обработака файла...");
        Map<String, StudGroup> result = new RaspParser(fileName).parse();
        IO.println("Промежуточная модель успешно создана.");

        IO.println("Обработка промежуточной модели...");
        List<ru.sbmpei.serik.raspviewer.model.StudGroup> groups = RaspModelMapper.transformRaspModel(result);
        IO.println("Рабочая модель успешно создана!");

        System.out.printf("Идёт %d неделя\n", currentWeek());

        new RaspViewerCLI(groups, beginDate).run();

        // Show result
//        groups.forEach(sg -> {
//            System.out.printf("%s %d курс\n", sg.getName(), sg.getCourseNumber());
//            sg.getSubjects().stream().sorted().forEach(subject -> {
//                System.out.printf("%s | %s | %s | %s | %s | %s | %s | %s\n", subject.getDay(), subject.getTimeString(),
//                        subject.getTitle(), subject.getTeachers(), subject.getType(),
//                        subject.getAudience(), subject.getWeeks(), subject.getSubgroup());
//            });
//        });
    }

    private static int currentWeek() {
        return (int) ChronoUnit.WEEKS.between(beginDate, LocalDate.now()) + 1;
    }

}
