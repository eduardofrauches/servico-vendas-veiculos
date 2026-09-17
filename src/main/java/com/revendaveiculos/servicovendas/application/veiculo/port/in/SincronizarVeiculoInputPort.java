package com.revendaveiculos.servicovendas.application.veiculo.port.in;

import com.revendaveiculos.servicovendas.application.veiculo.dto.request.SincronizarVeiculoRequest;
import com.revendaveiculos.servicovendas.application.veiculo.dto.response.VeiculoResponse;

public interface SincronizarVeiculoInputPort {

    VeiculoResponse sincronizar(SincronizarVeiculoRequest request);
}
