package com.revendaveiculos.servicovendas.application.veiculo.port.in;

import com.revendaveiculos.servicovendas.domain.model.veiculo.Veiculo;

import java.util.List;

public interface ListarVeiculosVendidosInputPort {

    List<Veiculo> listar();
}
