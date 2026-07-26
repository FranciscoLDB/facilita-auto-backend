-- 1. Insurance Companies Table
CREATE TABLE insurance_companies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    name VARCHAR(100) NOT NULL,
    base_code VARCHAR(50),                          -- Branch/Base code (Optional, e.g., 'BASE-PR-01')
    operational_system_url VARCHAR(255),            -- Portal URL for operational/dispatch routines
    closing_system_url VARCHAR(255),                -- Portal URL for monthly financial closing
    system_username VARCHAR(100),                   -- Portal login username
    system_password VARCHAR(100),                   -- Encrypted portal password
    contact_phones TEXT,                            -- Emergency and support phone numbers
    notes TEXT,                                     -- Operational notes and instructions
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Service Types Table
CREATE TABLE service_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    name VARCHAR(50) NOT NULL,                       -- Ex: Towing, Battery Jump, Locksmith, Dollies/Skate
    description VARCHAR(200),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. Vehicle Storage Yards / Company Bases
CREATE TABLE yards (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    name VARCHAR(100) NOT NULL,
    -- Location / yard (Rua, Bairro, Cidade, Estado, Complemento)
    yard_street VARCHAR(150) NOT NULL,
    yard_neighborhood VARCHAR(100) NOT NULL,
    yard_city VARCHAR(100) NOT NULL,
    yard_state VARCHAR(50) NOT NULL,
    yard_complement VARCHAR(100),
    max_capacity INT NOT NULL,
    manager_id UUID REFERENCES users(id),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. Dynamic Pricing Tables per Insurance Company
CREATE TABLE pricing_tables (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    insurance_company_id UUID NOT NULL REFERENCES insurance_companies(id),
    service_type_id UUID NOT NULL REFERENCES service_types(id),
    base_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,          -- Base / Departure fee (Saída)
    extra_km_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,      -- Extra KM fee for paved roads (KM Excedente)
    dirt_road_km_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,  -- Extra KM fee for dirt/unpaved roads (KM Estrada de Chão)
    included_km_allowance INT NOT NULL DEFAULT 0,          -- Included KM allowance
    idle_hour_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,     -- Stopped hour fee (HP - Hora Parada)
    worked_hour_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,   -- Worked hour fee (HT - Hora Trabalhada)
    skate_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,         -- Optional skate/dolly add-on fee
    night_shift_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,   -- Additional fee for night services
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraint: prevent duplicate pricing for the same insurance company and service type per tenant
    CONSTRAINT uk_pricing_insurance_service UNIQUE (tenant_id, insurance_company_id, service_type_id)
);

-- 5. Performance Indexes
CREATE INDEX idx_insurance_companies_tenant ON insurance_companies(tenant_id);
CREATE INDEX idx_service_types_tenant ON service_types(tenant_id);
CREATE INDEX idx_yards_tenant ON yards(tenant_id);
CREATE INDEX idx_pricing_tables_tenant ON pricing_tables(tenant_id);