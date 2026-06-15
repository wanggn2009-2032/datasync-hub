package com.datasync.repository;

import com.datasync.entity.DataSyncTaskExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataSyncTaskExecutionRepository extends JpaRepository<DataSyncTaskExecution, Long> {
    List<DataSyncTaskExecution> findByTaskIdOrderByCreateTimeDesc(Long taskId);
    Page<DataSyncTaskExecution> findByTaskId(Long taskId, Pageable pageable);
}
