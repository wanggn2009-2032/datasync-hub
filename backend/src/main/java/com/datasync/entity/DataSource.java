package com.datasync.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "datasync_datasource")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sourceCode;

    @Column(nullable = false)
    private String sourceName;

    @Column(nullable = false)
    private String sourceType;

    private String host;

    private Integer port;

    private String database;

    private String username;

    @Column(columnDefinition = "TEXT")
    private String password;

    @Column(columnDefinition = "TEXT")
    private String config;

    private Integer enabled;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date createTime;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;
}
