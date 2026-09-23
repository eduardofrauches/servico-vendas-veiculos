package com.revendaveiculos.servicovendas.application.veiculo.usecase;

import com.revendaveiculos.servicovendas.application.veiculo.port.in.ListarVeiculosDisponiveisInputPort;
import com.revendaveiculos.servicovendas.application.veiculo.port.out.VeiculoRepositoryPort;
import com.revendaveiculos.servicovendas.domain.model.veiculo.Veiculo;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class ListarVeiculosDisponiveisUseCase implements ListarVeiculosDisponiveisInputPort {

    private final VeiculoRepositoryPort veiculoRepositoryPort;

    public ListarVeiculosDisponiveisUseCase(VeiculoRepositoryPort veiculoRepositoryPort) {
        this.veiculoRepositoryPort = veiculoRepositoryPort;
    }

    @Override
    public List<Veiculo> listar() {
        return veiculoRepositoryPort.listarDisponiveis().stream()
                .sorted(Comparator.comparing(veiculo -> veiculo.getPreco().valor()))
                .toList();
    }
}
