package com.fldb.facilita.auto.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fldb.facilita.auto.api.dto.pricing.table.CreatePricingTableRequest;
import com.fldb.facilita.auto.api.dto.pricing.table.UpdatePricingTableRequest;
import com.fldb.facilita.auto.domain.entity.InsuranceCompany;
import com.fldb.facilita.auto.domain.entity.PricingTable;
import com.fldb.facilita.auto.domain.entity.ServiceType;
import com.fldb.facilita.auto.domain.entity.company.Tenant;
import com.fldb.facilita.auto.domain.repository.InsuranceRepository;
import com.fldb.facilita.auto.domain.repository.PricingTableRepository;
import com.fldb.facilita.auto.domain.repository.ServiceTypeRepository;
import com.fldb.facilita.auto.domain.repository.TenantRepository;
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

import java.math.BigDecimal;
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
class PricingTableControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IntegrationTestHelper testHelper; // Injetando nosso helper

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoSpyBean
    private TenantRepository tenantRepository;
    @MockitoSpyBean
    private InsuranceRepository insuranceRepository;
    @MockitoSpyBean
    private ServiceTypeRepository serviceTypeRepository;
    @MockitoSpyBean
    private PricingTableRepository pricingTableRepository;

    @Autowired
    private JwtTestUtil jwtTestUtil;

    private static final String ADMIN_KEY = "very-secret-key";
    private String TENANT_1_ID;
    private String TENANT_2_ID;

    private InsuranceCompany insuranceCompanyTenant1;
    private ServiceType serviceType1Tenant1;
    private ServiceType serviceType2Tenant1;
    private InsuranceCompany insuranceCompanyTenant2;
    private ServiceType serviceType1Tenant2;

    private Map<String, List<PricingTable>> pricingTableTenants;

    @BeforeAll
    void setUp() {
        // Criando tenants dinâmicos sem risco de duplicidade de taxId/nome
        Tenant tenant1 = testHelper.createTenant("Tenant 1");
        Tenant tenant2 = testHelper.createTenant("Tenant 2");

        TENANT_1_ID = tenant1.getId().toString();
        TENANT_2_ID = tenant2.getId().toString();

        // Dados para o Tenant 1
        insuranceCompanyTenant1 = testHelper.createInsuranceCompany(tenant1.getId(), "Seguradora Porto");
        serviceType1Tenant1 = testHelper.createServiceType(tenant1.getId(), "Reboque Leve");
        serviceType2Tenant1 = testHelper.createServiceType(tenant1.getId(), "Reboque Pesado");
        testHelper.createPricingTable(tenant1.getId(), insuranceCompanyTenant1, serviceType1Tenant1);

        // Dados para o Tenant 2
        insuranceCompanyTenant2 = testHelper.createInsuranceCompany(tenant2.getId(), "Seguradora SulAmérica");
        serviceType1Tenant2 = testHelper.createServiceType(tenant2.getId(), "Troca de Pneu");
        testHelper.createPricingTable(tenant2.getId(), insuranceCompanyTenant2, serviceType1Tenant2);

        pricingTableTenants = Map.of(
                TENANT_1_ID, runInTenantContext(UUID.fromString(TENANT_1_ID), () -> pricingTableRepository.findAll()),
                TENANT_2_ID, runInTenantContext(UUID.fromString(TENANT_2_ID), () -> pricingTableRepository.findAll())
        );
    }

    @AfterAll
    void clearData() {
        pricingTableRepository.deleteAll();
        serviceTypeRepository.deleteAll();
        insuranceRepository.deleteAll();
        tenantRepository.deleteAll();
    }

    private CreatePricingTableRequest.CreatePricingTableRequestBuilder buildValidCreateRequest() {
        return CreatePricingTableRequest.builder()
                .insuranceCompanyId(insuranceCompanyTenant1.getId())
                .serviceTypeId(serviceType1Tenant1.getId())
                .baseFee(new BigDecimal("100.00"))
                .extraKmFee(new BigDecimal("2.50"))
                .dirtRoadKmFee(new BigDecimal("4.00"))
                .includedKmAllowance(20)
                .idleHourFee(new BigDecimal("30.00"))
                .workedHourFee(new BigDecimal("60.00"))
                .skateFee(new BigDecimal("40.00"))
                .nightShiftFee(new BigDecimal("20.00"));
    }

    // ==========================================
    // POST /api/v1/pricing-tables
    // ==========================================

    @Test
    @DisplayName("Cenário 1: Deve negar solicitação sem token e sem chave Admin")
    void shouldReturn401WhenNoTokenAndNoAdminKey() throws Exception {
        CreatePricingTableRequest request = buildValidCreateRequest()
                .insuranceCompanyId(insuranceCompanyTenant1.getId())
                .serviceTypeId(serviceType1Tenant1.getId())
                .build();

        mockMvc.perform(post("/api/v1/pricing-tables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Cenário 2: Deve retornar HTTP 400 quando a validação de campos obrigatórios/valores negativos falhar")
    void shouldReturn400WhenValidationFails() throws Exception {
        CreatePricingTableRequest request = CreatePricingTableRequest.builder()
                .insuranceCompanyId(insuranceCompanyTenant1.getId())
                .serviceTypeId(serviceType1Tenant1.getId())
                .baseFee(new BigDecimal("-10.00")) // Valor negativo violando @PositiveOrZero
                .extraKmFee(null)                  // Campo obrigatório violando @NotNull
                .build();

        mockMvc.perform(post("/api/v1/pricing-tables")
                        .header("X-Admin-Api-Key", ADMIN_KEY)
                        .header("X-Tenant-ID", TENANT_1_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    @DisplayName("Cenário 3: Deve retornar HTTP 404 quando a seguradora não for encontrada")
    void shouldReturn404WhenInsuranceCompanyNotFound() throws Exception {
        CreatePricingTableRequest request = buildValidCreateRequest()
                .insuranceCompanyId(UUID.randomUUID())
                .build();

        mockMvc.perform(post("/api/v1/pricing-tables")
                        .header("X-Admin-Api-Key", ADMIN_KEY)
                        .header("X-Tenant-ID", TENANT_1_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Cenário 4: Deve retornar HTTP 404 quando o tipo de serviço não for encontrado")
    void shouldReturn404WhenServiceTypeNotFound() throws Exception {
        CreatePricingTableRequest request = buildValidCreateRequest()
                .serviceTypeId(UUID.randomUUID())
                .build();

        mockMvc.perform(post("/api/v1/pricing-tables")
                        .header("X-Admin-Api-Key", ADMIN_KEY)
                        .header("X-Tenant-ID", TENANT_1_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Cenário 5: Não deve permitir associar seguradora de outro tenant")
    void shouldNotAllowInsuranceCompanyFromAnotherTenant() throws Exception {
        // Tentando criar no Tenant 1 usando a seguradora do Tenant 2
        CreatePricingTableRequest request = buildValidCreateRequest()
                .insuranceCompanyId(insuranceCompanyTenant2.getId())
                .build();

        mockMvc.perform(post("/api/v1/pricing-tables")
                        .header("X-Admin-Api-Key", ADMIN_KEY)
                        .header("X-Tenant-ID", TENANT_1_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Cenário 6: Deve cadastrar tabela de preço com sucesso via X-Admin-Api-Key")
    void shouldCreatePricingTableSuccessfullyWithAdminKey() throws Exception {
        ServiceType serviceType = testHelper.createServiceType(UUID.fromString(TENANT_1_ID), "Test Service Type");

        CreatePricingTableRequest request = buildValidCreateRequest()
                .insuranceCompanyId(insuranceCompanyTenant1.getId())
                .serviceTypeId(serviceType.getId())
                .build();

        mockMvc.perform(post("/api/v1/pricing-tables")
                        .header("X-Admin-Api-Key", ADMIN_KEY)
                        .header("X-Tenant-ID", TENANT_1_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.message").value("Pricing table created successfully."))
                .andExpect(jsonPath("$.data.baseFee").value(100.00))
                .andExpect(jsonPath("$.data.insuranceCompanyId").value(insuranceCompanyTenant1.getId().toString()))
                .andExpect(jsonPath("$.data.serviceTypeId").value(serviceType.getId().toString()))
                .andExpect(jsonPath("$.data.tenantId").value(TENANT_1_ID));
    }

    @Test
    @DisplayName("Cenário 7: Deve cadastrar tabela de preço com sucesso via Token JWT")
    void shouldCreatePricingTableSuccessfullyWithValidToken() throws Exception {
        String adminToken = jwtTestUtil.generateAdminTokenForTenant(UUID.fromString(TENANT_1_ID));
        ServiceType serviceType = testHelper.createServiceType(UUID.fromString(TENANT_1_ID), "Test Service Type");

        CreatePricingTableRequest request = buildValidCreateRequest()
                .insuranceCompanyId(insuranceCompanyTenant1.getId())
                .serviceTypeId(serviceType.getId())
                .build();

        mockMvc.perform(post("/api/v1/pricing-tables")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.data.tenantId").value(TENANT_1_ID));
    }

    @Test
    @DisplayName("Cenário 8: Deve retornar HTTP 500 quando o banco de dados falhar")
    void shouldReturn500WhenDatabaseIsUnavailable() throws Exception {
        doThrow(new DataAccessResourceFailureException("Connection refused"))
                .when(pricingTableRepository).save(any());

        CreatePricingTableRequest request = buildValidCreateRequest().build();

        mockMvc.perform(post("/api/v1/pricing-tables")
                        .header("X-Admin-Api-Key", ADMIN_KEY)
                        .header("X-Tenant-ID", TENANT_1_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.statusCode").value(500));
    }

    // ==========================================
    // GET /api/v1/pricing-tables
    // ==========================================

    @Test
    @DisplayName("Cenário 9: Deve listar tabelas de preço paginadas isoladas pelo tenant do token JWT")
    void shouldListPricingTablesWithJwtToken() throws Exception {
        String adminToken = jwtTestUtil.generateAdminTokenForTenant(UUID.fromString(TENANT_1_ID));

        mockMvc.perform(get("/api/v1/pricing-tables")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Pricing tables retrieved successfully."))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[*].tenantId",
                        Matchers.everyItem(Matchers.is(TENANT_1_ID))));
    }

    // ==========================================
    // PATCH /api/v1/pricing-tables/{id}
    // ==========================================

    @Test
    @DisplayName("Cenário 10: Deve atualizar parcialmente uma tabela de preço com sucesso (PATCH)")
    void shouldPatchPricingTableSuccessfully() throws Exception {
        PricingTable existingPricingTable = pricingTableTenants.get(TENANT_1_ID).getFirst();
        String adminToken = jwtTestUtil.generateAdminTokenForTenant(UUID.fromString(TENANT_1_ID));

        UpdatePricingTableRequest patchRequest = UpdatePricingTableRequest.builder()
                .baseFee(new BigDecimal("180.00"))
                .extraKmFee(new BigDecimal("4.50"))
                .build();

        mockMvc.perform(patch("/api/v1/pricing-tables/{id}", existingPricingTable.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Pricing table updated successfully."))
                .andExpect(jsonPath("$.data.baseFee").value(180.00))
                .andExpect(jsonPath("$.data.extraKmFee").value(4.50));
    }

    @Test
    @DisplayName("Cenário 11: Deve retornar HTTP 400 ao enviar valor negativo no PATCH")
    void shouldReturn400WhenPatchingWithNegativeValues() throws Exception {
        PricingTable existingPricingTable = pricingTableTenants.get(TENANT_1_ID).getFirst();
        String adminToken = jwtTestUtil.generateAdminTokenForTenant(UUID.fromString(TENANT_1_ID));

        UpdatePricingTableRequest patchRequest = UpdatePricingTableRequest.builder()
                .baseFee(new BigDecimal("-50.00"))
                .build();

        mockMvc.perform(patch("/api/v1/pricing-tables/{id}", existingPricingTable.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    @DisplayName("Cenário 12: Deve retornar HTTP 404 ao tentar atualizar tabela de preço inexistente")
    void shouldReturn404WhenUpdatingNonExistentPricingTable() throws Exception {
        String adminToken = jwtTestUtil.generateAdminTokenForTenant(UUID.fromString(TENANT_1_ID));

        UpdatePricingTableRequest patchRequest = UpdatePricingTableRequest.builder()
                .baseFee(new BigDecimal("200.00"))
                .build();

        mockMvc.perform(patch("/api/v1/pricing-tables/{id}", UUID.randomUUID())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Cenário 13: Não deve atualizar tabela de preço pertencente a outro tenant")
    void shouldNotUpdatePricingTableFromAnotherTenant() throws Exception {
        PricingTable tenant2PricingTable = pricingTableTenants.get(TENANT_2_ID).getFirst();
        String tenant1Token = jwtTestUtil.generateAdminTokenForTenant(UUID.fromString(TENANT_1_ID));

        UpdatePricingTableRequest patchRequest = UpdatePricingTableRequest.builder()
                .baseFee(new BigDecimal("999.00"))
                .build();

        mockMvc.perform(patch("/api/v1/pricing-tables/{id}", tenant2PricingTable.getId())
                        .header("Authorization", "Bearer " + tenant1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isNotFound());
    }

    // ==========================================
    // DELETE /api/v1/pricing-tables/{id}
    // ==========================================

    @Test
    @DisplayName("Cenário 14: Deve deletar tabela de preço com sucesso")
    void shouldDeletePricingTableSuccessfully() throws Exception {
        ServiceType serviceType = testHelper.createServiceType(UUID.fromString(TENANT_2_ID), "Service Type to Delete");
        AtomicReference<UUID> pricingTableId = new AtomicReference<>();

        runInTenantContext(UUID.fromString(TENANT_2_ID), () -> {
            PricingTable toDelete = pricingTableRepository.save(PricingTable.builder()
                    .tenantId(UUID.fromString(TENANT_2_ID))
                    .insuranceCompany(insuranceCompanyTenant2)
                    .serviceType(serviceType)
                    .baseFee(new BigDecimal("100.00"))
                    .extraKmFee(new BigDecimal("2.00"))
                    .dirtRoadKmFee(new BigDecimal("3.00"))
                    .includedKmAllowance(10)
                    .idleHourFee(new BigDecimal("20.00"))
                    .workedHourFee(new BigDecimal("40.00"))
                    .skateFee(new BigDecimal("30.00"))
                    .nightShiftFee(new BigDecimal("15.00"))
                    .build());
            pricingTableId.set(toDelete.getId());
        });

        String adminToken = jwtTestUtil.generateAdminTokenForTenant(UUID.fromString(TENANT_2_ID));

        mockMvc.perform(delete("/api/v1/pricing-tables/{id}", pricingTableId.get())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        runInTenantContext(UUID.fromString(TENANT_2_ID), () -> {
            Optional<PricingTable> deleted = pricingTableRepository.findById(pricingTableId.get());
            assertThat(deleted).isEmpty();
        });
    }

    @Test
    @DisplayName("Cenário 15: Não deve deletar tabela de preço de outro tenant")
    void shouldNotDeletePricingTableFromDifferentTenant() throws Exception {
        PricingTable tenant1PricingTable = pricingTableTenants.get(TENANT_1_ID).getFirst();
        String tenant2Token = jwtTestUtil.generateAdminTokenForTenant(UUID.fromString(TENANT_2_ID));

        mockMvc.perform(delete("/api/v1/pricing-tables/{id}", tenant1PricingTable.getId())
                        .header("Authorization", "Bearer " + tenant2Token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Cenário 16: Deve retornar HTTP 404 ao tentar deletar tabela de preço inexistente")
    void shouldReturn404WhenDeletingNonExistentPricingTable() throws Exception {
        String adminToken = jwtTestUtil.generateAdminTokenForTenant(UUID.fromString(TENANT_1_ID));

        mockMvc.perform(delete("/api/v1/pricing-tables/{id}", UUID.randomUUID())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}