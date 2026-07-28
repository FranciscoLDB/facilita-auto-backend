package com.fldb.facilita.auto.api.exception;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ErrorCode {

    // Tenant
    TENANT_ALREADY_EXISTS("TNT-001", "Já existe um tenant cadastrado com este CNPJ."),
    TENANT_NOT_FOUND("TNT-002", "Tenant não encontrado."),

    INTERNAL_ERROR("SYS-500", "Erro interno no servidor");

    @JsonValue
    private final String code;
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
}
