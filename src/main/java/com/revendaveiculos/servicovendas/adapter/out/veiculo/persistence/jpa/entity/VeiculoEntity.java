package com.revendaveiculos.servicovendas.adapter.out.veiculo.persistence.jpa.entity;

import com.revendaveiculos.servicovendas.domain.model.veiculo.StatusVeiculo;
import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Copia local de um veiculo (sincronizada via HTTP a partir do
 * sistema-principal-veiculos). O id aqui e o mesmo id do veiculo no
 * sistema-principal (nao gerado localmente).
 */
@Entity
@Table(name = "veiculos")
public class VeiculoEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String marca;

    @Column(nullable = false)
    private String modelo;

    @Column(nullable = false)
    private Integer ano;

    @Column(nullable = false)
    private String cor;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal preco;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusVeiculo status;

    protected VeiculoEntity() {
        // exigido pelo JPA
    }

    public VeiculoEntity(Long id, String marca, String modelo, Integer ano, String cor,
                          BigDecimal preco, StatusVeiculo status) {
        this.id = id;
        this.marca = marca;
        this.modelo = modelo;
        this.ano = ano;
        this.cor = cor;
        this.preco = preco;
        this.status = status;
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

    public BigDecimal getPreco() {
        return preco;
    }

    public StatusVeiculo getStatus() {
        return status;
    }
}
