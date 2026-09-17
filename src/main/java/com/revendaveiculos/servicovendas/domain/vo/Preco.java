package com.revendaveiculos.servicovendas.domain.vo;

import com.revendaveiculos.servicovendas.domain.exception.PrecoInvalidoException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object que representa um preco monetario valido (sempre > 0,
 * sempre com 2 casas decimais). Imutavel.
 */
public final class Preco {

    private final BigDecimal valor;

    private Preco(BigDecimal valor) {
        this.valor = valor;
    }

    public static Preco de(BigDecimal valor) {
        if (valor == null) {
            throw new PrecoInvalidoException("Preco nao pode ser nulo");
        }
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PrecoInvalidoException("Preco deve ser maior que zero, recebido: " + valor);
        }
        return new Preco(valor.setScale(2, RoundingMode.HALF_UP));
    }

    public static Preco de(double valor) {
        return de(BigDecimal.valueOf(valor));
    }

    public BigDecimal valor() {
        return valor;
    }

    public boolean maiorQue(Preco outro) {
        return this.valor.compareTo(outro.valor) > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Preco other)) return false;
        return valor.compareTo(other.valor) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
