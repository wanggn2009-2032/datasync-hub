package com.datasync.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExecutionMessage {
    private Long executionId;
    private Long taskId;
    private String taskCode;
    private Integer status;
    private String message;
    private Integer progress; // 0-100
}
