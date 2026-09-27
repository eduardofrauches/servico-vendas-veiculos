package com.revendaveiculos.servicovendas.domain.vo;

import com.revendaveiculos.servicovendas.domain.exception.PrecoInvalidoException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrecoTest {

    @Test
    void deveAceitarValorPositivo() {
        Preco preco = Preco.de(new BigDecimal("95000.00"));

        assertThat(preco.valor()).isEqualByComparingTo("95000.00");
    }

    @Test
    void deveAceitarValorPositivoComDouble() {
        Preco preco = Preco.de(95000.00);

        assertThat(preco.valor()).isEqualByComparingTo("95000.00");
    }

    @Test
    void deveArredondarParaDuasCasasDecimais() {
        Preco preco = Preco.de(new BigDecimal("95000.005"));

        assertThat(preco.valor()).isEqualByComparingTo("95000.01");
    }

    @Test
    void deveRejeitarValorNulo() {
        assertThatThrownBy(() -> Preco.de((BigDecimal) null))
                .isInstanceOf(PrecoInvalidoException.class)
                .hasMessageContaining("nao pode ser nulo");
    }

    @Test
    void deveRejeitarValorZero() {
        assertThatThrownBy(() -> Preco.de(BigDecimal.ZERO))
                .isInstanceOf(PrecoInvalidoException.class)
                .hasMessageContaining("maior que zero");
    }

    @Test
    void deveRejeitarValorNegativo() {
        assertThatThrownBy(() -> Preco.de(new BigDecimal("-10.00")))
                .isInstanceOf(PrecoInvalidoException.class)
                .hasMessageContaining("maior que zero");
    }

    @Test
    void maiorQueDeveCompararValoresCorretamente() {
        Preco caro = Preco.de(new BigDecimal("100.00"));
        Preco barato = Preco.de(new BigDecimal("50.00"));

        assertThat(caro.maiorQue(barato)).isTrue();
        assertThat(barato.maiorQue(caro)).isFalse();
    }

    @Test
    void precosComMesmoValorEEscalasDiferentesDevemSerIguais() {
        Preco a = Preco.de(new BigDecimal("100.10"));
        Preco b = Preco.de(new BigDecimal("100.1"));

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void precoDeveSerIgualAEleMesmo() {
        Preco preco = Preco.de(new BigDecimal("100.00"));

        assertThat(preco).isEqualTo(preco);
    }

    @Test
    void precoNaoDeveSerIgualANuloOuOutroTipo() {
        Preco preco = Preco.de(new BigDecimal("100.00"));

        assertThat(preco).isNotEqualTo(null);
        assertThat(preco).isNotEqualTo("100.00");
    }

    @Test
    void toStringDeveRetornarRepresentacaoDoValor() {
        Preco preco = Preco.de(new BigDecimal("100.00"));

        assertThat(preco.toString()).isEqualTo("100.00");
    }
}