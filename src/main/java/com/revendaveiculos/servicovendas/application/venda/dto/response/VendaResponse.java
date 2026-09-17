package com.revendaveiculos.servicovendas.application.venda.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VendaResponse(
        Long id,
        Long veiculoId,
        String cpfComprador,
        LocalDateTime dataVenda,
        BigDecimal valorVenda,
        String status,
        String codigoPagamento
) {
}
