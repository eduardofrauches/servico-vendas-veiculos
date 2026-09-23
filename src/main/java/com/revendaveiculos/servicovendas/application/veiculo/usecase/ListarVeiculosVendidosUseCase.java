package com.revendaveiculos.servicovendas.application.veiculo.usecase;

import com.revendaveiculos.servicovendas.application.veiculo.port.in.ListarVeiculosVendidosInputPort;
import com.revendaveiculos.servicovendas.application.veiculo.port.out.VeiculoRepositoryPort;
import com.revendaveiculos.servicovendas.domain.model.veiculo.Veiculo;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class ListarVeiculosVendidosUseCase implements ListarVeiculosVendidosInputPort {

    private final VeiculoRepositoryPort veiculoRepositoryPort;

    public ListarVeiculosVendidosUseCase(VeiculoRepositoryPort veiculoRepositoryPort) {
        this.veiculoRepositoryPort = veiculoRepositoryPort;
    }

    @Override
    public List<Veiculo> listar() {
        return veiculoRepositoryPort.listarVendidos().stream()
                .sorted(Comparator.comparing(veiculo -> veiculo.getPreco().valor()))
                .toList();
    }
}
