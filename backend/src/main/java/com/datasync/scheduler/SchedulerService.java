package com.datasync.scheduler;

import com.datasync.entity.DataSyncTask;
import com.datasync.service.DataSyncTaskService;
import com.datasync.executor.TaskExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
public class SchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);

    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    private DataSyncTaskService taskService;

    @Autowired
    private TaskExecutorService executorService;

    private Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        logger.info("Initializing SchedulerService: loading enabled tasks");
        taskService.listAll().stream().filter(t -> t.getEnabled() != null && t.getEnabled() == 1 && t.getCronExpression() != null)
                .forEach(this::scheduleTask);
    }

    public void scheduleTask(DataSyncTask task) {
        try {
            CronTrigger trigger = new CronTrigger(task.getCronExpression());
            ScheduledFuture<?> future = taskScheduler.schedule(() -> {
                logger.info("Scheduled trigger for task {}", task.getTaskCode());
                executorService.executeTaskAsync(task.getId());
            }, trigger);
            ScheduledFuture<?> old = scheduledTasks.put(task.getId(), future);
            if (old != null) {
                old.cancel(false);
            }
            logger.info("Scheduled task {} with cron {}", task.getTaskCode(), task.getCronExpression());
        } catch (Exception ex) {
            logger.error("Failed to schedule task {}: {}", task.getTaskCode(), ex.getMessage());
        }
    }

    public void cancelTask(Long taskId) {
        ScheduledFuture<?> future = scheduledTasks.remove(taskId);
        if (future != null) {
            future.cancel(false);
        }
    }

    public void reschedule(DataSyncTask task) {
        cancelTask(task.getId());
        if (task.getEnabled() != null && task.getEnabled() == 1 && task.getCronExpression() != null) {
            scheduleTask(task);
        }
    }
}
