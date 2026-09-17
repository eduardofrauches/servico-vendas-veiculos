package com.revendaveiculos.servicovendas.application.venda.dto.request;

import com.revendaveiculos.servicovendas.domain.model.venda.ResultadoPagamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Payload enviado pela entidade de pagamento externa (simulada). */
public record WebhookPagamentoRequest(
        @NotBlank(message = "codigoPagamento e obrigatorio") String codigoPagamento,
        @NotNull(message = "resultado e obrigatorio") ResultadoPagamento resultado
) {
}
