package com.revendaveiculos.servicovendas.application.veiculo.dto.request;

import com.revendaveiculos.servicovendas.domain.model.veiculo.StatusVeiculo;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/** Payload enviado pelo sistema-principal-veiculos a cada cadastro/edicao. */
public record SincronizarVeiculoRequest(
        @NotNull(message = "id e obrigatorio") Long id,
        @NotBlank(message = "marca e obrigatoria") String marca,
        @NotBlank(message = "modelo e obrigatorio") String modelo,
        @NotNull(message = "ano e obrigatorio") @Min(value = 1900, message = "ano invalido") Integer ano,
        @NotBlank(message = "cor e obrigatoria") String cor,
        @NotNull(message = "preco e obrigatorio") @Positive(message = "preco deve ser maior que zero") BigDecimal preco,
        @NotNull(message = "status e obrigatorio") StatusVeiculo status
) {
}
