package com.datasync.executor;

import com.datasync.entity.DataSyncTask;
import com.datasync.entity.DataSyncTaskExecution;
import com.datasync.repository.DataSyncTaskExecutionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class RealMySQLExecutorAdapter implements ExecutorAdapter {

    private static final Logger logger = LoggerFactory.getLogger(RealMySQLExecutorAdapter.class);

    @Autowired
    private DataSyncTaskExecutionRepository executionRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(String sourceType, String targetType) {
        return "mysql".equalsIgnoreCase(sourceType) && "mysql".equalsIgnoreCase(targetType);
    }

    @Override
    public void execute(DataSyncTask task, DataSyncTaskExecution exec) throws Exception {
        // Expect sourceConfig and targetConfig as JSON strings with connection info
        if (task.getSourceConfig() == null || task.getTargetConfig() == null) {
            throw new IllegalArgumentException("sourceConfig or targetConfig is empty");
        }

        JsonNode src = objectMapper.readTree(task.getSourceConfig());
        JsonNode tgt = objectMapper.readTree(task.getTargetConfig());

        String srcUrl = src.path("url").asText();
        String srcUser = src.path("username").asText();
        String srcPass = src.path("password").asText();
        String srcQuery = src.path("query").asText();
        int fetchSize = src.path("fetchSize").asInt(500);

        String tgtUrl = tgt.path("url").asText();
        String tgtUser = tgt.path("username").asText();
        String tgtPass = tgt.path("password").asText();
        String tgtTable = tgt.path("table").asText();
        int batchSize = tgt.path("batchSize").asInt(500);

        if (srcUrl.isEmpty() || tgtUrl.isEmpty() || srcQuery.isEmpty() || tgtTable.isEmpty()) {
            throw new IllegalArgumentException("Invalid source/target configuration; url/query/table required");
        }

        exec.setExecutionLog((exec.getExecutionLog() == null ? "" : exec.getExecutionLog()) + "\nRealMySQLExecutor: connecting to source/target");
        executionRepository.save(exec);
        sendProgress(exec, 0, "Connecting");

        Connection srcConn = null;
        Connection tgtConn = null;
        PreparedStatement srcStmt = null;
        ResultSet rs = null;
        PreparedStatement insertStmt = null;

        try {
            srcConn = DriverManager.getConnection(srcUrl, srcUser, srcPass);
            tgtConn = DriverManager.getConnection(tgtUrl, tgtUser, tgtPass);
            tgtConn.setAutoCommit(false);

            srcStmt = srcConn.prepareStatement(srcQuery, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            srcStmt.setFetchSize(fetchSize);
            rs = srcStmt.executeQuery();

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            List<String> cols = new ArrayList<>(colCount);
            for (int i = 1; i <= colCount; i++) {
                cols.add(meta.getColumnName(i));
            }

            StringBuilder placeholders = new StringBuilder();
            for (int i = 0; i < colCount; i++) {
                placeholders.append("?");
                if (i < colCount - 1) placeholders.append(",");
            }
            String insertSql = String.format("INSERT INTO %s (%s) VALUES (%s)", tgtTable, String.join(",", cols), placeholders.toString());
            insertStmt = tgtConn.prepareStatement(insertSql);

            int count = 0;
            long total = 0;
            int progress = 0;
            while (rs.next()) {
                for (int i = 1; i <= colCount; i++) {
                    Object v = rs.getObject(i);
                    insertStmt.setObject(i, v);
                }
                insertStmt.addBatch();
                count++;
                total++;
                if (count >= batchSize) {
                    insertStmt.executeBatch();
                    tgtConn.commit();
                    count = 0;
                    exec.setExecutionLog(exec.getExecutionLog() + "\nInserted batch, total=" + total);
                    exec.setSuccessCount(total);
                    executionRepository.save(exec);
                    progress = (int) Math.min(99, total % Integer.MAX_VALUE);
                    sendProgress(exec, progress, "Inserted " + total + " rows");
                }
            }

            if (count > 0) {
                insertStmt.executeBatch();
                tgtConn.commit();
                exec.setExecutionLog(exec.getExecutionLog() + "\nInserted final batch, total=" + total);
                exec.setSuccessCount(total);
                executionRepository.save(exec);
                sendProgress(exec, 100, "Inserted total " + total + " rows");
            }

        } finally {
            if (rs != null) try { rs.close(); } catch (Exception ignore) {}
            if (srcStmt != null) try { srcStmt.close(); } catch (Exception ignore) {}
            if (insertStmt != null) try { insertStmt.close(); } catch (Exception ignore) {}
            if (srcConn != null) try { srcConn.close(); } catch (Exception ignore) {}
            if (tgtConn != null) try { tgtConn.close(); } catch (Exception ignore) {}
        }

        exec.setExecutionLog(exec.getExecutionLog() + "\nRealMySQLExecutor: finished");
        exec.setStatus(2);
        executionRepository.save(exec);
        sendProgress(exec, 100, "Finished: copied rows");
    }

    private void sendProgress(DataSyncTaskExecution exec, int progress, String message) {
        try {
            messagingTemplate.convertAndSend("/topic/task-progress", new com.datasync.dto.ExecutionMessage(exec.getId(), exec.getTaskId(), exec.getTaskCode(), exec.getStatus(), message, progress));
        } catch (Exception e) {
            logger.warn("Failed to send websocket message", e);
        }
    }
}
