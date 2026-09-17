package com.revendaveiculos.servicovendas.domain.model.venda;

import com.revendaveiculos.servicovendas.domain.exception.TransicaoStatusInvalidaException;
import com.revendaveiculos.servicovendas.domain.vo.Cpf;
import com.revendaveiculos.servicovendas.domain.vo.Preco;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade de dominio pura (sem anotacao JPA). Representa uma venda em
 * andamento ou concluida no servico-vendas-veiculos.
 */
public class Venda {

    private Long id;
    private final Long veiculoId;
    private final Cpf cpfComprador;
    private final LocalDateTime dataVenda;
    private final Preco valorVenda;
    private StatusVenda status;
    private final String codigoPagamento;

    private Venda(Long id, Long veiculoId, Cpf cpfComprador, LocalDateTime dataVenda, Preco valorVenda,
                  StatusVenda status, String codigoPagamento) {
        this.id = id;
        this.veiculoId = veiculoId;
        this.cpfComprador = cpfComprador;
        this.dataVenda = dataVenda;
        this.valorVenda = valorVenda;
        this.status = status;
        this.codigoPagamento = codigoPagamento;
    }

    /**
     * Efetua uma nova venda: gera o codigo de pagamento e marca como
     * PENDENTE, aguardando o webhook da entidade de pagamento externa.
     * O chamador (EfetuarVendaUseCase) e responsavel por reservar o
     * veiculo correspondente antes/depois de chamar este metodo.
     */
    public static Venda efetuar(Long veiculoId, Cpf cpfComprador, Preco valorVenda) {
        return new Venda(null, veiculoId, cpfComprador, LocalDateTime.now(), valorVenda,
                StatusVenda.PENDENTE, UUID.randomUUID().toString());
    }

    /** Reconstroi uma venda existente (vinda da persistencia). */
    public static Venda restaurar(Long id, Long veiculoId, Cpf cpfComprador, LocalDateTime dataVenda,
                                   Preco valorVenda, StatusVenda status, String codigoPagamento) {
        return new Venda(id, veiculoId, cpfComprador, dataVenda, valorVenda, status, codigoPagamento);
    }

    public void confirmarPagamento() {
        exigirPendente();
        this.status = StatusVenda.PAGAMENTO_APROVADO;
    }

    public void cancelarPagamento() {
        exigirPendente();
        this.status = StatusVenda.PAGAMENTO_CANCELADO;
    }

    private void exigirPendente() {
        if (status != StatusVenda.PENDENTE) {
            throw new TransicaoStatusInvalidaException(
                    "So e possivel processar o webhook de pagamento de uma venda PENDENTE (atual: " + status + ")");
        }
    }

    public Long getId() {
        return id;
    }

    public Long getVeiculoId() {
        return veiculoId;
    }

    public Cpf getCpfComprador() {
        return cpfComprador;
    }

    public LocalDateTime getDataVenda() {
        return dataVenda;
    }

    public Preco getValorVenda() {
        return valorVenda;
    }

    public StatusVenda getStatus() {
        return status;
    }

    public String getCodigoPagamento() {
        return codigoPagamento;
    }
}
