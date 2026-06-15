package com.datasync.controller;

import com.datasync.entity.DataSyncTask;
import com.datasync.entity.DataSyncTaskExecution;
import com.datasync.service.DataSyncTaskService;
import com.datasync.executor.TaskExecutorService;
import com.datasync.scheduler.SchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @PutMapping("/{id}")
    public ResponseEntity<DataSyncTask> update(@PathVariable Long id, @RequestBody DataSyncTask task) {
        // ensure id consistency
        task.setId(id);
        DataSyncTask saved = taskService.save(task);
        schedulerService.reschedule(saved);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}/dag")
    public ResponseEntity<Map<String,Object>> saveDag(@PathVariable Long id, @RequestBody Map<String,Object> body) {
        DataSyncTask task = taskService.findById(id).orElse(null);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        Object dag = body.get("dagConfig");
        if (dag != null) {
            task.setDagConfig(dag.toString());
            taskService.save(task);
        }
        Map<String,Object> resp = new HashMap<>();
        resp.put("success", true);
        return ResponseEntity.ok(resp);
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
    public ResponseEntity<Object> executions(@PathVariable Long id,
                                             @RequestParam(required = false) Integer page,
                                             @RequestParam(required = false) Integer size) {
        if (page == null || size == null) {
            List<DataSyncTaskExecution> list = taskService.getExecutions(id);
            return ResponseEntity.ok(list);
        } else {
            Page<DataSyncTaskExecution> p = taskService.getExecutionsPaged(id, page, size);
            Map<String,Object> resp = new HashMap<>();
            resp.put("content", p.getContent());
            resp.put("page", p.getNumber());
            resp.put("size", p.getSize());
            resp.put("totalElements", p.getTotalElements());
            resp.put("totalPages", p.getTotalPages());
            return ResponseEntity.ok(resp);
        }
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<String> executeNow(@PathVariable Long id) {
        executorService.executeTaskAsync(id);
        return ResponseEntity.ok("Triggered");
    }
}
