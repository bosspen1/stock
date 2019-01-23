package vip.linhs.stock.scheduled;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import vip.linhs.stock.model.po.ExecuteInfo;
import vip.linhs.stock.model.po.Task;
import vip.linhs.stock.service.HolidayCalendarService;
import vip.linhs.stock.service.TaskService;

@Component
public class ScheduledTasks {

    private final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    @Autowired
    private HolidayCalendarService holidayCalendarService;

    @Autowired
    private TaskService taskService;

    /**
     * begin of year
     */
    @Scheduled(cron = "0 0 0 1 1 ?")
    public void runBeginOfYear() {
        try {
            List<ExecuteInfo> list = taskService.getPendingTaskListById(Task.BeginOfYear.getId());
            executeTask(list);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * end of day
     */
    @Scheduled(cron = "0 0 23 31 12 ?")
    public void runEndOfYear() {
        try {
            List<ExecuteInfo> list = taskService.getPendingTaskListById(Task.EndOfYear.getId());
            executeTask(list);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * begin of day
     */
    @Scheduled(cron = "0 0 6 ? * MON-FRI")
    public void runBeginOfDay() {
        boolean isHoliday = holidayCalendarService.isHoliday(new Date());
        if (isHoliday) {
            return;
        }
        try {
            List<ExecuteInfo> list = taskService.getPendingTaskListById(Task.BeginOfDay.getId());
            executeTask(list);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * end of day
     */
    @Scheduled(cron = "0 0 22 ? * MON-FRI")
    public void runEndOfDay() {
        boolean isHoliday = holidayCalendarService.isHoliday(new Date());
        if (isHoliday) {
            return;
        }
        try {
            List<ExecuteInfo> list = taskService.getPendingTaskListById(Task.EndOfDay.getId());
            executeTask(list);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * update of stock
     */
    @Scheduled(cron = "0 30 7,8 ? * MON-FRI")
    public void runUpdateOfStock() {
        boolean isHoliday = holidayCalendarService.isHoliday(new Date());
        if (isHoliday) {
            return;
        }
        try {
            List<ExecuteInfo> list = taskService.getPendingTaskListById(Task.UpdateOfStock.getId(),
                    Task.UpdateOfStockState.getId());
            executeTask(list);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * update of daily index
     */
    @Scheduled(cron = "0 0 17,18 ? * MON-FRI")
    public void runUpdateOfDailyIndex() {
        boolean isHoliday = holidayCalendarService.isHoliday(new Date());
        if (isHoliday) {
            return;
        }
        try {
            List<ExecuteInfo> list = taskService.getPendingTaskListById(Task.UpdateOfDailyIndex.getId());
            executeTask(list);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * ticker
     */
    @Scheduled(cron = "0 * 9,10,11,13,14 ? * MON-FRI")
    public void runTicker() {
        boolean isHoliday = holidayCalendarService.isHoliday(new Date());
        if (isHoliday) {
            return;
        }
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        if (hour == 9 && minute < 30 || hour == 11 && minute > 30) {
            return;
        }
        taskService.runTicker();
    }

    private void executeTask(List<ExecuteInfo> list) {
        for (ExecuteInfo executeInfo : list) {
            taskService.executeTask(executeInfo);
        }
    }

}
