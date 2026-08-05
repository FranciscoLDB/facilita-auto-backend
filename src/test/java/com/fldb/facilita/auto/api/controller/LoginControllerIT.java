package com.fldb.facilita.auto.api.controller;

import com.fldb.facilita.auto.api.config.security.CustomUserDetailsService;
import com.fldb.facilita.auto.api.config.security.JwtTokenProvider;
import com.fldb.facilita.auto.api.dto.ApiResponseData;
import com.fldb.facilita.auto.api.exception.ErrorCode;
import com.fldb.facilita.auto.domain.entity.Tenant;
import com.fldb.facilita.auto.domain.entity.User;
import com.fldb.facilita.auto.domain.enums.UserRole;
import com.fldb.facilita.auto.domain.repository.TenantRepository;
import com.fldb.facilita.auto.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
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
    private CustomUserDetailsService customUserDetailsServiceSpy;

    @MockitoSpyBean
    private UserRepository userRepositorySpy;

    @MockitoSpyBean
    private AuthenticationManager authenticationManagerMock;

    private Tenant testTenant;
    private User testUser;
    private final String rawPassword = "password123";

    @BeforeEach
    void setUp() {
        // Limpa a base por garantia no contexto transacional
        userRepository.deleteAll();
        tenantRepository.deleteAll();

        // Cria e salva um Tenant para associar ao usuário
        testTenant = Tenant.builder()
                .companyName("Tenant Teste")
                .taxId("12345678901234")
                .build();
        testTenant = tenantRepository.save(testTenant);

        // Cria e salva um User com senha codificada
        testUser = User.builder()
                .tenant(testTenant)
                .name("Usuário Teste")
                .email("teste@email.com")
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role(UserRole.ADMIN)
                .isActive(true)
                .build();
        userRepository.save(testUser);
    }


    @Test
    @DisplayName("Cenário 1: Deve autenticar com sucesso e verificar que o tenantID no token é o correto")
    void shouldLoginSuccessfullyAndVerifyTenantId() throws Exception {
        var loginRequest = new AuthController.LoginRequest("teste@email.com", rawPassword);

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
                .isEqualTo(testTenant.getId().toString());
    }

    @Test
    @DisplayName("Cenário 2: Deve retornar erro ao tentar logar com senha incorreta")
    void shouldFailWhenPasswordIsWrong() throws Exception {
        var loginRequest = new AuthController.LoginRequest("teste@email.com", "senhaErrada");

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

        var loginRequest = new AuthController.LoginRequest("teste@email.com", rawPassword);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].code").value("SYS-501"));
    }

}
