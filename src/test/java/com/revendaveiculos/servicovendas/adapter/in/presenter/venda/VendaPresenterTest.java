package com.revendaveiculos.servicovendas.adapter.in.presenter.venda;

import com.revendaveiculos.servicovendas.application.venda.dto.response.VendaResponse;
import com.revendaveiculos.servicovendas.domain.model.venda.StatusVenda;
import com.revendaveiculos.servicovendas.domain.model.venda.Venda;
import com.revendaveiculos.servicovendas.domain.vo.Cpf;
import com.revendaveiculos.servicovendas.domain.vo.Preco;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class VendaPresenterTest {

    private final VendaPresenter presenter = new VendaPresenter();

    @Test
    void deveApresentarVendaComoResponse() {
        LocalDateTime dataVenda = LocalDateTime.now();
        Venda venda = Venda.restaurar(10L, 1L, Cpf.de("529.982.247-25"), dataVenda,
                Preco.de(BigDecimal.valueOf(95000)), StatusVenda.PENDENTE, "codigo-teste-123");

        VendaResponse response = presenter.apresentar(venda);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.veiculoId()).isEqualTo(1L);
        assertThat(response.cpfComprador()).isEqualTo("52998224725");
        assertThat(response.dataVenda()).isEqualTo(dataVenda);
        assertThat(response.valorVenda()).isEqualByComparingTo(BigDecimal.valueOf(95000));
        assertThat(response.status()).isEqualTo("PENDENTE");
        assertThat(response.codigoPagamento()).isEqualTo("codigo-teste-123");
    }
}
