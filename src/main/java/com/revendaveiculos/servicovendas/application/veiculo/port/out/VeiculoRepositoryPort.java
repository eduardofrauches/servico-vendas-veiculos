package com.revendaveiculos.servicovendas.application.veiculo.port.out;

import com.revendaveiculos.servicovendas.domain.model.veiculo.Veiculo;

import java.util.List;
import java.util.Optional;

public interface VeiculoRepositoryPort {

    Veiculo salvar(Veiculo veiculo);

    Optional<Veiculo> buscarPorId(Long id);

    List<Veiculo> listarDisponiveis();

    List<Veiculo> listarVendidos();
}
