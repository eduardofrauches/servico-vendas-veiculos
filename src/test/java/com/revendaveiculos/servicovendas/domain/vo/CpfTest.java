package com.revendaveiculos.servicovendas.domain.vo;

import com.revendaveiculos.servicovendas.domain.exception.CpfInvalidoException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfTest {

    @Test
    void deveAceitarCpfValidoComMascara() {
        Cpf cpf = Cpf.de("529.982.247-25");

        assertThat(cpf.numero()).isEqualTo("52998224725");
    }

    @Test
    void deveAceitarCpfValidoSemMascara() {
        Cpf cpf = Cpf.de("11144477735");

        assertThat(cpf.numero()).isEqualTo("11144477735");
    }

    @Test
    void deveFormatarComMascaraPadrao() {
        Cpf cpf = Cpf.de("52998224725");

        assertThat(cpf.formatado()).isEqualTo("529.982.247-25");
    }

    @Test
    void toStringDeveRetornarValorFormatado() {
        Cpf cpf = Cpf.de("52998224725");

        assertThat(cpf.toString()).isEqualTo(cpf.formatado());
    }

    @Test
    void deveRejeitarValorNulo() {
        assertThatThrownBy(() -> Cpf.de(null))
                .isInstanceOf(CpfInvalidoException.class)
                .hasMessageContaining("nao pode ser nulo");
    }

    @Test
    void deveRejeitarQuantidadeDeDigitosDiferenteDeOnze() {
        assertThatThrownBy(() -> Cpf.de("123456789"))
                .isInstanceOf(CpfInvalidoException.class)
                .hasMessageContaining("11 digitos");
    }

    @Test
    void deveRejeitarTodosDigitosIguais() {
        assertThatThrownBy(() -> Cpf.de("11111111111"))
                .isInstanceOf(CpfInvalidoException.class);
    }

    @Test
    void deveRejeitarDigitoVerificadorInvalido() {
        assertThatThrownBy(() -> Cpf.de("12345678900"))
                .isInstanceOf(CpfInvalidoException.class);
    }

    @Test
    void doisCpfsComMesmoNumeroDevemSerIguais() {
        Cpf a = Cpf.de("52998224725");
        Cpf b = Cpf.de("529.982.247-25");

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void cpfDeveSerIgualAEleMesmo() {
        Cpf a = Cpf.de("52998224725");

        assertThat(a).isEqualTo(a);
    }

    @Test
    void cpfNaoDeveSerIgualANuloOuOutroTipo() {
        Cpf a = Cpf.de("52998224725");

        assertThat(a).isNotEqualTo(null);
        assertThat(a).isNotEqualTo("52998224725");
    }

    @Test
    void cpfsComNumerosDiferentesNaoDevemSerIguais() {
        Cpf a = Cpf.de("52998224725");
        Cpf b = Cpf.de("11144477735");

        assertThat(a).isNotEqualTo(b);
    }
}