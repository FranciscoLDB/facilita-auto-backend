package com.fldb.facilita.auto.api.controller;

import com.fldb.facilita.auto.api.config.security.JwtTokenProvider;
import com.fldb.facilita.auto.api.dto.ApiResponseData;
import com.fldb.facilita.auto.domain.entity.Tenant;
import com.fldb.facilita.auto.domain.entity.User;
import com.fldb.facilita.auto.domain.enums.UserRole;
import com.fldb.facilita.auto.domain.repository.TenantRepository;
import com.fldb.facilita.auto.domain.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.fldb.facilita.auto.util.TenantTestUtils.runInTenantContext;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LoginControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoSpyBean
    private AuthenticationManager authenticationManagerMock;

    private final String rawPassword = "password123";

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

        String passwordHash = passwordEncoder.encode(rawPassword);

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

    @Test
    @DisplayName("Cenário 1: Deve autenticar com sucesso e verificar que o tenantID no token é o correto")
    void shouldLoginSuccessfullyAndVerifyTenantId() throws Exception {
        User user = userTenants.get(TENANT_1_ID).getFirst();
        var loginRequest = new AuthController.LoginRequest(user.getEmail(), rawPassword);

        String responseJson = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Autenticado com sucesso."))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        ApiResponseData<?> response = objectMapper.readValue(responseJson, ApiResponseData.class);
        String token = (String) response.getData();

        var claims = jwtTokenProvider.validateToken(token);
        String tenantIdFromToken = claims.getBody().get("tenantId", String.class);

        org.assertj.core.api.Assertions.assertThat(tenantIdFromToken)
                .isEqualTo(TENANT_1_ID);
    }

    @Test
    @DisplayName("Cenário 2: Deve retornar erro ao tentar logar com senha incorreta")
    void shouldFailWhenPasswordIsWrong() throws Exception {
        User user = userTenants.get(TENANT_1_ID).getFirst();
        var loginRequest = new AuthController.LoginRequest(user.getEmail(), "senhaErrada");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].code").value("AUT-001"));
    }

    @Test
    @DisplayName("Cenário 3: Deve retornar erro ao tentar logar com e-mail inexistente")
    void shouldFailWhenEmailNotFound() throws Exception {
        var loginRequest = new AuthController.LoginRequest("naoexiste@email.com", rawPassword);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].code").value("AUT-001"));
    }

    @Test
    @DisplayName("Cenário 4: Deve retornar erro DATABASE_ERROR quando a base de dados estiver fora do ar")
    void shouldFailWhenDatabaseIsDown() throws Exception {
        doThrow(new org.springframework.dao.DataAccessResourceFailureException("Database connection error"))
                .when(authenticationManagerMock).authenticate(any());

        User user = userTenants.get(TENANT_1_ID).getFirst();
        var loginRequest = new AuthController.LoginRequest(user.getEmail(), rawPassword);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].code").value("SYS-501"));
    }

}
