package com.revendaveiculos.servicovendas.adapter.out.venda.persistence.jpa.entity;

import com.revendaveiculos.servicovendas.domain.model.venda.StatusVenda;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vendas")
public class VendaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "veiculo_id", nullable = false)
    private Long veiculoId;

    @Column(name = "cpf_comprador", nullable = false, length = 11)
    private String cpfComprador;

    @Column(name = "data_venda", nullable = false)
    private LocalDateTime dataVenda;

    @Column(name = "valor_venda", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorVenda;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusVenda status;

    @Column(name = "codigo_pagamento", nullable = false, unique = true)
    private String codigoPagamento;

    protected VendaEntity() {
        // exigido pelo JPA
    }

    public VendaEntity(Long id, Long veiculoId, String cpfComprador, LocalDateTime dataVenda,
                        BigDecimal valorVenda, StatusVenda status, String codigoPagamento) {
        this.id = id;
        this.veiculoId = veiculoId;
        this.cpfComprador = cpfComprador;
        this.dataVenda = dataVenda;
        this.valorVenda = valorVenda;
        this.status = status;
        this.codigoPagamento = codigoPagamento;
    }

    public Long getId() {
        return id;
    }

    public Long getVeiculoId() {
        return veiculoId;
    }

    public String getCpfComprador() {
        return cpfComprador;
    }

    public LocalDateTime getDataVenda() {
        return dataVenda;
    }

    public BigDecimal getValorVenda() {
        return valorVenda;
    }

    public StatusVenda getStatus() {
        return status;
    }

    public String getCodigoPagamento() {
        return codigoPagamento;
    }
}
