package com.fldb.facilita.auto.api.controller;

import com.fldb.facilita.auto.api.dto.user.CreateUserRequest;
import com.fldb.facilita.auto.domain.entity.Tenant;
import com.fldb.facilita.auto.domain.entity.User;
import com.fldb.facilita.auto.domain.enums.UserRole;
import com.fldb.facilita.auto.domain.repository.TenantRepository;
import com.fldb.facilita.auto.domain.repository.UserRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static com.fldb.facilita.auto.util.TenantTestUtils.runInTenantContext;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoSpyBean
    private TenantRepository tenantRepository;

    @MockitoSpyBean
    private UserRepository userRepository;

    @Autowired
    private JwtTestUtil jwtTestUtil;

    private static final String ADMIN_KEY = "very-secret-key";
    private String TENANT_1_ID;
    private String TENANT_2_ID;
    private Map<String, List<User>> userTenants;

    @BeforeAll
    void setUp() {
        // 1. Tenants geralmente não têm @TenantId em si mesmos, então são salvos normal
        TENANT_1_ID = tenantRepository.save(Tenant.builder()
                .companyName("Tenant 1")
                .taxId("12345678900001")
                .build()).getId().toString();

        TENANT_2_ID = tenantRepository.save(Tenant.builder()
                .companyName("Tenant 2")
                .taxId("12345678900002")
                .build()).getId().toString();

        String passwordHash = passwordEncoder.encode("password123");

        // 2. Salva os usuários do Tenant 1 no contexto do Tenant 1
        runInTenantContext(UUID.fromString(TENANT_1_ID), () -> {
            userRepository.save(User.builder()
                    .name("User 1")
                    .email("user1@tenant1.com")
                    .passwordHash(passwordHash)
                    .role(UserRole.ADMIN)
                    .tenantId(UUID.fromString(TENANT_1_ID))
                    .build());

            userRepository.save(User.builder()
                    .name("User 2")
                    .email("user2@tenant1.com")
                    .passwordHash(passwordHash)
                    .role(UserRole.OPERATOR)
                    .tenantId(UUID.fromString(TENANT_1_ID))
                    .build());
        });

        // 3. Salva os usuários do Tenant 2 no contexto do Tenant 2
        runInTenantContext(UUID.fromString(TENANT_2_ID), () -> {
            userRepository.save(User.builder()
                    .name("User 3")
                    .email("user3@tenant2.com")
                    .passwordHash(passwordHash)
                    .role(UserRole.ADMIN)
                    .tenantId(UUID.fromString(TENANT_2_ID))
                    .build());
        });

        // 4. Se o seu repositório aplica o filtro de multitenancy no findAll(),
        // você precisa buscar sem o filtro ou concatenar as buscas por tenant:
        userTenants = Map.of(
                TENANT_1_ID, runInTenantContext(UUID.fromString(TENANT_1_ID), () -> userRepository.findAll()),
                TENANT_2_ID, runInTenantContext(UUID.fromString(TENANT_2_ID), () -> userRepository.findAll())
        );
    }

    @AfterAll
    void clearData() {
        userRepository.deleteAll();
        tenantRepository.deleteAll();
    }

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
    void shouldCreateUserSuccessfullyWithValidToken() throws Exception {
        String adminToken = jwtTestUtil.generateAdminTokenForTenant(UUID.fromString(TENANT_1_ID));
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
                .when(userRepository).saveAndFlush(any());

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
    void shouldFailWhenEmailAlreadyExists() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .name("Duplicate Email User")
                .email("user1@tenant1.com")
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
        String nonexistentTenantId = "ab000000-0000-0000-0000-000000000000";
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
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    @DisplayName("Cenário 12: Deve falhar quando X-Tenant-ID está ausente no fluxo Admin")
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
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    @DisplayName("Cenário 13: Usuário com token cria usuário apenas para seu próprio tenant")
    void shouldCreateUserOnlyInOwnTenant() throws Exception {
        String tenant1AdminToken = jwtTestUtil.generateAdminTokenForTenant(UUID.fromString(TENANT_1_ID));
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
    @DisplayName("Cenário 15: Ambos headers presentes - JWT key tem prioridade")
    void shouldPrioritizeJwtTokenWhenBothHeadersPresent() throws Exception {
        String adminToken = jwtTestUtil.generateAdminTokenForTenant(UUID.fromString(TENANT_1_ID));
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
                .andExpect(jsonPath("$.data.tenantId").value(TENANT_1_ID));
    }

    @Test
    @DisplayName("Cenário 16: Listar usuários - JWT Token")
    void shouldListUsersWithJwtToken() throws Exception {
        String adminToken = jwtTestUtil.generateAdminTokenForTenant(UUID.fromString(TENANT_1_ID));

        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[*].tenantId",
                        Matchers.everyItem(Matchers.is(TENANT_1_ID))));
    }

    @Test
    @DisplayName("Cenário 17: Listar usuários - JWT Token sem permissões")
    void shouldNotListUsersWithoutProperPermissions() throws Exception {
        String financeToken = jwtTestUtil.generateFinanceToken();
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + financeToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Cenário 18: Deletar usuário - JWT Token")
    void shouldDeleteUserWithJwtToken() throws Exception {
        AtomicReference<String> userId = new AtomicReference<>();
        runInTenantContext(UUID.fromString(TENANT_2_ID), () -> {
            User user = User.builder()
                    .name("User 4")
                    .email("user4@tenant2.com")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .role(UserRole.OPERATOR)
                    .tenantId(UUID.fromString(TENANT_2_ID))
                    .build();
            userRepository.save(user);
            userId.set(user.getId().toString());
        });

        String adminToken = jwtTestUtil.generateAdminTokenForTenant(UUID.fromString(TENANT_2_ID));
        mockMvc.perform(delete("/api/v1/users/{id}", userId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        Optional<User> deletedUser = userRepository.findById(UUID.fromString(userId.get()));
        assertThat(deletedUser).isNotPresent();
    }

    @Test
    @DisplayName("Cenário 19: Deletar próprio usuário - Deve falhar")
    void shouldNotDeleteOwnUser() throws Exception {
        User user = userTenants.get(TENANT_2_ID).getFirst();

        String operatorToken = jwtTestUtil.generateValidToken(user);
        mockMvc.perform(delete("/api/v1/users/{id}", user.getId())
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Cenário 20: Deletar usuário de outro tenant - Deve falhar")
    void shouldNotDeleteUserFromDifferentTenant() throws Exception {
        User user1 = userTenants.get(TENANT_1_ID).getFirst();
        User user2 = userTenants.get(TENANT_2_ID).getFirst();

        String adminToken = jwtTestUtil.generateValidToken(user2);
        mockMvc.perform(delete("/api/v1/users/{id}", user1.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
