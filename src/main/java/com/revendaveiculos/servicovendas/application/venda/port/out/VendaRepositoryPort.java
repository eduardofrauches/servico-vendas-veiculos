package com.revendaveiculos.servicovendas.application.venda.port.out;

import com.revendaveiculos.servicovendas.domain.model.venda.Venda;

import java.util.Optional;

public interface VendaRepositoryPort {

    Venda salvar(Venda venda);

    Optional<Venda> buscarPorCodigoPagamento(String codigoPagamento);
}
