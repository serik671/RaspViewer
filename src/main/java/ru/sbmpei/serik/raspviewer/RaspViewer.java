package ru.sbmpei.serik.raspviewer;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import ru.sbmpei.serik.raspviewer.mapper.RaspModelMapper;
import ru.sbmpei.serik.raspviewer.parser.RaspParser;
import ru.sbmpei.serik.raspviewer.parser.model.StudGroup;
import ru.sbmpei.serik.raspviewer.service.RaspService;

/**
 *
 * @author SLakeev
 */
public class RaspViewer {

    private static String fileName = null;
    private static LocalDate beginDate = null;

    public static void main(String[] args) {
        System.out.println("RaspViewer");

        try {
            init(args);
        } catch (DateTimeParseException e) {
            IO.println("Введённое значение '" + e.getParsedString() + "' не соответствует дате в формате: ГГГГ-ММ-ДД");
            IO.println("Система завершила работу: Неудалось инициализировать систему");
            System.exit(0);
        }

        IO.println("Промежуточная обработака файла...");
        Map<String, StudGroup> result = new RaspParser(fileName).parse();
        IO.println("Промежуточная модель успешно создана.");

        IO.println("Обработка промежуточной модели...");
        List<ru.sbmpei.serik.raspviewer.model.StudGroup> groups = RaspModelMapper.transformRaspModel(result);
        IO.println("Рабочая модель успешно создана!");

        System.out.printf("Идёт %d неделя\n", currentWeek());

        new RaspViewerCLI(new RaspService(groups, beginDate)).run();

    }

    private static void init(String[] args) throws DateTimeParseException {
        if (args.length > 0) {
            fileName = args[0];
            if (args.length > 1) {
                beginDate = LocalDate.parse(args[1]);
            } else {
                beginDate = LocalDate.parse(IO.readln("Введите дату начала семестра(ГГГГ-ММ-ДД): "));
            }
        } else {
            fileName = IO.readln("Укажие файл расписания: ");
            beginDate = LocalDate.parse(IO.readln("Введите дату начала семестра(гггг-мм-дд): "));
        }
    }

    private static int currentWeek() {
        return (int) ChronoUnit.WEEKS.between(beginDate, LocalDate.now()) + 1;
    }

}
