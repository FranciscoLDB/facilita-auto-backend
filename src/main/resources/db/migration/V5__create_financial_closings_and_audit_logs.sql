-- 1. Financial Closing Batches Table [RF20]
CREATE TABLE financial_closings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    insurance_company_id UUID NOT NULL REFERENCES insurance_companies(id),
    batch_code VARCHAR(50) NOT NULL,                    -- Unique batch identifier (e.g., 'BATCH-PORTO-2026-07')
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,  -- Sum of all service order items in this batch
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',         -- 'OPEN', 'CLOSED', 'INVOICED'
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraint to enforce valid batch status
    CONSTRAINT chk_closing_status CHECK (
        status IN (
            'OPEN',
            'CLOSED',
            'INVOICED'
        )
    )
);

-- 2. Junction Table linking Service Order Items to Financial Closing Batches
CREATE TABLE financial_closing_items (
    financial_closing_id UUID NOT NULL REFERENCES financial_closings(id) ON DELETE CASCADE,
    service_order_item_id UUID NOT NULL REFERENCES service_order_items(id),
    PRIMARY KEY (financial_closing_id, service_order_item_id)
);

-- 3. System Audit Logs Table [RF04]
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    user_id UUID NOT NULL REFERENCES users(id),
    action VARCHAR(50) NOT NULL,                        -- Action performed (e.g., 'UPDATE_KM', 'CHANGE_STATUS')
    entity_name VARCHAR(50) NOT NULL,                   -- Affected entity (e.g., 'service_orders')
    entity_id UUID NOT NULL,                            -- ID of the affected record
    details_json JSONB,                                 -- Stores old vs new values: {"old": {...}, "new": {...}}
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. Performance Indexes
CREATE INDEX idx_financial_closings_tenant ON financial_closings(tenant_id);
CREATE INDEX idx_financial_closings_insurance ON financial_closings(insurance_company_id);
CREATE INDEX idx_audit_logs_tenant ON audit_logs(tenant_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_name, entity_id);
CREATE INDEX idx_audit_logs_created ON audit_logs(created_at DESC);