package ru.sbmpei.serik.raspviewer.cron.job;

import java.time.LocalDate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import ru.sbmpei.serik.raspviewer.controller.ClientController;
import ru.sbmpei.serik.raspviewer.service.Service;

/**
 *
 * @author SLakeev
 */
public class CurrentWeekJob implements Job {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String CLIENT_CTRL = "currentWeekClientController";
    public static final String SERVICE = "currentWeekService";

    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        try {
            ClientController clientCtrl = (ClientController) jec.getScheduler().getContext().get(CLIENT_CTRL);
            Service service = (Service) jec.getScheduler().getContext().get(SERVICE);
            String currentWeek = String.valueOf(service.currentWeek(LocalDate.now()));
            clientCtrl.sendMessage(currentWeek, "current-week");
        } catch (SchedulerException e) {
            LOGGER.warn(e);
        }
    }

}
