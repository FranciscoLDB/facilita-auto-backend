package com.fldb.facilita.auto.api.controller;

import com.fldb.facilita.auto.api.dto.user.CreateUserRequest;
import com.fldb.facilita.auto.domain.enums.UserRole;
import com.fldb.facilita.auto.domain.repository.TenantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class UserControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoSpyBean
    private TenantRepository tenantRepository;

    @Autowired
    private JwtTestUtil jwtTestUtil;

    private static final String ADMIN_KEY = "very-secret-key";
    private static final String TENANT_1_ID = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11";
    private static final String TENANT_2_ID = "b1ffcd00-0d1c-5fa9-cc7e-7cc0ce491b22";

    @Test
    @DisplayName("Cenário 1: Deve negar solicitação sem token e sem chave Admin")
    void shouldReturn401WhenNoTokenAndNoAdminKey() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .name("New User")
                .email("newuser@example.com")
                .password("password123")
                .role(UserRole.OPERATOR)
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Cenário 2: Deve negar acesso HTTP 401 se a chave X-Admin-Api-Key for inválida")
    void shouldReturn401WhenAdminKeyIsInvalid() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .name("New User")
                .email("newuser@example.com")
                .password("password123")
                .role(UserRole.OPERATOR)
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .header("X-Admin-Api-Key", "WRONG_KEY")
                        .header("X-Tenant-ID", TENANT_1_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value(401))
                .andExpect(jsonPath("$.message").value("Not authorized"));
    }

    @Test
    @DisplayName("Cenário 3: Deve retornar HTTP 400 com lista de erros quando request for inválido")
    void shouldReturn400WhenValidationFails() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .name("a")
                .email("invalid-email")
                .password("1")
                .role(UserRole.OPERATOR)
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .header("X-Admin-Api-Key", ADMIN_KEY)
                        .header("X-Tenant-ID", TENANT_1_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    @DisplayName("Cenário 4: Deve negar acesso caso usuário não tenha autorização")
    @Sql(scripts = "/scripts/seed-tenant.sql")
    void shouldDenyAccessForUserWithoutRole() throws Exception {
        String financeToken = jwtTestUtil.generateFinanceToken();
        CreateUserRequest request = CreateUserRequest.builder()
                .name("New User")
                .email("newuser@example.com")
                .password("password123")
                .role(UserRole.OPERATOR)
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + financeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Cenário 5: Deve cadastrar com sucesso sem token mas com chave X-Admin-Api-Key")
    @Sql(scripts = "/scripts/seed-tenant.sql")
    void shouldCreateUserSuccessfullyWithAdminKey() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .name("Admin Created User")
                .email("admincreated@example.com")
                .password("password123")
                .role(UserRole.OPERATOR)
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .header("X-Admin-Api-Key", ADMIN_KEY)
                        .header("X-Tenant-ID", TENANT_1_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.message").value("User created successfully."))
                .andExpect(jsonPath("$.data.name").value("Admin Created User"))
                .andExpect(jsonPath("$.data.email").value("admincreated@example.com"))
                .andExpect(jsonPath("$.data.tenantId").value(TENANT_1_ID));
    }

    @Test
    @DisplayName("Cenário 6: Deve cadastrar com sucesso com token valido e role valida")
    @Sql(scripts = {"/scripts/seed-tenant.sql", "/scripts/seed-users.sql"})
    void shouldCreateUserSuccessfullyWithValidToken() throws Exception {
        String adminToken = jwtTestUtil.generateAdminToken();
        CreateUserRequest request = CreateUserRequest.builder()
                .name("Token Created User")
                .email("tokencreated@example.com")
                .password("password123")
                .role(UserRole.OPERATOR)
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.message").value("User created successfully."))
                .andExpect(jsonPath("$.data.name").value("Token Created User"))
                .andExpect(jsonPath("$.data.email").value("tokencreated@example.com"))
                .andExpect(jsonPath("$.data.tenantId").value(TENANT_1_ID));
    }

    @Test
    @DisplayName("Cenário 7: Deve falhar caso token tenha expirado")
    @Sql(scripts = "/scripts/seed-tenant.sql")
    void shouldFailWhenTokenIsExpired() throws Exception {
        String expiredToken = jwtTestUtil.generateExpiredToken();
        CreateUserRequest request = CreateUserRequest.builder()
                .name("New User")
                .email("newuser@example.com")
                .password("password123")
                .role(UserRole.OPERATOR)
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + expiredToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Cenário 8: Deve retornar HTTP 500 com JSON padrão quando o banco de dados estiver fora do ar")
    void shouldReturn500WhenDatabaseIsUnavailable() throws Exception {
        doThrow(new DataAccessResourceFailureException("Connection refused to PostgreSQL server"))
                .when(tenantRepository).findById(any());

        CreateUserRequest request = CreateUserRequest.builder()
                .name("New User")
                .email("newuser@example.com")
                .password("password123")
                .role(UserRole.OPERATOR)
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .header("X-Admin-Api-Key", ADMIN_KEY)
                        .header("X-Tenant-ID", TENANT_1_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.statusCode").value(500));
    }

    @Test
    @DisplayName("Cenário 9: Deve falhar ao tentar criar usuário com email duplicado")
    @Sql(scripts = {"/scripts/seed-tenant.sql", "/scripts/seed-users.sql"})
    void shouldFailWhenEmailAlreadyExists() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .name("Duplicate Email User")
                .email("admin@tenant1.com")
                .password("password123")
                .role(UserRole.OPERATOR)
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .header("X-Admin-Api-Key", ADMIN_KEY)
                        .header("X-Tenant-ID", TENANT_1_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    @DisplayName("Cenário 10: Deve falhar ao criar usuário com tenant inexistente")
    void shouldFailWhenTenantDoesNotExist() throws Exception {
        String nonexistentTenantId = "00000000-0000-0000-0000-000000000000";
        CreateUserRequest request = CreateUserRequest.builder()
                .name("User for Nonexistent Tenant")
                .email("newuser@example.com")
                .password("password123")
                .role(UserRole.OPERATOR)
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .header("X-Admin-Api-Key", ADMIN_KEY)
                        .header("X-Tenant-ID", nonexistentTenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404));
    }

    @Test
    @DisplayName("Cenário 11: Deve falhar quando X-Tenant-ID está inválido (não é UUID)")
    @Sql(scripts = "/scripts/seed-tenant.sql")
    void shouldFailWhenTenantIdIsInvalid() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .name("New User")
                .email("newuser@example.com")
                .password("password123")
                .role(UserRole.OPERATOR)
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .header("X-Admin-Api-Key", ADMIN_KEY)
                        .header("X-Tenant-ID", "not-a-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value("Invalid X-Tenant-ID format."));
    }

    @Test
    @DisplayName("Cenário 12: Deve falhar quando X-Tenant-ID está ausente no fluxo Admin")
    @Sql(scripts = "/scripts/seed-tenant.sql")
    void shouldFailWhenTenantIdIsMissingWithAdminKey() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .name("New User")
                .email("newuser@example.com")
                .password("password123")
                .role(UserRole.OPERATOR)
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .header("X-Admin-Api-Key", ADMIN_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value("X-Tenant-ID is required."));
    }

    @Test
    @DisplayName("Cenário 13: Usuário com token cria usuário apenas para seu próprio tenant")
    @Sql(scripts = {"/scripts/seed-tenant.sql", "/scripts/seed-users.sql"})
    void shouldCreateUserOnlyInOwnTenant() throws Exception {
        String tenant1AdminToken = jwtTestUtil.generateAdminToken();
        CreateUserRequest request = CreateUserRequest.builder()
                .name("New User for Tenant1")
                .email("user4@example.com")
                .password("password123")
                .role(UserRole.OPERATOR)
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + tenant1AdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.tenantId").value(TENANT_1_ID));
    }

    @Test
    @DisplayName("Cenário 14: Usuário de tenant 2 não consegue criar usuário em tenant 1 via token")
    @Sql(scripts = {"/scripts/seed-tenant.sql", "/scripts/seed-users.sql"})
    void shouldNotCreateUserInDifferentTenant() throws Exception {
        String tenant2AdminToken = jwtTestUtil.generateAdminTokenTenant2();
        CreateUserRequest request = CreateUserRequest.builder()
                .name("User for Tenant1 from Tenant2")
                .email("user6@example.com")
                .password("password123")
                .role(UserRole.OPERATOR)
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + tenant2AdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.tenantId").value(TENANT_2_ID));
    }

    @Test
    @DisplayName("Cenário 15: Ambos headers presentes - API key tem prioridade")
    @Sql(scripts = {"/scripts/seed-tenant.sql", "/scripts/seed-users.sql"})
    void shouldPrioritizeApiKeyWhenBothHeadersPresent() throws Exception {
        String adminToken = jwtTestUtil.generateAdminToken();
        CreateUserRequest request = CreateUserRequest.builder()
                .name("User with Both Headers")
                .email("bothheaders@example.com")
                .password("password123")
                .role(UserRole.OPERATOR)
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .header("X-Admin-Api-Key", ADMIN_KEY)
                        .header("X-Tenant-ID", TENANT_2_ID)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.tenantId").value(TENANT_2_ID));
    }
}
