package com.revendaveiculos.servicovendas.adapter.in.presenter.veiculo;

import com.revendaveiculos.servicovendas.application.veiculo.dto.response.VeiculoResponse;
import com.revendaveiculos.servicovendas.domain.model.veiculo.StatusVeiculo;
import com.revendaveiculos.servicovendas.domain.model.veiculo.Veiculo;
import com.revendaveiculos.servicovendas.domain.vo.Preco;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VeiculoPresenterTest {

    private final VeiculoPresenter presenter = new VeiculoPresenter();

    @Test
    void deveApresentarVeiculoComoResponse() {
        Veiculo veiculo = Veiculo.restaurar(1L, "Toyota", "Corolla", 2022, "Prata",
                Preco.de(BigDecimal.valueOf(95000)), StatusVeiculo.DISPONIVEL);

        VeiculoResponse response = presenter.apresentar(veiculo);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.marca()).isEqualTo("Toyota");
        assertThat(response.modelo()).isEqualTo("Corolla");
        assertThat(response.ano()).isEqualTo(2022);
        assertThat(response.cor()).isEqualTo("Prata");
        assertThat(response.preco()).isEqualByComparingTo(BigDecimal.valueOf(95000));
        assertThat(response.status()).isEqualTo("DISPONIVEL");
    }

    @Test
    void deveApresentarListaDeVeiculosPreservandoOrdem() {
        Veiculo primeiro = Veiculo.restaurar(1L, "Toyota", "Corolla", 2022, "Prata",
                Preco.de(BigDecimal.valueOf(95000)), StatusVeiculo.DISPONIVEL);
        Veiculo segundo = Veiculo.restaurar(2L, "Honda", "Civic", 2023, "Preto",
                Preco.de(BigDecimal.valueOf(110000)), StatusVeiculo.VENDIDO);

        List<VeiculoResponse> resultado = presenter.apresentarLista(List.of(primeiro, segundo));

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).id()).isEqualTo(1L);
        assertThat(resultado.get(1).id()).isEqualTo(2L);
        assertThat(resultado.get(1).status()).isEqualTo("VENDIDO");
    }
}
