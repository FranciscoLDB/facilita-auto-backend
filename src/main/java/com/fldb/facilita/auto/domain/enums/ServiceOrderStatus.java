package com.fldb.facilita.auto.domain.enums;

public enum ServiceOrderStatus {
    PENDING,                  // Pendente
    EN_ROUTE_TO_ORIGIN,       // Em rota indo para a origem
    ON_SITE_ORIGIN,           // Na origem
    EN_ROUTE_TO_DESTINATION,  // Em rota indo para o destino
    IN_YARD,                  // Na base
    COMPLETED,                // Concluído
    SCHEDULED,                // Agendado
    CANCELLED                 // Cancelado
}
