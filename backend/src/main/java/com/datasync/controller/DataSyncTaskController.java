package com.datasync.controller;

import com.datasync.entity.DataSyncTask;
import com.datasync.entity.DataSyncTaskExecution;
import com.datasync.service.DataSyncTaskService;
import com.datasync.executor.TaskExecutorService;
import com.datasync.scheduler.SchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class DataSyncTaskController {

    @Autowired
    private DataSyncTaskService taskService;

    @Autowired
    private TaskExecutorService executorService;

    @Autowired
    private SchedulerService schedulerService;

    @GetMapping
    public ResponseEntity<List<DataSyncTask>> list() {
        return ResponseEntity.ok(taskService.listAll());
    }

    @PostMapping
    public ResponseEntity<DataSyncTask> create(@RequestBody DataSyncTask task) {
        DataSyncTask saved = taskService.save(task);
        // schedule if enabled
        schedulerService.reschedule(saved);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DataSyncTask> get(@PathVariable Long id) {
        return taskService.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        schedulerService.cancelTask(id);
        taskService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/executions")
    public ResponseEntity<List<DataSyncTaskExecution>> executions(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getExecutions(id));
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<String> executeNow(@PathVariable Long id) {
        executorService.executeTaskAsync(id);
        return ResponseEntity.ok("Triggered");
    }
}
