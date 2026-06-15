package ru.sbmpei.serik.raspviewer.cron.job;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import ru.sbmpei.serik.raspviewer.controller.ClientController;

/**
 *
 * @author SLakeev
 */
public class CurrentTimeJob implements Job {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String CLIENT_CTRL = "currentTimeClientController";

    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        LocalDateTime currentTime = LocalDateTime.now();
        String currentTimeMsg = currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        try {
            ClientController clientCtrl = (ClientController) jec.getScheduler().getContext().get(CLIENT_CTRL);
            clientCtrl.sendMessage(currentTimeMsg, "current-time");
        } catch (SchedulerException e) {
            LOGGER.warn(e);
        }
    }

}
