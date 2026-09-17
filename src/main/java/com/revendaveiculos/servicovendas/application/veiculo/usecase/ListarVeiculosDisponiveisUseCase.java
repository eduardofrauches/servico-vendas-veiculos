package com.revendaveiculos.servicovendas.application.veiculo.usecase;

import com.revendaveiculos.servicovendas.application.veiculo.dto.response.VeiculoResponse;
import com.revendaveiculos.servicovendas.application.veiculo.mapper.VeiculoMapper;
import com.revendaveiculos.servicovendas.application.veiculo.port.in.ListarVeiculosDisponiveisInputPort;
import com.revendaveiculos.servicovendas.application.veiculo.port.out.VeiculoRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListarVeiculosDisponiveisUseCase implements ListarVeiculosDisponiveisInputPort {

    private final VeiculoRepositoryPort veiculoRepositoryPort;
    private final VeiculoMapper veiculoMapper;

    public ListarVeiculosDisponiveisUseCase(VeiculoRepositoryPort veiculoRepositoryPort, VeiculoMapper veiculoMapper) {
        this.veiculoRepositoryPort = veiculoRepositoryPort;
        this.veiculoMapper = veiculoMapper;
    }

    @Override
    public List<VeiculoResponse> listar() {
        return veiculoRepositoryPort.listarDisponiveis().stream()
                .map(veiculoMapper::paraResponse)
                .toList();
    }
}
