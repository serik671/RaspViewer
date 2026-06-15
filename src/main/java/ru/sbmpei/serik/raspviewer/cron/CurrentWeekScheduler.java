package ru.sbmpei.serik.raspviewer.cron;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import ru.sbmpei.serik.raspviewer.controller.ClientController;
import ru.sbmpei.serik.raspviewer.cron.job.CurrentWeekJob;
import ru.sbmpei.serik.raspviewer.service.Service;

/**
 *
 * @author SLakeev
 */
public class CurrentWeekScheduler {

    private final Scheduler scheduler;

    public CurrentWeekScheduler(Service service, ClientController clientCtrl) throws SchedulerException {

        JobDetail job = JobBuilder.newJob(CurrentWeekJob.class)
                .withIdentity("currentWeek")
                .build();

        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("currentWeek")
                .withSchedule(
                        CronScheduleBuilder.cronSchedule("0 0 0 ? * MON") // Каждый понедельник в полночь
                ).build();

        scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.getContext().put(CurrentWeekJob.CLIENT_CTRL, clientCtrl);
        scheduler.getContext().put(CurrentWeekJob.SERVICE, service);
        scheduler.scheduleJob(job, trigger);
    }

    public void start() throws SchedulerException {
        scheduler.start();
    }
}
