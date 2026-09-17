package com.revendaveiculos.servicovendas.application.venda.usecase;

import com.revendaveiculos.servicovendas.application.venda.dto.request.EfetuarVendaRequest;
import com.revendaveiculos.servicovendas.application.venda.dto.response.VendaResponse;
import com.revendaveiculos.servicovendas.application.venda.mapper.VendaMapper;
import com.revendaveiculos.servicovendas.application.venda.port.in.EfetuarVendaInputPort;
import com.revendaveiculos.servicovendas.application.venda.port.out.SistemaPrincipalPort;
import com.revendaveiculos.servicovendas.application.venda.port.out.VendaRepositoryPort;
import com.revendaveiculos.servicovendas.application.veiculo.port.out.VeiculoRepositoryPort;
import com.revendaveiculos.servicovendas.domain.exception.VeiculoNaoEncontradoException;
import com.revendaveiculos.servicovendas.domain.model.veiculo.Veiculo;
import com.revendaveiculos.servicovendas.domain.model.venda.Venda;
import com.revendaveiculos.servicovendas.domain.vo.Cpf;
import org.springframework.stereotype.Service;

/**
 * O comprador aciona a compra diretamente neste servico (CPF + veiculo).
 * Reserva o veiculo e gera um codigoPagamento, aguardando o webhook da
 * entidade de pagamento externa para confirmar ou cancelar.
 */
@Service
public class EfetuarVendaUseCase implements EfetuarVendaInputPort {

    private final VeiculoRepositoryPort veiculoRepositoryPort;
    private final VendaRepositoryPort vendaRepositoryPort;
    private final SistemaPrincipalPort sistemaPrincipalPort;
    private final VendaMapper vendaMapper;

    public EfetuarVendaUseCase(VeiculoRepositoryPort veiculoRepositoryPort,
                                VendaRepositoryPort vendaRepositoryPort,
                                SistemaPrincipalPort sistemaPrincipalPort,
                                VendaMapper vendaMapper) {
        this.veiculoRepositoryPort = veiculoRepositoryPort;
        this.vendaRepositoryPort = vendaRepositoryPort;
        this.sistemaPrincipalPort = sistemaPrincipalPort;
        this.vendaMapper = vendaMapper;
    }

    @Override
    public VendaResponse efetuar(EfetuarVendaRequest request) {
        // Valida o CPF antes de tocar no veiculo: se o CPF for invalido, o
        // veiculo nao deve ficar reservado sem nenhuma venda associada.
        Cpf cpfComprador = Cpf.de(request.cpfComprador());

        Veiculo veiculo = veiculoRepositoryPort.buscarPorId(request.veiculoId())
                .orElseThrow(() -> new VeiculoNaoEncontradoException(request.veiculoId()));

        veiculo.reservar();
        veiculoRepositoryPort.salvar(veiculo);

        Venda venda = Venda.efetuar(veiculo.getId(), cpfComprador, veiculo.getPreco());
        Venda vendaSalva = vendaRepositoryPort.salvar(venda);

        // Propaga a reserva para o sistema-principal-veiculos (dado-mestre),
        // para que sua propria maquina de estados fique coerente ate o
        // webhook de pagamento confirmar ou cancelar a venda.
        sistemaPrincipalPort.notificarResultadoVenda(vendaSalva);

        return vendaMapper.paraResponse(vendaSalva);
    }
}
