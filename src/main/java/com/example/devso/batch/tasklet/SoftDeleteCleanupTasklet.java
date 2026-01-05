package com.example.devso.batch.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SoftDeleteCleanupTasklet implements Tasklet {

    private final JdbcTemplate jdbcTemplate;

    public SoftDeleteCleanupTasklet(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        // Find all tables with 'deleted_at' column in the current database
        String schemaQuery = "SELECT DATABASE()";
        String currentSchema = jdbcTemplate.queryForObject(schemaQuery, String.class);

        String findTablesQuery = """
            SELECT TABLE_NAME 
            FROM information_schema.COLUMNS 
            WHERE TABLE_SCHEMA = ? 
            AND COLUMN_NAME = 'deleted_at'
        """;

        List<String> tables = jdbcTemplate.queryForList(findTablesQuery, String.class, currentSchema);

        if (tables != null) {
            for (String table : tables) {
                // Ensure table name is safe or quoted if necessary, though internal schema names are usually safe.
                // Using simple string concatenation for table name as PreparedStatement doesn't support table names.
                String deleteSql = String.format("DELETE FROM %s WHERE deleted_at IS NOT NULL", table);
                int count = jdbcTemplate.update(deleteSql);
                System.out.println("Deleted " + count + " rows from table: " + table);
            }
        }

        return RepeatStatus.FINISHED;
    }
}
