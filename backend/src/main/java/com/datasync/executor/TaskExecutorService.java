package com.datasync.executor;

import com.datasync.dto.ExecutionMessage;
import com.datasync.entity.DataSyncTask;
import com.datasync.entity.DataSyncTaskExecution;
import com.datasync.repository.DataSyncTaskExecutionRepository;
import com.datasync.repository.DataSyncTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class TaskExecutorService {

    private static final Logger logger = LoggerFactory.getLogger(TaskExecutorService.class);

    @Autowired
    private DataSyncTaskRepository taskRepository;

    @Autowired
    private DataSyncTaskExecutionRepository executionRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Async("taskExecutor")
    public void executeTaskAsync(Long taskId) {
        Optional<DataSyncTask> opt = taskRepository.findById(taskId);
        if (!opt.isPresent()) {
            logger.warn("Task not found: {}", taskId);
            return;
        }
        DataSyncTask task = opt.get();

        DataSyncTaskExecution exec = DataSyncTaskExecution.builder()
                .taskId(task.getId())
                .taskCode(task.getTaskCode())
                .status(1) // running
                .startTime(System.currentTimeMillis())
                .successCount(0L)
                .failureCount(0L)
                .executionLog("Started")
                .build();
        exec = executionRepository.save(exec);

        sendProgress(exec, 0, "Started");

        try {
            // Simulate execution steps and progress
            for (int i = 1; i <= 5; i++) {
                Thread.sleep(800);
                int progress = i * 20;
                exec.setExecutionLog(exec.getExecutionLog() + "\nStep " + i + " completed");
                executionRepository.save(exec);
                sendProgress(exec, progress, "Step " + i + " completed");
            }

            exec.setEndTime(System.currentTimeMillis());
            exec.setDuration(exec.getEndTime() - exec.getStartTime());
            exec.setStatus(2); // success
            exec.setSuccessCount(100L);
            exec.setExecutionLog(exec.getExecutionLog() + "\nSuccess");
            executionRepository.save(exec);
            sendProgress(exec, 100, "Success");

        } catch (InterruptedException e) {
            logger.error("Execution interrupted", e);
            exec.setEndTime(System.currentTimeMillis());
            exec.setDuration(exec.getEndTime() - exec.getStartTime());
            exec.setStatus(3); // failed
            exec.setFailureCount(1L);
            exec.setErrorMessage(e.getMessage());
            exec.setExecutionLog(exec.getExecutionLog() + "\nFailed: " + e.getMessage());
            executionRepository.save(exec);
            sendProgress(exec, 100, "Failed");
        } catch (Exception ex) {
            logger.error("Execution error", ex);
            exec.setEndTime(System.currentTimeMillis());
            exec.setDuration(exec.getEndTime() - exec.getStartTime());
            exec.setStatus(3); // failed
            exec.setFailureCount(1L);
            exec.setErrorMessage(ex.getMessage());
            exec.setExecutionLog(exec.getExecutionLog() + "\nFailed: " + ex.getMessage());
            executionRepository.save(exec);
            sendProgress(exec, 100, "Failed: " + ex.getMessage());
        }
    }

    private void sendProgress(DataSyncTaskExecution exec, int progress, String message) {
        ExecutionMessage msg = new ExecutionMessage(exec.getId(), exec.getTaskId(), exec.getTaskCode(), exec.getStatus(), message, progress);
        try {
            messagingTemplate.convertAndSend("/topic/task-progress", msg);
        } catch (Exception e) {
            logger.warn("Failed to send websocket message", e);
        }
    }
}
