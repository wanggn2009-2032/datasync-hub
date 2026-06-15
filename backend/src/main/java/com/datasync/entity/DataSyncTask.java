package com.datasync.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "datasync_task")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataSyncTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String taskCode;

    @Column(nullable = false)
    private String taskName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "LONGTEXT")
    private String dagConfig;

    @Column(nullable = false)
    private String sourceType;

    @Column(nullable = false)
    private String targetType;

    @Column(columnDefinition = "TEXT")
    private String sourceConfig;

    @Column(columnDefinition = "TEXT")
    private String targetConfig;

    private String cronExpression;

    @Column(nullable = false)
    private Integer status;

    @Column(nullable = false)
    private Integer enabled;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastExecuteTime;

    @Temporal(TemporalType.TIMESTAMP)
    private Date nextExecuteTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date createTime;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    private String createdBy;

    private String updatedBy;
}
