package com.revendaveiculos.servicovendas.application.venda.usecase;

import com.revendaveiculos.servicovendas.application.venda.dto.request.WebhookPagamentoRequest;
import com.revendaveiculos.servicovendas.application.venda.dto.response.VendaResponse;
import com.revendaveiculos.servicovendas.application.venda.mapper.VendaMapper;
import com.revendaveiculos.servicovendas.application.venda.port.in.ProcessarWebhookPagamentoInputPort;
import com.revendaveiculos.servicovendas.application.venda.port.out.SistemaPrincipalPort;
import com.revendaveiculos.servicovendas.application.venda.port.out.VendaRepositoryPort;
import com.revendaveiculos.servicovendas.application.veiculo.port.out.VeiculoRepositoryPort;
import com.revendaveiculos.servicovendas.domain.exception.VeiculoNaoEncontradoException;
import com.revendaveiculos.servicovendas.domain.exception.VendaNaoEncontradaException;
import com.revendaveiculos.servicovendas.domain.model.veiculo.Veiculo;
import com.revendaveiculos.servicovendas.domain.model.venda.Venda;
import org.springframework.stereotype.Service;

/**
 * Processa o resultado informado pela entidade de pagamento externa
 * (simulada) via webhook: aprova ou cancela a venda, atualiza o status
 * do veiculo correspondente e notifica o sistema-principal-veiculos.
 */
@Service
public class ProcessarWebhookPagamentoUseCase implements ProcessarWebhookPagamentoInputPort {

    private final VendaRepositoryPort vendaRepositoryPort;
    private final VeiculoRepositoryPort veiculoRepositoryPort;
    private final SistemaPrincipalPort sistemaPrincipalPort;
    private final VendaMapper vendaMapper;

    public ProcessarWebhookPagamentoUseCase(VendaRepositoryPort vendaRepositoryPort,
                                             VeiculoRepositoryPort veiculoRepositoryPort,
                                             SistemaPrincipalPort sistemaPrincipalPort,
                                             VendaMapper vendaMapper) {
        this.vendaRepositoryPort = vendaRepositoryPort;
        this.veiculoRepositoryPort = veiculoRepositoryPort;
        this.sistemaPrincipalPort = sistemaPrincipalPort;
        this.vendaMapper = vendaMapper;
    }

    @Override
    public VendaResponse processar(WebhookPagamentoRequest request) {
        Venda venda = vendaRepositoryPort.buscarPorCodigoPagamento(request.codigoPagamento())
                .orElseThrow(() -> new VendaNaoEncontradaException(request.codigoPagamento()));

        Veiculo veiculo = veiculoRepositoryPort.buscarPorId(venda.getVeiculoId())
                .orElseThrow(() -> new VeiculoNaoEncontradoException(venda.getVeiculoId()));

        switch (request.resultado()) {
            case APROVADO -> {
                venda.confirmarPagamento();
                veiculo.confirmarVenda();
            }
            case CANCELADO -> {
                venda.cancelarPagamento();
                veiculo.cancelarReserva();
            }
        }

        veiculoRepositoryPort.salvar(veiculo);
        Venda vendaAtualizada = vendaRepositoryPort.salvar(venda);

        sistemaPrincipalPort.notificarResultadoVenda(vendaAtualizada);

        return vendaMapper.paraResponse(vendaAtualizada);
    }
}
