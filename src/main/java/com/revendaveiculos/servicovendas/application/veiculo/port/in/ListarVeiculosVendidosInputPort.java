package com.revendaveiculos.servicovendas.application.veiculo.port.in;

import com.revendaveiculos.servicovendas.application.veiculo.dto.response.VeiculoResponse;

import java.util.List;

public interface ListarVeiculosVendidosInputPort {

    List<VeiculoResponse> listar();
}
