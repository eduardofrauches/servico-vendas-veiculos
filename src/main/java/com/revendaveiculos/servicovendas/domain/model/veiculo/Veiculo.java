package com.revendaveiculos.servicovendas.domain.model.veiculo;

import com.revendaveiculos.servicovendas.domain.exception.TransicaoStatusInvalidaException;
import com.revendaveiculos.servicovendas.domain.exception.VeiculoNaoDisponivelException;
import com.revendaveiculos.servicovendas.domain.vo.Preco;

import java.util.Map;
import java.util.Set;

/**
 * Copia local (sincronizada via HTTP a partir do sistema-principal-veiculos)
 * dos dados de um veiculo. Este servico nao e dono do dado-mestre — apenas
 * mantem uma copia suficiente para listar e vender sem depender do
 * sistema-principal estar no ar.
 */
public class Veiculo {

    private static final Map<StatusVeiculo, Set<StatusVeiculo>> TRANSICOES_VALIDAS = Map.of(
            StatusVeiculo.DISPONIVEL, Set.of(StatusVeiculo.RESERVADO),
            StatusVeiculo.RESERVADO, Set.of(StatusVeiculo.VENDIDO, StatusVeiculo.DISPONIVEL),
            StatusVeiculo.VENDIDO, Set.of()
    );

    private final Long id;
    private String marca;
    private String modelo;
    private Integer ano;
    private String cor;
    private Preco preco;
    private StatusVeiculo status;

    private Veiculo(Long id, String marca, String modelo, Integer ano, String cor, Preco preco,
                     StatusVeiculo status) {
        this.id = id;
        this.marca = marca;
        this.modelo = modelo;
        this.ano = ano;
        this.cor = cor;
        this.preco = preco;
        this.status = status;
    }

    /** Cria/atualiza a copia local a partir da sincronizacao vinda do sistema-principal. */
    public static Veiculo sincronizar(Long id, String marca, String modelo, Integer ano, String cor,
                                       Preco preco, StatusVeiculo status) {
        return new Veiculo(id, marca, modelo, ano, cor, preco, status);
    }

    /** Reconstroi a partir da persistencia local. */
    public static Veiculo restaurar(Long id, String marca, String modelo, Integer ano, String cor,
                                     Preco preco, StatusVeiculo status) {
        return new Veiculo(id, marca, modelo, ano, cor, preco, status);
    }

    /** Reserva o veiculo ao iniciar uma venda. So permitido se estiver DISPONIVEL. */
    public void reservar() {
        if (status != StatusVeiculo.DISPONIVEL) {
            throw new VeiculoNaoDisponivelException(
                    "Veiculo " + id + " nao esta disponivel para venda (status atual: " + status + ")");
        }
        transicionarPara(StatusVeiculo.RESERVADO);
    }

    public void confirmarVenda() {
        transicionarPara(StatusVeiculo.VENDIDO);
    }

    public void cancelarReserva() {
        transicionarPara(StatusVeiculo.DISPONIVEL);
    }

    private void transicionarPara(StatusVeiculo novoStatus) {
        Set<StatusVeiculo> permitidas = TRANSICOES_VALIDAS.get(this.status);
        if (permitidas == null || !permitidas.contains(novoStatus)) {
            throw new TransicaoStatusInvalidaException(
                    "Transicao de status invalida: " + this.status + " -> " + novoStatus);
        }
        this.status = novoStatus;
    }

    public Long getId() {
        return id;
    }

    public String getMarca() {
        return marca;
    }

    public String getModelo() {
        return modelo;
    }

    public Integer getAno() {
        return ano;
    }

    public String getCor() {
        return cor;
    }

    public Preco getPreco() {
        return preco;
    }

    public StatusVeiculo getStatus() {
        return status;
    }
}
