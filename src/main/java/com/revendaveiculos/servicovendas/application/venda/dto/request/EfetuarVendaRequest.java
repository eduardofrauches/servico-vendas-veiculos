package com.revendaveiculos.servicovendas.application.venda.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EfetuarVendaRequest(
        @NotNull(message = "veiculoId e obrigatorio") Long veiculoId,
        @NotBlank(message = "cpfComprador e obrigatorio") String cpfComprador
) {
}
