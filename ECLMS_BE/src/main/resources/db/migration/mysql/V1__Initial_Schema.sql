-- 1. Departments Table
CREATE TABLE departments (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    parent_department_id VARCHAR(36) REFERENCES departments(id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NULL,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP NULL,
    deleted_by VARCHAR(100) NULL,
    version INT DEFAULT 0 NOT NULL
);

-- 2. Permissions Table
CREATE TABLE permissions (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    permission_group VARCHAR(100) NOT NULL,
    description VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NULL,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP NULL,
    deleted_by VARCHAR(100) NULL,
    version INT DEFAULT 0 NOT NULL
);

-- 3. Roles Table
CREATE TABLE roles (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255) NULL,
    parent_role_id VARCHAR(36) REFERENCES roles(id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NULL,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP NULL,
    deleted_by VARCHAR(100) NULL,
    version INT DEFAULT 0 NOT NULL
);

-- Role-Permissions Mapping Table
CREATE TABLE role_permissions (
    role_id VARCHAR(36) NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id VARCHAR(36) NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- 4. Users Table
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    department_id VARCHAR(36) REFERENCES departments(id),
    status VARCHAR(50) DEFAULT 'ACTIVE' NOT NULL,
    locked BOOLEAN DEFAULT FALSE NOT NULL,
    failed_login_attempts INT DEFAULT 0 NOT NULL,
    locked_until TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NULL,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP NULL,
    deleted_by VARCHAR(100) NULL,
    version INT DEFAULT 0 NOT NULL
);

-- User-Roles Mapping Table
CREATE TABLE user_roles (
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id VARCHAR(36) NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- 5. Teams Table
CREATE TABLE teams (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    department_id VARCHAR(36) REFERENCES departments(id),
    lead_user_id VARCHAR(36) REFERENCES users(id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NULL,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP NULL,
    deleted_by VARCHAR(100) NULL,
    version INT DEFAULT 0 NOT NULL
);

-- Team-Members Mapping Table
CREATE TABLE team_members (
    team_id VARCHAR(36) NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (team_id, user_id)
);

-- 6. Vendors Table
CREATE TABLE vendors (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NULL,
    address TEXT NULL,
    gst_number VARCHAR(15) NULL,
    pan_number VARCHAR(10) NULL,
    risk_level VARCHAR(50) DEFAULT 'LOW' NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NULL,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP NULL,
    deleted_by VARCHAR(100) NULL,
    version INT DEFAULT 0 NOT NULL
);

-- 7. Contract Templates Table
CREATE TABLE contract_templates (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    content TEXT NOT NULL,
    variables TEXT NULL,
    version_string VARCHAR(50) DEFAULT '1.0' NOT NULL,
    status VARCHAR(50) DEFAULT 'DRAFT' NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NULL,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP NULL,
    deleted_by VARCHAR(100) NULL,
    version INT DEFAULT 0 NOT NULL
);

-- 8. Contracts Table
CREATE TABLE contracts (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    vendor_id VARCHAR(36) NOT NULL REFERENCES vendors(id),
    department_id VARCHAR(36) NOT NULL REFERENCES departments(id),
    template_id VARCHAR(36) REFERENCES contract_templates(id),
    content TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'DRAFT' NOT NULL,
    start_date DATE NULL,
    end_date DATE NULL,
    renewal_date DATE NULL,
    risk_score DECIMAL(5, 2) DEFAULT 0.00 NOT NULL,
    risk_assessment TEXT NULL,
    metadata TEXT NULL,
    version_string VARCHAR(50) DEFAULT '1.0' NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NULL,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP NULL,
    deleted_by VARCHAR(100) NULL,
    version INT DEFAULT 0 NOT NULL
);

-- 9. Workflows Table
CREATE TABLE workflows (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE' NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NULL,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP NULL,
    deleted_by VARCHAR(100) NULL,
    version INT DEFAULT 0 NOT NULL
);

-- Workflow Steps Table
CREATE TABLE workflow_steps (
    id VARCHAR(36) PRIMARY KEY,
    workflow_id VARCHAR(36) NOT NULL REFERENCES workflows(id) ON DELETE CASCADE,
    step_number INT NOT NULL,
    step_type VARCHAR(50) NOT NULL,
    assignee_role_id VARCHAR(36) REFERENCES roles(id),
    assignee_user_id VARCHAR(36) REFERENCES users(id),
    required_approvals INT DEFAULT 1 NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NULL,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP NULL,
    deleted_by VARCHAR(100) NULL,
    version INT DEFAULT 0 NOT NULL
);

-- Workflow Instances Table
CREATE TABLE workflow_instances (
    id VARCHAR(36) PRIMARY KEY,
    workflow_id VARCHAR(36) NOT NULL REFERENCES workflows(id),
    contract_id VARCHAR(36) NOT NULL REFERENCES contracts(id),
    current_step_number INT DEFAULT 1 NOT NULL,
    status VARCHAR(50) DEFAULT 'IN_PROGRESS' NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NULL,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP NULL,
    deleted_by VARCHAR(100) NULL,
    version INT DEFAULT 0 NOT NULL
);

-- Workflow Approvals Table
CREATE TABLE workflow_approvals (
    id VARCHAR(36) PRIMARY KEY,
    workflow_instance_id VARCHAR(36) NOT NULL REFERENCES workflow_instances(id) ON DELETE CASCADE,
    step_number INT NOT NULL,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id),
    status VARCHAR(50) NOT NULL,
    comments TEXT NULL,
    action_date TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NULL,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP NULL,
    deleted_by VARCHAR(100) NULL,
    version INT DEFAULT 0 NOT NULL
);

-- 10. Documents Table
CREATE TABLE documents (
    id VARCHAR(36) PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(512) NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    checksum VARCHAR(64) NOT NULL,
    doc_size BIGINT NOT NULL,
    version_number INT DEFAULT 1 NOT NULL,
    ocr_text TEXT NULL,
    metadata TEXT NULL,
    contract_id VARCHAR(36) REFERENCES contracts(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NULL,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP NULL,
    deleted_by VARCHAR(100) NULL,
    version INT DEFAULT 0 NOT NULL
);

-- 11. Audit Logs Table
CREATE TABLE audit_logs (
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

-- 12. Login Histories Table
CREATE TABLE login_histories (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    ip_address VARCHAR(45) NULL,
    user_agent VARCHAR(255) NULL,
    timestamp TIMESTAMP NOT NULL
);

-- Indexes for performance Optimization
CREATE INDEX idx_contracts_vendor ON contracts(vendor_id);
CREATE INDEX idx_contracts_department ON contracts(department_id);
CREATE INDEX idx_contracts_status ON contracts(status);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_audit_entity ON audit_logs(entity_name, entity_id);
