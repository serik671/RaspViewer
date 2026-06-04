package ru.sbmpei.serik.raspviewer.controller;

import io.javalin.http.Context;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import ru.sbmpei.serik.raspviewer.model.Group;
import ru.sbmpei.serik.raspviewer.service.Service;
import ru.sbmpei.serik.raspviewer.view.DayView;
import ru.sbmpei.serik.raspviewer.view.SubjectView;

/**
 *
 * @author SLakeev
 */
public class RaspController {

    private static class Template {

        public static final String MAIN = "main.jte";
        public static final String SUBGROUP = "subgroup.jte";
        public static final String RASP_CONTENT = "rasp-content.jte";
    }

    private static class JTEParam {

        public static final String GROUPS = "groups";
        public static final String SUBGROUPS = "subgroups";
        public static final String WEEK = "week";
        public static final String DAYS = "days";
    }

    private static class Param {

        public static final String GROUP_NAME = "group";
        public static final String SUBGROUP_NAME = "subgroup";
        public static final String FROM_DATE = "fromDate";
        public static final String TO_DATE = "toDate";
    }

    private static final Map<DayOfWeek, String> dayOfWeek = Collections.unmodifiableMap(
            Map.of(
                    DayOfWeek.MONDAY, "понедельник",
                    DayOfWeek.TUESDAY, "вторник",
                    DayOfWeek.WEDNESDAY, "среда",
                    DayOfWeek.THURSDAY, "четверг",
                    DayOfWeek.FRIDAY, "пятница",
                    DayOfWeek.SATURDAY, "суббота",
                    DayOfWeek.SUNDAY, "воскресение"
            )
    );

    private final Service service;

    private RaspController(Service service) {
        this.service = service;
    }

    public static RaspController of(Service service) {
        return new RaspController(service);
    }

    public void main(Context ctx) {
        List<Group> groupList = service.groupList();
        List<String> groups = groupList.stream().map(Group::getName).collect(Collectors.toList());
        ctx.render(Template.MAIN, Map.of(
                JTEParam.GROUPS, groups,
                JTEParam.WEEK, service.currentWeek(LocalDate.now())
        ));
    }

    public void subgroup(Context ctx) {
        String groupName = ctx.formParam(Param.GROUP_NAME);
        ctx.render(Template.SUBGROUP, Map.of(JTEParam.SUBGROUPS, service.subgroupList(groupName)));
    }

    public void raspContentForToday(Context ctx) {
        String groupName = ctx.formParam(Param.GROUP_NAME);
        String subgroupName = ctx.formParam(Param.SUBGROUP_NAME);
        String date = LocalDate.now().toString();

        raspContent(ctx, date, date, groupName, subgroupName);
    }

    public void raspContentForTomorrow(Context ctx) {
        String groupName = ctx.formParam(Param.GROUP_NAME);
        String subgroupName = ctx.formParam(Param.SUBGROUP_NAME);
        String date = LocalDate.now().plusDays(1).toString();

        raspContent(ctx, date, date, groupName, subgroupName);
    }

    public void raspContentForThreeDays(Context ctx) {
        String groupName = ctx.formParam(Param.GROUP_NAME);
        String subgroupName = ctx.formParam(Param.SUBGROUP_NAME);
        String fromDate = LocalDate.now().toString();
        String toDate = LocalDate.now().plusDays(3).toString();

        raspContent(ctx, fromDate, toDate, groupName, subgroupName);
    }

    public void raspContentForCurrentWeek(Context ctx) {
        String groupName = ctx.formParam(Param.GROUP_NAME);
        String subgroupName = ctx.formParam(Param.SUBGROUP_NAME);

        LocalDate today = LocalDate.now();

        String fromDate = today
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .toString();
        String toDate = today
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))
                .toString();

        raspContent(ctx, fromDate, toDate, groupName, subgroupName);
    }

    public void raspContentForNextWeek(Context ctx) {
        String groupName = ctx.formParam(Param.GROUP_NAME);
        String subgroupName = ctx.formParam(Param.SUBGROUP_NAME);

        LocalDate dayOnNextWeek = LocalDate.now().plusWeeks(1);

        String fromDate = dayOnNextWeek
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .toString();
        String toDate = dayOnNextWeek
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))
                .toString();

        raspContent(ctx, fromDate, toDate, groupName, subgroupName);
    }

    public void raspContent(Context ctx) {
        String fromDate = ctx.formParam(Param.FROM_DATE);
        String toDate = ctx.formParam(Param.TO_DATE);
        String groupName = ctx.formParam(Param.GROUP_NAME);
        String subgroupName = ctx.formParam(Param.SUBGROUP_NAME);

        raspContent(ctx, fromDate, toDate, groupName, subgroupName);

    }

    private void raspContent(Context ctx, String fromDate, String toDate, String groupName, String subgroupName) {
        if (StringUtils.isBlank(fromDate)) {
            ctx.result("fromDate is null");
            return;
        }
        if (StringUtils.isBlank(toDate)) {
            ctx.result("toDate is null");
            return;
        }

        try {
            LocalDate startDate = LocalDate.parse(fromDate);
            LocalDate endDate = LocalDate.parse(toDate);
            if (startDate.isAfter(endDate)) {
                throw new Exception("Start date is after the end date");
            }
            List<DayView> days = startDate.datesUntil(endDate.plusDays(1)).map(day -> {
                List<SubjectView> subjects = service
                        .subjectsOfGroupForDay(day, groupName, subgroupName)
                        .stream().map(it -> new SubjectView(it.getTimeString(), it.getTitle(), it.getAudience())).toList();
                return new DayView(dayOfWeek.get(day.getDayOfWeek()).toUpperCase(), day.toString(), subjects, service.currentWeek(day));
            }).toList();

            ctx.render(Template.RASP_CONTENT, Map.of(JTEParam.DAYS, days));
        } catch (Exception e) {
            ctx.result(e.getMessage());
        }
    }
}
