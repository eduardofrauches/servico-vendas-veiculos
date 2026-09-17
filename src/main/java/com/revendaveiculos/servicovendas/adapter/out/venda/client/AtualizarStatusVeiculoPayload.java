package com.revendaveiculos.servicovendas.adapter.out.venda.client;

/** Payload enviado ao endpoint PATCH /veiculos/{id}/status do sistema-principal-veiculos. */
public record AtualizarStatusVeiculoPayload(
        String status
) {
}
