package com.revendaveiculos.servicovendas.application.veiculo.port.in;

import com.revendaveiculos.servicovendas.application.veiculo.dto.request.SincronizarVeiculoRequest;
import com.revendaveiculos.servicovendas.domain.model.veiculo.Veiculo;

public interface SincronizarVeiculoInputPort {

    Veiculo sincronizar(SincronizarVeiculoRequest request);
}
