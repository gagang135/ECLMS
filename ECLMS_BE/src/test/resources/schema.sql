-- Initialize JDBC-only tables in H2 test database
CREATE TABLE IF NOT EXISTS login_histories (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    ip_address VARCHAR(45) NULL,
    user_agent VARCHAR(255) NULL,
    timestamp TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id VARCHAR(36) PRIMARY KEY,
    entity_name VARCHAR(255) NOT NULL,
    entity_id VARCHAR(36) NOT NULL,
    action VARCHAR(50) NOT NULL,
    old_value TEXT NULL,
    new_value TEXT NULL,
    user_id VARCHAR(100) NOT NULL,
    ip_address VARCHAR(45) NULL,
    timestamp TIMESTAMP NOT NULL
);
