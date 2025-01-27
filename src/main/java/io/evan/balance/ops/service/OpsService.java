package io.evan.balance.ops.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class OpsService {
    private final JdbcTemplate jdbcTemplate;

    public OpsService(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void cleanDatabase() {
        jdbcTemplate.execute("DELETE FROM accounts");
        jdbcTemplate.execute("DELETE FROM balance_logs");

        jdbcTemplate.execute("DELETE FROM transactions");
        jdbcTemplate.execute("DELETE FROM transactions_tcc");
    }

    public void createSampleAccounts(final int total) {
        String sql = "INSERT INTO accounts (account_number, balance, froze_balance, created_at, updated_at) VALUES (?, ?, 0.00, NOW(), NOW())";
        jdbcTemplate.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
            @Override
            public void setValues(java.sql.PreparedStatement ps, int i) throws java.sql.SQLException {
                String accountNumber = String.format("acc_%06d", i + 1);
                ps.setString(1, accountNumber);
                ps.setBigDecimal(2, new java.math.BigDecimal(1000 * (i + 1)));
            }

            @Override
            public int getBatchSize() {
                return total;
            }
        });
    }
}