package com.fldb.facilita.auto.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fldb.facilita.auto.api.dto.AddressDto;
import com.fldb.facilita.auto.api.dto.yard.CreateYardRequest;
import com.fldb.facilita.auto.api.dto.yard.UpdateYardRequest;
import com.fldb.facilita.auto.domain.entity.company.Tenant;
import com.fldb.facilita.auto.domain.entity.company.Yard;
import com.fldb.facilita.auto.domain.repository.TenantRepository;
import com.fldb.facilita.auto.domain.repository.YardRepository;
import com.fldb.facilita.auto.util.IntegrationTestHelper;
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
class YardControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IntegrationTestHelper testHelper;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoSpyBean
    private TenantRepository tenantRepository;

    @MockitoSpyBean
    private YardRepository yardRepository;

    @Autowired
    private JwtTestUtil jwtTestUtil;

    private static final String ADMIN_KEY = "very-secret-key";
    private String TENANT_1_ID;
    private String TENANT_2_ID;

    private Map<String, List<Yard>> yardTenants;

    @BeforeAll
    void setUp() {
        Tenant tenant1 = testHelper.createTenant("Tenant 1");
        Tenant tenant2 = testHelper.createTenant("Tenant 2");

        TENANT_1_ID = tenant1.getId().toString();
        TENANT_2_ID = tenant2.getId().toString();

        testHelper.createYard(tenant1.getId(), "Pátio Central T1", testHelper.createAddressDto(), null, true);
        testHelper.createYard(tenant2.getId(), "Pátio Matriz T2", testHelper.createAddressDto(), null, true);

        yardTenants = Map.of(
                TENANT_1_ID, runInTenantContext(UUID.fromString(TENANT_1_ID), () -> yardRepository.findAll()),
                TENANT_2_ID, runInTenantContext(UUID.fromString(TENANT_2_ID), () -> yardRepository.findAll())
        );
    }

    @AfterAll
    void clearData() {
        yardRepository.deleteAll();
        tenantRepository.deleteAll();
    }

    private CreateYardRequest buildValidCreateRequest(String name) {
        return CreateYardRequest.builder()
                .name(name)
                .address(testHelper.createAddressDto())
                .managerId(null)
                .isActive(true)
                .build();
    }

    private CreateYardRequest buildValidCreateRequest() {
        return buildValidCreateRequest("Novo Pátio de Teste");
    }

    // ==========================================
    // POST /api/v1/yards
    // ==========================================

    @Test
    @DisplayName("Cenário 1: Deve negar solicitação sem token e sem chave Admin")
    void shouldReturn401WhenNoTokenAndNoAdminKey() throws Exception {
        CreateYardRequest request = buildValidCreateRequest();

        mockMvc.perform(post("/api/v1/yards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Cenário 2: Deve retornar HTTP 400 quando a validação de campos obrigatórios falhar")
    void shouldReturn400WhenValidationFails() throws Exception {
        CreateYardRequest request = CreateYardRequest.builder()
                .name("")      // Violando @NotBlank
                .address(null) // Violando @NotNull
                .build();

        mockMvc.perform(post("/api/v1/yards")
                        .header("X-Admin-Api-Key", ADMIN_KEY)
                        .header("X-Tenant-ID", TENANT_1_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    @DisplayName("Cenário 3: Deve cadastrar pátio com sucesso via X-Admin-Api-Key")
    void shouldCreateYardSuccessfullyWithAdminKey() throws Exception {
        CreateYardRequest request = buildValidCreateRequest("Pátio Novo Admin");

        mockMvc.perform(post("/api/v1/yards")
                        .header("X-Admin-Api-Key", ADMIN_KEY)
                        .header("X-Tenant-ID", TENANT_1_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.message").value("Yard created successfully."))
                .andExpect(jsonPath("$.data.address.street").value(request.getAddress().getStreet()))
                .andExpect(jsonPath("$.data.tenantId").value(TENANT_1_ID));
    }

    @Test
    @DisplayName("Cenário 4: Deve cadastrar pátio com sucesso via Token JWT")
    void shouldCreateYardSuccessfullyWithValidToken() throws Exception {
        String adminToken = jwtTestUtil.generateAdminTokenForTenant(UUID.fromString(TENANT_1_ID));
        CreateYardRequest request = buildValidCreateRequest("Pátio Novo JWT");

        mockMvc.perform(post("/api/v1/yards")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.data.tenantId").value(TENANT_1_ID));
    }

    @Test
    @DisplayName("Cenário 5: Deve retornar HTTP 500 quando o banco de dados falhar")
    void shouldReturn500WhenDatabaseIsUnavailable() throws Exception {
        doThrow(new DataAccessResourceFailureException("Connection refused"))
                .when(yardRepository).save(any());

        CreateYardRequest request = buildValidCreateRequest();

        mockMvc.perform(post("/api/v1/yards")
                        .header("X-Admin-Api-Key", ADMIN_KEY)
                        .header("X-Tenant-ID", TENANT_1_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.statusCode").value(500));
    }

    // ==========================================
    // GET /api/v1/yards
    // ==========================================

    @Test
    @DisplayName("Cenário 6: Deve listar pátios paginados isolados pelo tenant do token JWT")
    void shouldListYardsWithJwtToken() throws Exception {
        String adminToken = jwtTestUtil.generateAdminTokenForTenant(UUID.fromString(TENANT_1_ID));

        mockMvc.perform(get("/api/v1/yards")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Yards retrieved successfully."))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[*].tenantId",
                        Matchers.everyItem(Matchers.is(TENANT_1_ID))));
    }

    // ==========================================
    // PATCH /api/v1/yards/{id}
    // ==========================================

    @Test
    @DisplayName("Cenário 7: Deve atualizar parcialmente um pátio com sucesso (PATCH)")
    void shouldPatchYardSuccessfully() throws Exception {
        Yard existingYard = yardTenants.get(TENANT_1_ID).getFirst();
        String adminToken = jwtTestUtil.generateAdminTokenForTenant(UUID.fromString(TENANT_1_ID));

        AddressDto newAddress = testHelper.createAddressDto("Av. Sete de Setembro", "Batel", "Curitiba", "PR");
        UpdateYardRequest patchRequest = UpdateYardRequest.builder()
                .name("Pátio Central Atualizado")
                .address(newAddress)
                .build();

        mockMvc.perform(patch("/api/v1/yards/{id}", existingYard.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Yard updated successfully."))
                .andExpect(jsonPath("$.data.name").value("Pátio Central Atualizado"))
                .andExpect(jsonPath("$.data.address.street").value("Av. Sete de Setembro"));
    }

    @Test
    @DisplayName("Cenário 8: Deve retornar HTTP 400 ao enviar nome excedendo limite de caracteres no PATCH")
    void shouldReturn400WhenPatchingWithInvalidData() throws Exception {
        Yard existingYard = yardTenants.get(TENANT_1_ID).getFirst();
        String adminToken = jwtTestUtil.generateAdminTokenForTenant(UUID.fromString(TENANT_1_ID));

        UpdateYardRequest patchRequest = UpdateYardRequest.builder()
                .name("A".repeat(101))
                .build();

        mockMvc.perform(patch("/api/v1/yards/{id}", existingYard.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    @DisplayName("Cenário 9: Deve retornar HTTP 404 ao tentar atualizar pátio inexistente")
    void shouldReturn404WhenUpdatingNonExistentYard() throws Exception {
        String adminToken = jwtTestUtil.generateAdminTokenForTenant(UUID.fromString(TENANT_1_ID));

        UpdateYardRequest patchRequest = UpdateYardRequest.builder()
                .name("Pátio Fantasma")
                .build();

        mockMvc.perform(patch("/api/v1/yards/{id}", UUID.randomUUID())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Cenário 10: Não deve atualizar pátio pertencente a outro tenant")
    void shouldNotUpdateYardFromAnotherTenant() throws Exception {
        Yard tenant2Yard = yardTenants.get(TENANT_2_ID).getFirst();
        String tenant1Token = jwtTestUtil.generateAdminTokenForTenant(UUID.fromString(TENANT_1_ID));

        UpdateYardRequest patchRequest = UpdateYardRequest.builder()
                .name("Tentativa de Invasão")
                .build();

        mockMvc.perform(patch("/api/v1/yards/{id}", tenant2Yard.getId())
                        .header("Authorization", "Bearer " + tenant1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isNotFound());
    }

    // ==========================================
    // DELETE /api/v1/yards/{id}
    // ==========================================

    @Test
    @DisplayName("Cenário 11: Deve deletar pátio com sucesso")
    void shouldDeleteYardSuccessfully() throws Exception {
        Yard toDelete = testHelper.createYard(
                UUID.fromString(TENANT_2_ID),
                "Pátio Para Deletar",
                testHelper.createAddressDto(),
                null,
                true
        );
        String adminToken = jwtTestUtil.generateAdminTokenForTenant(UUID.fromString(TENANT_2_ID));

        mockMvc.perform(delete("/api/v1/yards/{id}", toDelete.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        runInTenantContext(UUID.fromString(TENANT_2_ID), () -> {
            Optional<Yard> deleted = yardRepository.findById(toDelete.getId());
            assertThat(deleted).isEmpty();
        });
    }

    @Test
    @DisplayName("Cenário 12: Não deve deletar pátio de outro tenant")
    void shouldNotDeleteYardFromDifferentTenant() throws Exception {
        Yard tenant1Yard = yardTenants.get(TENANT_1_ID).getFirst();
        String tenant2Token = jwtTestUtil.generateAdminTokenForTenant(UUID.fromString(TENANT_2_ID));

        mockMvc.perform(delete("/api/v1/yards/{id}", tenant1Yard.getId())
                        .header("Authorization", "Bearer " + tenant2Token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Cenário 13: Deve retornar HTTP 404 ao tentar deletar pátio inexistente")
    void shouldReturn404WhenDeletingNonExistentYard() throws Exception {
        String adminToken = jwtTestUtil.generateAdminTokenForTenant(UUID.fromString(TENANT_1_ID));

        mockMvc.perform(delete("/api/v1/yards/{id}", UUID.randomUUID())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}