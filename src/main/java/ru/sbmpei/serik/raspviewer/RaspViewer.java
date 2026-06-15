package ru.sbmpei.serik.raspviewer;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinJte;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.SchedulerException;
import ru.sbmpei.serik.raspviewer.controller.ClientController;
import ru.sbmpei.serik.raspviewer.controller.RaspController;
import ru.sbmpei.serik.raspviewer.cron.CurrentTimeScheduler;
import ru.sbmpei.serik.raspviewer.cron.CurrentWeekScheduler;
import ru.sbmpei.serik.raspviewer.service.RaspService;

/**
 *
 * @author SLakeev
 */
public class RaspViewer {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final List<String> fileNames = new ArrayList<>();
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

        LOGGER.debug("Программа запущена с параметрами: Файлы({}), Дата начала семестра({})",
                fileNames, beginDate);

        RaspService raspService = new RaspService(beginDate);
        fileNames.forEach(raspService::parseGroupFromFile);
        System.out.printf("Идёт %d неделя\n", raspService.currentWeek(LocalDate.now()));
        new Thread(new RaspViewerCLI(raspService)).start();

        RaspController raspCtrl = RaspController.of(raspService);
        ClientController clientCtrl = new ClientController();

        Javalin.create(conf -> {
            conf.fileRenderer(new JavalinJte(JavalinJte.Companion.directoryTemplateEngine()));
            conf.staticFiles.add("public", Location.CLASSPATH);
            conf.routes.get("/", raspCtrl::main);
            conf.routes.post("/subgroup", raspCtrl::subgroup);
            conf.routes.post("/raspForToday", raspCtrl::raspContentForToday);
            conf.routes.post("/raspForTomorrow", raspCtrl::raspContentForTomorrow);
            conf.routes.post("/raspForThreeDays", raspCtrl::raspContentForThreeDays);
            conf.routes.post("/raspForCurrentWeek", raspCtrl::raspContentForCurrentWeek);
            conf.routes.post("/raspForNextWeek", raspCtrl::raspContentForNextWeek);
            conf.routes.post("/rasp", raspCtrl::raspContent);
            conf.routes.sse("/current-time", clientCtrl::addClient);
        }).start(80);

        try {
            new CurrentTimeScheduler(clientCtrl).start();
            new CurrentWeekScheduler(raspService, clientCtrl).start();
        } catch (SchedulerException e) {
            LOGGER.warn(e);
        }

    }

    private static void init(String[] args) throws DateTimeParseException {
        LOGGER.info("Инициализация программы...");
        LOGGER.info("Формат входных параметров: [имя файлов, дата начала семестра]");
        LOGGER.debug("Входные параметры: {}", List.of(args));
        if (args.length > 0) {
            for (int i = 0; i < args.length - 1; i++) {
                fileNames.add(args[i]);
            }
            if (args.length > 1) {
                beginDate = LocalDate.parse(args[args.length - 1]);
            } else {
                beginDate = LocalDate.parse(IO.readln("Введите дату начала семестра(ГГГГ-ММ-ДД): "));
                LOGGER.debug("Пользователь ввёл дату начала семестра: {}", beginDate);
            }
        } else {
            fileNames.add(IO.readln("Укажие файл расписания: "));
            beginDate = LocalDate.parse(IO.readln("Введите дату начала семестра(ГГГГ-ММ-ДД): "));
            LOGGER.debug("Пользователь ввёл имя файла с расписанием: {}", fileNames.getLast());
            LOGGER.debug("Пользователь ввёл дату начала семестра: {}", beginDate);
        }
        LOGGER.info("Программа инициализирована");
    }

}
