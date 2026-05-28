package ru.sbmpei.serik.raspviewer;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.sbmpei.serik.raspviewer.mapper.RaspModelMapper;
import ru.sbmpei.serik.raspviewer.model.Group;
import ru.sbmpei.serik.raspviewer.parser.RaspParser;
import ru.sbmpei.serik.raspviewer.parser.model.StudGroup;
import ru.sbmpei.serik.raspviewer.service.RaspService;

/**
 *
 * @author SLakeev
 */
public class RaspViewer {

    private static final Logger LOGGER = LogManager.getLogger();

    private static String fileName = null;
    private static LocalDate beginDate = null;

    public static void main(String[] args) {
        System.out.println("RaspViewer");

        try {
            init(args);
        } catch (DateTimeParseException e) {
            LOGGER.error("Введённое значение '" + e.getParsedString() + "' не соответствует дате в формате: ГГГГ-ММ-ДД");
            LOGGER.fatal("Система завершила работу: Неудалось инициализировать систему");
            System.exit(0);
        }

        LOGGER.debug("Программа запущена с параметрами: Файл({}), Дата начала семестра({})",
                fileName, beginDate);

        LOGGER.info("Промежуточная обработака файла...");
        Map<String, StudGroup> result = new RaspParser(fileName).parse();
        LOGGER.info("Промежуточная модель успешно создана.");

        LOGGER.info("Обработка промежуточной модели...");
        List<Group> groups = RaspModelMapper.transformRaspModel(result);
        LOGGER.info("Рабочая модель успешно создана!");

        RaspService raspService = new RaspService(groups, beginDate);
        System.out.printf("Идёт %d неделя\n", raspService.currentWeek(LocalDate.now()));
        new RaspViewerCLI(raspService).run();

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
            beginDate = LocalDate.parse(IO.readln("Введите дату начала семестра(ГГГГ-ММ-ДД): "));
        }
    }

}
