package com.fldb.facilita.auto.api.controller;


import com.fldb.facilita.auto.api.dto.tenant.CreateTenantRequest;
import com.fldb.facilita.auto.domain.repository.TenantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class TenantControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoSpyBean
    private TenantRepository tenantRepository;

    private static final String ADMIN_KEY = "very-secret-key";

    @Test
    @DisplayName("Cenário 1: Deve cadastrar Tenant com sucesso (Retorno HTTP 201 e JSON padrão)")
    void shouldCreateTenantEndToEnd() throws Exception {
        CreateTenantRequest request = CreateTenantRequest.builder()
                .name("Guinchos do Sul")
                .cnpj("99.888.777/0001-11")
                .build();

        mockMvc.perform(post("/api/v1/tenants")
                        .header("X-Admin-Api-Key", ADMIN_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.message").value("Tenant cadastrado com sucesso."))
                .andExpect(jsonPath("$.data.name").value("Guinchos do Sul"))
                .andExpect(jsonPath("$.data.cnpj").value("99888777000111"))
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    @DisplayName("Cenário 2: Deve negar acesso HTTP 401 se a chave X-Admin-Api-Key for inválida")
    void shouldReturn401WhenApiKeyIsInvalid() throws Exception {
        CreateTenantRequest request = CreateTenantRequest.builder()
                .name("Guinchos do Sul")
                .cnpj("99.888.777/0001-11")
                .build();

        mockMvc.perform(post("/api/v1/tenants")
                        .header("X-Admin-Api-Key", "CHAVE_ERRADA")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value(401))
                .andExpect(jsonPath("$.message").value("Not authorized"));
    }

    @Test
    @DisplayName("Cenário 3: Deve retornar HTTP 400 com lista de erros quando request for inválido")
    void shouldReturn400WhenValidationFails() throws Exception {
        CreateTenantRequest request = CreateTenantRequest.builder()
                .name("") // Nome em branco
                .cnpj("123") // CNPJ inválido
                .build();

        mockMvc.perform(post("/api/v1/tenants")
                        .header("X-Admin-Api-Key", ADMIN_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors[0].field").exists());
    }

    @Test
    @Sql(scripts = "/scripts/seed-tenant.sql") // Carrega Tenant prévio via script SQL
    @DisplayName("Cenário 4: Deve buscar um Tenant existente criado via script de banco")
    void shouldFindTenantCreatedByScript() throws Exception {
        // ID gerado dentro do arquivo seed-tenant.sql
        String tenantId = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11";

        mockMvc.perform(get("/api/v1/tenants/" + tenantId)
                        .header("X-Admin-Api-Key", ADMIN_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.id").value(tenantId));
    }

    @Test
    @Sql(scripts = "/scripts/seed-tenant.sql") // Carrega Tenant prévio via script SQL
    @DisplayName("Cenário 5: Deve listar todos os tenants e retornar 2 registros cadastrados")
    void shouldFindAllTenantsSuccessfully() throws Exception {
        // Act & Assert: Faz a requisição GET e valida o conteúdo da página
        mockMvc.perform(get("/api/v1/tenants")
                        .header("X-Admin-Api-Key", ADMIN_KEY)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Lista de tenants recuperada com sucesso."))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].name").value("Empresa SQL 1"))
                .andExpect(jsonPath("$.data.content[1].name").value("Empresa SQL 2"));
    }

    @Test
    @Sql(scripts = "/scripts/seed-tenant.sql") // Carrega Tenant prévio via script SQL
    @DisplayName("Cenário 6: Deve falhar na buscar de um Tenant não existente")
    void shouldNotCreateTenant() throws Exception {
        CreateTenantRequest request = CreateTenantRequest.builder()
                .name("Guinchos")
                .cnpj("11222333000144")
                .build();

        mockMvc.perform(post("/api/v1/tenants")
                        .header("X-Admin-Api-Key", ADMIN_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].code").value("TNT-001"));
    }

    @Test
    @DisplayName("Cenário 7: Deve falhar na buscar de um Tenant não existente")
    void shouldNotFindTenant() throws Exception {
        String tenantId = "00000000-0000-0000-0000-000000000000";

        mockMvc.perform(get("/api/v1/tenants/" + tenantId)
                        .header("X-Admin-Api-Key", ADMIN_KEY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].code").value("TNT-002"));
    }

    @Test
    @DisplayName("Cenário 8: Deve retornar HTTP 500 com JSON padrão quando o banco de dados estiver fora do ar")
    void shouldReturn500WhenDatabaseIsUnavailable() throws Exception {
        // Arrange: Simula que qualquer chamada ao repositório lança uma exceção de falha de conexão com o banco
        doThrow(new DataAccessResourceFailureException("Connection refused to PostgreSQL server"))
                .when(tenantRepository).findAll(any(Pageable.class));

        mockMvc.perform(get("/api/v1/tenants")
                        .header("X-Admin-Api-Key", ADMIN_KEY))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.statusCode").value(500))
                .andExpect(jsonPath("$.message").value("Falha na comunicação com o banco de dados."))
                .andExpect(jsonPath("$.errors[0].code").value("SYS-501"))
                .andExpect(jsonPath("$.errors[0].message").value("Erro na comunicação com o banco de dados."));
    }
}
