-- Usuários de teste para testes de integração do UserController
-- Passwords são bcrypt encoded (usando senha "password123" como exemplo)
-- bcrypt('password123') = $2a$10$fEKJmHqSHjlxzqVFGZb5Gu8aDT8rFuCn/7bMZa7F0nCc5Z9L6z5tS

-- Usuário ADMIN do tenant a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11
INSERT INTO users (id, tenant_id, name, email, password_hash, role, is_active, created_at)
VALUES (
    'a1111111-1111-1111-1111-111111111111',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'Admin User',
    'admin@tenant1.com',
    '$2a$10$fEKJmHqSHjlxzqVFGZb5Gu8aDT8rFuCn/7bMZa7F0nCc5Z9L6z5tS',
    'ADMIN',
    true,
    NOW()
);

-- Usuário OPERATOR do tenant a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11
INSERT INTO users (id, tenant_id, name, email, password_hash, role, is_active, created_at)
VALUES (
    'a2222222-2222-2222-2222-222222222222',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'Operator User',
    'operator@tenant1.com',
    '$2a$10$fEKJmHqSHjlxzqVFGZb5Gu8aDT8rFuCn/7bMZa7F0nCc5Z9L6z5tS',
    'OPERATOR',
    true,
    NOW()
);

-- Usuário FINANCE do tenant a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11
INSERT INTO users (id, tenant_id, name, email, password_hash, role, is_active, created_at)
VALUES (
    'a3333333-3333-3333-3333-333333333333',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'Finance User',
    'finance@tenant1.com',
    '$2a$10$fEKJmHqSHjlxzqVFGZb5Gu8aDT8rFuCn/7bMZa7F0nCc5Z9L6z5tS',
    'FINANCE',
    true,
    NOW()
);

-- Usuário ADMIN do tenant b1ffcd00-0d1c-5fa9-cc7e-7cc0ce491b22
INSERT INTO users (id, tenant_id, name, email, password_hash, role, is_active, created_at)
VALUES (
    'b1111111-1111-1111-1111-111111111111',
    'b1ffcd00-0d1c-5fa9-cc7e-7cc0ce491b22',
    'Admin User Tenant2',
    'admin@tenant2.com',
    '$2a$10$fEKJmHqSHjlxzqVFGZb5Gu8aDT8rFuCn/7bMZa7F0nCc5Z9L6z5tS',
    'ADMIN',
    true,
    NOW()
);

-- Usuário INATIVO do tenant a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11 (para teste de usuário inativo)
INSERT INTO users (id, tenant_id, name, email, password_hash, role, is_active, created_at)
VALUES (
    'a4444444-4444-4444-4444-444444444444',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'Inactive User',
    'inactive@tenant1.com',
    '$2a$10$fEKJmHqSHjlxzqVFGZb5Gu8aDT8rFuCn/7bMZa7F0nCc5Z9L6z5tS',
    'OPERATOR',
    false,
    NOW()
);

