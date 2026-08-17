package com.fldb.facilita.auto.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fldb.facilita.auto.api.dto.insurance.CreateInsuranceRequest;
import com.fldb.facilita.auto.api.dto.insurance.UpdateInsuranceRequest;
import com.fldb.facilita.auto.domain.entity.InsuranceCompany;
import com.fldb.facilita.auto.domain.entity.Tenant;
import com.fldb.facilita.auto.domain.repository.InsuranceRepository;
import com.fldb.facilita.auto.domain.repository.TenantRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InsuranceControllerIT {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoSpyBean
    private TenantRepository tenantRepository;

    @MockitoSpyBean
    private InsuranceRepository insuranceRepository;

    @Autowired
    private JwtTestUtil jwtTestUtil;

    private static final String ADMIN_KEY = "very-secret-key";
    private String TENANT_1_ID;
    private String TENANT_2_ID;
    private Map<String, List<InsuranceCompany>> insuranceTenants;

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
            insuranceRepository.save(InsuranceCompany.builder()
                    .name("Porto Seguro")
                    .baseCode("PS001")
                    .operationalSystemUrl("https://porto.com")
                    .tenantId(UUID.fromString(TENANT_1_ID))
                    .build());

            insuranceRepository.save(InsuranceCompany.builder()
                    .name("Azul Seguros")
                    .baseCode("AZ002")
                    .operationalSystemUrl("https://azul.com")
                    .tenantId(UUID.fromString(TENANT_1_ID))
                    .build());
        });

        runInTenantContext(UUID.fromString(TENANT_2_ID), () -> {
            insuranceRepository.save(InsuranceCompany.builder()
                    .name("Mapfre Seguros")
                    .baseCode("MF003")
                    .operationalSystemUrl("https://mapfre.com")
                    .tenantId(UUID.fromString(TENANT_2_ID))
                    .build());
        });

        insuranceTenants = Map.of(
                TENANT_1_ID, runInTenantContext(UUID.fromString(TENANT_1_ID), () -> insuranceRepository.findAll()),
                TENANT_2_ID, runInTenantContext(UUID.fromString(TENANT_2_ID), () -> insuranceRepository.findAll())
        );
    }

    @AfterAll
    void clearData() {
        insuranceRepository.deleteAll();
        tenantRepository.deleteAll();
    }

    // ==========================================
    // POST /api/v1/insurances
    // ==========================================

    @Test
    @DisplayName("Cenário 1: Deve negar solicitação sem token e sem chave Admin")
    void shouldReturn401WhenNoTokenAndNoAdminKey() throws Exception {
        CreateInsuranceRequest request = CreateInsuranceRequest.builder()
                .name("Tokio Marine")
                .baseCode("TM004")
                .build();

        mockMvc.perform(post("/api/v1/insurances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Cenário 2: Deve retornar HTTP 400 quando a validação falhar")
    void shouldReturn400WhenValidationFails() throws Exception {
        CreateInsuranceRequest request = CreateInsuranceRequest.builder()
                .name("") // Nome em branco (violando @NotBlank)
                .build();

        mockMvc.perform(post("/api/v1/insurances")
                        .header("X-Admin-Api-Key", ADMIN_KEY)
                        .header("X-Tenant-ID", TENANT_1_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    @DisplayName("Cenário 3: Deve cadastrar seguradora com sucesso via X-Admin-Api-Key")
    void shouldCreateInsuranceSuccessfullyWithAdminKey() throws Exception {
        CreateInsuranceRequest request = CreateInsuranceRequest.builder()
                .name("SulAmérica Seguros")
                .baseCode("SA005")
                .operationalSystemUrl("https://sulamerica.com")
                .build();

        mockMvc.perform(post("/api/v1/insurances")
                        .header("X-Admin-Api-Key", ADMIN_KEY)
                        .header("X-Tenant-ID", TENANT_1_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.message").value("Insurance created successfully."))
                .andExpect(jsonPath("$.data.name").value("SulAmérica Seguros"))
                .andExpect(jsonPath("$.data.tenantId").value(TENANT_1_ID));
    }

    @Test
    @DisplayName("Cenário 4: Deve cadastrar seguradora com sucesso via Token JWT")
    void shouldCreateInsuranceSuccessfullyWithValidToken() throws Exception {
        String adminToken = jwtTestUtil.generateAdminTokenForTenant(UUID.fromString(TENANT_1_ID));
        CreateInsuranceRequest request = CreateInsuranceRequest.builder()
                .name("Allianz Seguros")
                .baseCode("AL006")
                .build();

        mockMvc.perform(post("/api/v1/insurances")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.data.name").value("Allianz Seguros"))
                .andExpect(jsonPath("$.data.tenantId").value(TENANT_1_ID));
    }

    @Test
    @DisplayName("Cenário 5: Deve retornar HTTP 500 quando o banco de dados falhar")
    void shouldReturn500WhenDatabaseIsUnavailable() throws Exception {
        doThrow(new DataAccessResourceFailureException("Connection refused"))
                .when(insuranceRepository).save(any());

        CreateInsuranceRequest request = CreateInsuranceRequest.builder()
                .name("Bradesco Seguros")
                .baseCode("BS007")
                .build();

        mockMvc.perform(post("/api/v1/insurances")
                        .header("X-Admin-Api-Key", ADMIN_KEY)
                        .header("X-Tenant-ID", TENANT_1_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.statusCode").value(500));
    }

    // ==========================================
    // GET /api/v1/insurances
    // ==========================================

    @Test
    @DisplayName("Cenário 6: Deve listar seguradoras paginadas do tenant do token JWT")
    void shouldListInsurancesWithJwtToken() throws Exception {
        String adminToken = jwtTestUtil.generateAdminTokenForTenant(UUID.fromString(TENANT_1_ID));

        mockMvc.perform(get("/api/v1/insurances")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Insurance companies retrieved successfully."))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[*].tenantId",
                        Matchers.everyItem(Matchers.is(TENANT_1_ID))));
    }

    // ==========================================
    // PATCH /api/v1/insurances/{id}
    // ==========================================

    @Test
    @DisplayName("Cenário 7: Deve atualizar parcialmente uma seguradora com sucesso (PATCH)")
    void shouldPatchInsuranceSuccessfully() throws Exception {
        InsuranceCompany existingInsurance = insuranceTenants.get(TENANT_1_ID).getFirst();
        String adminToken = jwtTestUtil.generateAdminTokenForTenant(UUID.fromString(TENANT_1_ID));

        UpdateInsuranceRequest patchRequest = UpdateInsuranceRequest.builder()
                .name("Porto Seguro Updated")
                .notes("Nova observação adicionada")
                .build();

        mockMvc.perform(patch("/api/v1/insurances/{id}", existingInsurance.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Insurance updated successfully."))
                .andExpect(jsonPath("$.data.name").value("Porto Seguro Updated"))
                .andExpect(jsonPath("$.data.notes").value("Nova observação adicionada"))
                .andExpect(jsonPath("$.data.baseCode").value(existingInsurance.getBaseCode())); // Mantém o valor antigo
    }

    @Test
    @DisplayName("Cenário 8: Deve retornar HTTP 404 ao tentar atualizar seguradora inexistente")
    void shouldReturn404WhenUpdatingNonExistentInsurance() throws Exception {
        String adminToken = jwtTestUtil.generateAdminTokenForTenant(UUID.fromString(TENANT_1_ID));
        UUID randomId = UUID.randomUUID();

        UpdateInsuranceRequest patchRequest = UpdateInsuranceRequest.builder()
                .name("Non Existent")
                .build();

        mockMvc.perform(patch("/api/v1/insurances/{id}", randomId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Cenário 9: Não deve atualizar seguradora pertencente a outro tenant")
    void shouldNotUpdateInsuranceFromAnotherTenant() throws Exception {
        InsuranceCompany tenant2Insurance = insuranceTenants.get(TENANT_2_ID).getFirst();
        String tenant1Token = jwtTestUtil.generateAdminTokenForTenant(UUID.fromString(TENANT_1_ID));

        UpdateInsuranceRequest patchRequest = UpdateInsuranceRequest.builder()
                .name("Hack Name")
                .build();

        mockMvc.perform(patch("/api/v1/insurances/{id}", tenant2Insurance.getId())
                        .header("Authorization", "Bearer " + tenant1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isNotFound());
    }

    // ==========================================
    // DELETE /api/v1/insurances/{id}
    // ==========================================

    @Test
    @DisplayName("Cenário 10: Deve deletar seguradora com sucesso")
    void shouldDeleteInsuranceSuccessfully() throws Exception {
        AtomicReference<UUID> insuranceId = new AtomicReference<>();

        runInTenantContext(UUID.fromString(TENANT_2_ID), () -> {
            InsuranceCompany toDelete = insuranceRepository.save(InsuranceCompany.builder()
                    .name("Seguradora Temporária")
                    .tenantId(UUID.fromString(TENANT_2_ID))
                    .build());
            insuranceId.set(toDelete.getId());
        });

        String adminToken = jwtTestUtil.generateAdminTokenForTenant(UUID.fromString(TENANT_2_ID));

        mockMvc.perform(delete("/api/v1/insurances/{id}", insuranceId.get())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        runInTenantContext(UUID.fromString(TENANT_2_ID), () -> {
            Optional<InsuranceCompany> deleted = insuranceRepository.findById(insuranceId.get());
            assertThat(deleted).isEmpty();
        });
    }

    @Test
    @DisplayName("Cenário 11: Não deve deletar seguradora de outro tenant")
    void shouldNotDeleteInsuranceFromDifferentTenant() throws Exception {
        InsuranceCompany tenant1Insurance = insuranceTenants.get(TENANT_1_ID).getFirst();
        String tenant2Token = jwtTestUtil.generateAdminTokenForTenant(UUID.fromString(TENANT_2_ID));

        mockMvc.perform(delete("/api/v1/insurances/{id}", tenant1Insurance.getId())
                        .header("Authorization", "Bearer " + tenant2Token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}