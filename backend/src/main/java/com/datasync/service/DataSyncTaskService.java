package com.datasync.service;

import com.datasync.entity.DataSyncTask;
import com.datasync.entity.DataSyncTaskExecution;
import com.datasync.repository.DataSyncTaskExecutionRepository;
import com.datasync.repository.DataSyncTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class DataSyncTaskService {

    @Autowired
    private DataSyncTaskRepository taskRepository;

    @Autowired
    private DataSyncTaskExecutionRepository executionRepository;

    public List<DataSyncTask> listAll() {
        return taskRepository.findAll();
    }

    public Optional<DataSyncTask> findById(Long id) {
        return taskRepository.findById(id);
    }

    public DataSyncTask save(DataSyncTask task) {
        Date now = new Date();
        if (task.getId() == null) {
            task.setCreateTime(now);
            task.setUpdateTime(now);
        } else {
            task.setUpdateTime(now);
        }
        return taskRepository.save(task);
    }

    @Transactional
    public void deleteById(Long id) {
        taskRepository.deleteById(id);
    }

    public DataSyncTaskExecution logExecution(DataSyncTaskExecution exec) {
        exec.setCreateTime(new Date());
        return executionRepository.save(exec);
    }

    public List<DataSyncTaskExecution> getExecutions(Long taskId) {
        return executionRepository.findByTaskIdOrderByCreateTimeDesc(taskId);
    }

    public Page<DataSyncTaskExecution> getExecutionsPaged(Long taskId, int page, int size) {
        return executionRepository.findByTaskId(taskId, PageRequest.of(page, size));
    }
}
