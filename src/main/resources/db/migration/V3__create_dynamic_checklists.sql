-- 1. Checklists Template Table (Linked to Service Types)
CREATE TABLE checklists (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    service_type_id UUID NOT NULL REFERENCES service_types(id),
    title VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Checklist Questions / Fields Configuration
CREATE TABLE checklist_questions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    checklist_id UUID NOT NULL REFERENCES checklists(id) ON DELETE CASCADE,
    position_order INT NOT NULL,                        -- Question order (1, 2, 3...)
    question_text VARCHAR(255) NOT NULL,                -- Question prompt (e.g., "Odometer photo")
    response_type VARCHAR(20) NOT NULL,                 -- 'TEXT', 'CHECKBOX', 'PHOTO', 'MULTIPLE_CHOICE'
    is_mandatory BOOLEAN NOT NULL DEFAULT FALSE,        -- Blocks app progression if true
    options_json JSONB,                                 -- Stores options for 'MULTIPLE_CHOICE' (e.g., ["Yes", "No", "N/A"])
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraint to enforce valid response types
    CONSTRAINT chk_question_response_type CHECK (response_type IN ('TEXT', 'CHECKBOX', 'PHOTO', 'MULTIPLE_CHOICE'))
);

-- 3. Performance Indexes
CREATE INDEX idx_checklists_tenant ON checklists(tenant_id);
CREATE INDEX idx_checklists_service_type ON checklists(service_type_id);
CREATE INDEX idx_checklist_questions_checklist ON checklist_questions(checklist_id);