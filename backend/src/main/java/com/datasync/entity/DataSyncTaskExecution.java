package com.datasync.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "datasync_task_execution")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataSyncTaskExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long taskId;

    private String taskCode;

    private Integer status;

    private Long startTime;

    private Long endTime;

    private Long duration;

    private Long successCount;

    private Long failureCount;

    @Column(columnDefinition = "LONGTEXT")
    private String executionLog;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date createTime;
}
