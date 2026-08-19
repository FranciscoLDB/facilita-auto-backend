package com.fldb.facilita.auto.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fldb.facilita.auto.api.dto.service.type.CreateServiceTypeRequest;
import com.fldb.facilita.auto.api.dto.service.type.UpdateServiceTypeRequest;
import com.fldb.facilita.auto.domain.entity.ServiceType;
import com.fldb.facilita.auto.domain.entity.company.Tenant;
import com.fldb.facilita.auto.domain.repository.ServiceTypeRepository;
import com.fldb.facilita.auto.domain.repository.TenantRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static com.fldb.facilita.auto.util.TenantTestUtils.runInTenantContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ServiceTypeControllerIT {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoSpyBean
    private TenantRepository tenantRepository;

    @MockitoSpyBean
    private ServiceTypeRepository serviceTypeRepository;

    @Autowired
    private JwtTestUtil jwtTestUtil;

    private static final String ADMIN_KEY = "very-secret-key";
    private String TENANT_1_ID;
    private String TENANT_2_ID;
    private Map<String, List<ServiceType>> serviceTypeTenants;

    @BeforeAll
    void setUp() {
        TENANT_1_ID = tenantRepository.save(Tenant.builder()
                .companyName("Tenant 1")
                .taxId("12345678900001")
                .build()).getId().toString();

        TENANT_2_ID = tenantRepository.save(Tenant.builder()
                .companyName("Tenant 2")
                .taxId("12345678900002")
                .build()).getId().toString();

        runInTenantContext(UUID.fromString(TENANT_1_ID), () -> {
            serviceTypeRepository.save(ServiceType.builder()
                    .name("Service Type 1")
                    .description("Description for Service Type 1")
                    .tenantId(UUID.fromString(TENANT_1_ID))
                    .build());

            serviceTypeRepository.save(ServiceType.builder()
                    .name("Service Type 2")
                    .description("Description for Service Type 2")
                    .tenantId(UUID.fromString(TENANT_1_ID))
                    .build());
        });

        runInTenantContext(UUID.fromString(TENANT_2_ID), () -> {
            serviceTypeRepository.save(ServiceType.builder()
                    .name("Service Type 3")
                    .description("Description for Service Type 3")
                    .tenantId(UUID.fromString(TENANT_2_ID))
                    .build());
        });

        serviceTypeTenants = Map.of(
                TENANT_1_ID, runInTenantContext(UUID.fromString(TENANT_1_ID), () -> serviceTypeRepository.findAll()),
                TENANT_2_ID, runInTenantContext(UUID.fromString(TENANT_2_ID), () -> serviceTypeRepository.findAll())
        );
    }

    @AfterAll
    void clearData() {
        serviceTypeRepository.deleteAll();
        tenantRepository.deleteAll();
    }

    // ==========================================
    // POST /api/v1/service-types
    // ==========================================

    @Test
    @DisplayName("Cenário 1: Deve negar solicitação sem token e sem chave Admin")
    void shouldReturn401WhenNoTokenAndNoAdminKey() throws Exception {
        CreateServiceTypeRequest request = CreateServiceTypeRequest.builder()
                .name("Service Type 1")
                .description("Description for Service Type 1")
                .build();

        mockMvc.perform(post("/api/v1/service-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Cenário 2: Deve retornar HTTP 400 quando a validação falhar")
    void shouldReturn400WhenValidationFails() throws Exception {
        CreateServiceTypeRequest request = CreateServiceTypeRequest.builder()
                .name("") // Nome em branco (violando @NotBlank)
                .build();

        mockMvc.perform(post("/api/v1/service-types")
                        .header("X-Admin-Api-Key", ADMIN_KEY)
                        .header("X-Tenant-ID", TENANT_1_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    @DisplayName("Cenário 3: Deve cadastrar tipo de serviço com sucesso via X-Admin-Api-Key")
    void shouldCreateServiceTypeSuccessfullyWithAdminKey() throws Exception {
        CreateServiceTypeRequest request = CreateServiceTypeRequest.builder()
                .name("Carga de Bateria")
                .description("Descrição para Carga de Bateria")
                .build();

        mockMvc.perform(post("/api/v1/service-types")
                        .header("X-Admin-Api-Key", ADMIN_KEY)
                        .header("X-Tenant-ID", TENANT_1_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.message").value("Service type created successfully."))
                .andExpect(jsonPath("$.data.name").value("Carga de Bateria"))
                .andExpect(jsonPath("$.data.tenantId").value(TENANT_1_ID));
    }

    @Test
    @DisplayName("Cenário 4: Deve cadastrar tipo de serviço com sucesso via Token JWT")
    void shouldCreateServiceTypeSuccessfullyWithValidToken() throws Exception {
        String adminToken = jwtTestUtil.generateAdminTokenForTenant(UUID.fromString(TENANT_1_ID));
        CreateServiceTypeRequest request = CreateServiceTypeRequest.builder()
                .name("Reboque leve")
                .description("Descrição para Reboque leve")
                .build();

        mockMvc.perform(post("/api/v1/service-types")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.data.name").value("Reboque leve"))
                .andExpect(jsonPath("$.data.tenantId").value(TENANT_1_ID));
    }

    @Test
    @DisplayName("Cenário 5: Deve retornar HTTP 500 quando o banco de dados falhar")
    void shouldReturn500WhenDatabaseIsUnavailable() throws Exception {
        doThrow(new DataAccessResourceFailureException("Connection refused"))
                .when(serviceTypeRepository).save(any());

        CreateServiceTypeRequest request = CreateServiceTypeRequest.builder()
                .name("Reboque leve")
                .description("Descrição para Reboque leve")
                .build();

        mockMvc.perform(post("/api/v1/service-types")
                        .header("X-Admin-Api-Key", ADMIN_KEY)
                        .header("X-Tenant-ID", TENANT_1_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.statusCode").value(500));
    }

    // ==========================================
    // GET /api/v1/service-types
    // ==========================================

    @Test
    @DisplayName("Cenário 6: Deve listar tipos de serviço paginados do tenant do token JWT")
    void shouldListServiceTypesWithJwtToken() throws Exception {
        String adminToken = jwtTestUtil.generateAdminTokenForTenant(UUID.fromString(TENANT_1_ID));

        mockMvc.perform(get("/api/v1/service-types")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Service types retrieved successfully."))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[*].tenantId",
                        Matchers.everyItem(Matchers.is(TENANT_1_ID))));
    }

    // ==========================================
    // PATCH /api/v1/service-types/{id}
    // ==========================================

    @Test
    @DisplayName("Cenário 7: Deve atualizar parcialmente um tipo de serviço com sucesso (PATCH)")
    void shouldPatchServiceTypeSuccessfully() throws Exception {
        ServiceType existingServiceType = serviceTypeTenants.get(TENANT_1_ID).getFirst();
        String adminToken = jwtTestUtil.generateAdminTokenForTenant(UUID.fromString(TENANT_1_ID));

        UpdateServiceTypeRequest patchRequest = UpdateServiceTypeRequest.builder()
                .description("Nova observação adicionada")
                .build();

        mockMvc.perform(patch("/api/v1/service-types/{id}", existingServiceType.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Service type updated successfully."))
                .andExpect(jsonPath("$.data.name").value("Service Type 1"))
                .andExpect(jsonPath("$.data.description").value("Nova observação adicionada"));
    }

    @Test
    @DisplayName("Cenário 8: Deve retornar HTTP 404 ao tentar atualizar tipo de serviço inexistente")
    void shouldReturn404WhenUpdatingNonExistentServiceType() throws Exception {
        String adminToken = jwtTestUtil.generateAdminTokenForTenant(UUID.fromString(TENANT_1_ID));
        UUID randomId = UUID.randomUUID();

        UpdateServiceTypeRequest patchRequest = UpdateServiceTypeRequest.builder()
                .name("Non Existent")
                .build();

        mockMvc.perform(patch("/api/v1/service-types/{id}", randomId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Cenário 9: Não deve atualizar tipo de serviço pertencente a outro tenant")
    void shouldNotUpdateServiceTypeFromAnotherTenant() throws Exception {
        ServiceType tenant2ServiceType = serviceTypeTenants.get(TENANT_2_ID).getFirst();
        String tenant1Token = jwtTestUtil.generateAdminTokenForTenant(UUID.fromString(TENANT_1_ID));

        UpdateServiceTypeRequest patchRequest = UpdateServiceTypeRequest.builder()
                .name("Hack Name")
                .build();

        mockMvc.perform(patch("/api/v1/service-types/{id}", tenant2ServiceType.getId())
                        .header("Authorization", "Bearer " + tenant1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isNotFound());
    }

    // ==========================================
    // DELETE /api/v1/service-types/{id}
    // ==========================================

    @Test
    @DisplayName("Cenário 10: Deve deletar tipo de serviço com sucesso")
    void shouldDeleteServiceTypeSuccessfully() throws Exception {
        AtomicReference<UUID> serviceTypeId = new AtomicReference<>();

        runInTenantContext(UUID.fromString(TENANT_2_ID), () -> {
            ServiceType toDelete = serviceTypeRepository.save(ServiceType.builder()
                    .name("Tipo de Serviço Temporário")
                    .tenantId(UUID.fromString(TENANT_2_ID))
                    .build());
            serviceTypeId.set(toDelete.getId());
        });

        String adminToken = jwtTestUtil.generateAdminTokenForTenant(UUID.fromString(TENANT_2_ID));

        mockMvc.perform(delete("/api/v1/service-types/{id}", serviceTypeId.get())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        runInTenantContext(UUID.fromString(TENANT_2_ID), () -> {
            Optional<ServiceType> deleted = serviceTypeRepository.findById(serviceTypeId.get());
            assertThat(deleted).isEmpty();
        });
    }

    @Test
    @DisplayName("Cenário 11: Não deve deletar tipo de serviço de outro tenant")
    void shouldNotDeleteServiceTypeFromDifferentTenant() throws Exception {
        ServiceType tenant1ServiceType = serviceTypeTenants.get(TENANT_1_ID).getFirst();
        String tenant2Token = jwtTestUtil.generateAdminTokenForTenant(UUID.fromString(TENANT_2_ID));

        mockMvc.perform(delete("/api/v1/service-types/{id}", tenant1ServiceType.getId())
                        .header("Authorization", "Bearer " + tenant2Token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}