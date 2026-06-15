package ru.sbmpei.serik.raspviewer.cron;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import ru.sbmpei.serik.raspviewer.controller.ClientController;
import ru.sbmpei.serik.raspviewer.cron.job.CurrentTimeJob;

/**
 *
 * @author SLakeev
 */
public class CurrentTimeScheduler {

    private final Scheduler scheduler;

    public CurrentTimeScheduler(ClientController clientCtrl) throws SchedulerException {

        JobDetail job = JobBuilder.newJob(CurrentTimeJob.class)
                .withIdentity("currentTime")
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("currentTime")
                .withSchedule(
                        CronScheduleBuilder.cronSchedule("0/1 * * * * ?") // Каждую секунду
                ).build();

        scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.getContext().put(CurrentTimeJob.CLIENT_CTRL, clientCtrl);
        scheduler.scheduleJob(job, trigger);
    }

    public void start() throws SchedulerException {
        scheduler.start();
    }
}
