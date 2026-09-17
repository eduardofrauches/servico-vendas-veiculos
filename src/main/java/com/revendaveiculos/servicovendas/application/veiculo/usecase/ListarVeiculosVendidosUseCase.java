package com.revendaveiculos.servicovendas.application.veiculo.usecase;

import com.revendaveiculos.servicovendas.application.veiculo.dto.response.VeiculoResponse;
import com.revendaveiculos.servicovendas.application.veiculo.mapper.VeiculoMapper;
import com.revendaveiculos.servicovendas.application.veiculo.port.in.ListarVeiculosVendidosInputPort;
import com.revendaveiculos.servicovendas.application.veiculo.port.out.VeiculoRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListarVeiculosVendidosUseCase implements ListarVeiculosVendidosInputPort {

    private final VeiculoRepositoryPort veiculoRepositoryPort;
    private final VeiculoMapper veiculoMapper;

    public ListarVeiculosVendidosUseCase(VeiculoRepositoryPort veiculoRepositoryPort, VeiculoMapper veiculoMapper) {
        this.veiculoRepositoryPort = veiculoRepositoryPort;
        this.veiculoMapper = veiculoMapper;
    }

    @Override
    public List<VeiculoResponse> listar() {
        return veiculoRepositoryPort.listarVendidos().stream()
                .map(veiculoMapper::paraResponse)
                .toList();
    }
}
