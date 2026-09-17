package com.revendaveiculos.servicovendas.application.venda.port.out;

import com.revendaveiculos.servicovendas.domain.model.venda.Venda;

/** Gateway HTTP para o sistema-principal-veiculos, notificado ao final de uma venda. */
public interface SistemaPrincipalPort {

    void notificarResultadoVenda(Venda venda);
}
