package com.revendaveiculos.servicovendas.application.venda.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record EfetuarVendaRequest(
        @NotNull(message = "veiculoId e obrigatorio") Long veiculoId,
        @NotBlank(message = "cpfComprador e obrigatorio") String cpfComprador,
        LocalDateTime dataVenda
) {
    /**
     * Data da venda e opcional: se o cliente nao informar, o servidor usa
     * o momento atual (LocalDateTime.now()) como data da venda, igual ao
     * comportamento anterior a este campo existir.
     */
    public EfetuarVendaRequest(Long veiculoId, String cpfComprador) {
        this(veiculoId, cpfComprador, null);
    }
}
