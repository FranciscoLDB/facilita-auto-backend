package com.fldb.facilita.auto.domain.service;

import com.fldb.facilita.auto.api.dto.tenant.CreateTenantRequest;
import com.fldb.facilita.auto.api.dto.tenant.TenantResponse;
import com.fldb.facilita.auto.api.exception.BusinessException;
import com.fldb.facilita.auto.domain.entity.Tenant;
import com.fldb.facilita.auto.domain.repository.TenantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private TenantService tenantService;

    @Test
    @DisplayName("Deve criar um tenant com sucesso quando os dados forem válidos")
    void shouldCreateTenantSuccessfully() {
        // Arrange (Preparação)
        CreateTenantRequest request = CreateTenantRequest.builder()
                .name("Empresa Teste")
                .cnpj("12.345.678/0001-99")
                .build();

        when(tenantRepository.existsByTaxId("12345678000199")).thenReturn(false);
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(invocation -> {
            Tenant t = invocation.getArgument(0);
            t.setIsActive(true);
            return t;
        });

        // Act (Ação)
        TenantResponse response = tenantService.create(request);

        // Assert (Validação)
        assertNotNull(response);
        assertEquals("Empresa Teste", response.getName());
        assertEquals("12345678000199", response.getCnpj());
        assertTrue(response.getActive());

        verify(tenantRepository, times(1)).save(any(Tenant.class));
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao tentar cadastrar CNPJ duplicado")
    void shouldThrowExceptionWhenCnpjAlreadyExists() {
        // Arrange
        CreateTenantRequest request = CreateTenantRequest.builder()
                .name("Empresa Teste")
                .cnpj("12.345.678/0001-99")
                .build();

        when(tenantRepository.existsByTaxId("12345678000199")).thenReturn(true);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
                () -> tenantService.create(request));

        assertEquals("Já existe um tenant cadastrado com este CNPJ.", exception.getMessage());
        verify(tenantRepository, never()).save(any(Tenant.class));
    }
}