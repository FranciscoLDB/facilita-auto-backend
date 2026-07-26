-- 1. Core Service Orders Table
CREATE TABLE service_orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    insurance_company_id UUID NOT NULL REFERENCES insurance_companies(id),
    service_type_id UUID NOT NULL REFERENCES service_types(id),
    operator_id UUID NOT NULL REFERENCES users(id),     -- Operator who created the order
    driver_id UUID REFERENCES users(id),               -- Assigned driver (Optional at creation)
    yard_id UUID REFERENCES yards(id),                 -- Yard location if vehicle is in custody [RF27]
    parent_service_order_id UUID REFERENCES service_orders(id), -- Linked OS for second-leg trips [RF29]

    claim_number VARCHAR(50) NOT NULL,                  -- Claim/Dispatch ID from Insurance (Acionamento) [RF11]
    request_number INT,                                 -- request number from Insurance, (1, 2, 3...)

    -- Status Management [RF09]
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    cancellation_reason TEXT,                          -- Reason required if status becomes 'CANCELLED' [RF07]

    -- Insured and Vehicle Details
    insured_details JSONB NOT NULL,                    -- {"name": "John", "phone": "+55...", "policy": "123"}
    vehicle_details JSONB NOT NULL,                    -- {"plate": "ABC1D23", "model": "Gol", "color": "Silver"}

    -- Pick-up Location / Origin (Rua, Bairro, Cidade, Estado, Complemento)
    origin_street VARCHAR(150) NOT NULL,
    origin_neighborhood VARCHAR(100) NOT NULL,
    origin_city VARCHAR(100) NOT NULL,
    origin_state VARCHAR(50) NOT NULL,
    origin_complement VARCHAR(100),

    -- Drop-off Location / Destination (Rua, Bairro, Cidade, Estado, Complemento)
    destination_street VARCHAR(150) NOT NULL,
    destination_neighborhood VARCHAR(100) NOT NULL,
    destination_city VARCHAR(100) NOT NULL,
    destination_state VARCHAR(50) NOT NULL,
    destination_complement VARCHAR(100),

    -- Total calculated from all service_order_items + tolls
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,

    -- Digital Signature
    signature_url VARCHAR(500),                        -- Object Storage URL [RF15]

    -- Timestamps
    opened_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraint to enforce valid operational status values
    CONSTRAINT chk_so_status CHECK (
        status IN (
            'PENDING',
            'EN_ROUTE_TO_ORIGIN',
            'ON_SITE_ORIGIN',
            'EN_ROUTE_TO_DESTINATION',
            'IN_YARD',
            'COMPLETED',
            'SCHEDULED',
            'CANCELLED'
        )
    )
);

-- 2. Service Order Line Items (Saída, KM Excedente, HP, HT, Patins, etc.)
CREATE TABLE service_order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_order_id UUID NOT NULL REFERENCES service_orders(id) ON DELETE CASCADE,
    item_type VARCHAR(50) NOT NULL,                    -- 'SAIDA_FIXA', 'KM_EXCEDENTE', etc.
    description VARCHAR(100),                          -- Optional custom description
    authorization_code VARCHAR(50),                    -- Optional authorization password/code from insurance company (Senha)
    quantity DECIMAL(10,2) NOT NULL DEFAULT 1.00,       -- Quantity (e.g., 40.00 KMs or 1.50 Hours)
    unit_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,     -- Price per unit
    total_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,    -- (quantity * unit_price)
    is_manual_override BOOLEAN NOT NULL DEFAULT FALSE, -- Identifies if price/qty was manually changed by operator
    status VARCHAR(30) NOT NULL DEFAULT 'TO_CHARGE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraint para validar os status do item
    CONSTRAINT chk_so_item_status CHECK (
        status IN (
            'TO_CHARGE',
            'TO_NEGOTIATE',
            'IN_NEGOTIATION',
            'POSTED',
            'CANCELLED'
        )
    )
);

-- 3. Status Change Timeline (Audit Trail) [RF09]
CREATE TABLE service_order_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_order_id UUID NOT NULL REFERENCES service_orders(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id),
    previous_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. Comments and Internal Notes Log [RF10]
CREATE TABLE service_order_comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_order_id UUID NOT NULL REFERENCES service_orders(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id),
    comment TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 5. Toll Receipts Submitted by Driver [RF16]
CREATE TABLE service_order_tolls (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_order_id UUID NOT NULL REFERENCES service_orders(id) ON DELETE CASCADE,
    amount DECIMAL(10,2) NOT NULL,
    receipt_photo_url VARCHAR(500) NOT NULL,           -- Object Storage URL
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 6. Checklist Answers Submitted by Driver in the Field [RF14, RF17]
CREATE TABLE service_order_checklist_answers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_order_id UUID NOT NULL REFERENCES service_orders(id) ON DELETE CASCADE,
    question_id UUID NOT NULL REFERENCES checklist_questions(id),
    text_answer TEXT,
    photo_url VARCHAR(500),                            -- Object Storage URL
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 7. Performance Indexes
CREATE INDEX idx_so_tenant ON service_orders(tenant_id);
CREATE INDEX idx_so_tenant_claim ON service_orders(tenant_id, claim_number);
CREATE INDEX idx_so_tenant_status ON service_orders(tenant_id, status);
CREATE INDEX idx_so_tenant_driver ON service_orders(tenant_id, driver_id);
CREATE INDEX idx_so_tenant_opened ON service_orders(tenant_id, opened_at DESC);
CREATE INDEX idx_so_tenant_plate ON service_orders(tenant_id, ((vehicle_details->>'plate')));
CREATE INDEX idx_so_items_so ON service_order_items(service_order_id);
CREATE INDEX idx_so_comments_so ON service_order_comments(service_order_id);
CREATE INDEX idx_so_history_so ON service_order_status_history(service_order_id);