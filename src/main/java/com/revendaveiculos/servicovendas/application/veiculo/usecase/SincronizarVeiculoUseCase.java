package com.revendaveiculos.servicovendas.application.veiculo.usecase;

import com.revendaveiculos.servicovendas.application.veiculo.dto.request.SincronizarVeiculoRequest;
import com.revendaveiculos.servicovendas.application.veiculo.dto.response.VeiculoResponse;
import com.revendaveiculos.servicovendas.application.veiculo.mapper.VeiculoMapper;
import com.revendaveiculos.servicovendas.application.veiculo.port.in.SincronizarVeiculoInputPort;
import com.revendaveiculos.servicovendas.application.veiculo.port.out.VeiculoRepositoryPort;
import com.revendaveiculos.servicovendas.domain.model.veiculo.Veiculo;
import org.springframework.stereotype.Service;

/**
 * Recebe do sistema-principal-veiculos a copia resumida de um veiculo
 * (a cada cadastro/edicao) e persiste localmente para listagem/venda.
 */
@Service
public class SincronizarVeiculoUseCase implements SincronizarVeiculoInputPort {

    private final VeiculoRepositoryPort veiculoRepositoryPort;
    private final VeiculoMapper veiculoMapper;

    public SincronizarVeiculoUseCase(VeiculoRepositoryPort veiculoRepositoryPort, VeiculoMapper veiculoMapper) {
        this.veiculoRepositoryPort = veiculoRepositoryPort;
        this.veiculoMapper = veiculoMapper;
    }

    @Override
    public VeiculoResponse sincronizar(SincronizarVeiculoRequest request) {
        Veiculo veiculo = veiculoMapper.paraDominio(request);
        Veiculo veiculoSalvo = veiculoRepositoryPort.salvar(veiculo);
        return veiculoMapper.paraResponse(veiculoSalvo);
    }
}
