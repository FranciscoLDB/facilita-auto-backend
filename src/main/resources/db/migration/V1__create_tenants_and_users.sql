-- 1. Enable native UUID support in PostgreSQL
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 2. Tenants Table (Companies / Service Providers)
CREATE TABLE tenants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_name VARCHAR(100) NOT NULL,
    tax_id VARCHAR(14) UNIQUE NOT NULL, -- CNPJ in Brazil
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. System Users Table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL, -- 'ADMIN', 'OPERATOR', 'FINANCE', 'DRIVER'
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraint to enforce valid roles
    CONSTRAINT chk_user_role CHECK (
        role IN (
            'ADMIN',
            'OPERATOR',
            'FINANCE',
            'DRIVER'
        )
    )
);

-- 4. Performance index for tenant-based queries
CREATE INDEX idx_users_tenant ON users(tenant_id);