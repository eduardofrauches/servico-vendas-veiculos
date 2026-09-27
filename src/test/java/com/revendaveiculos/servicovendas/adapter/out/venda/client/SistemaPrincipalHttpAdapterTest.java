package com.revendaveiculos.servicovendas.adapter.out.venda.client;

import com.revendaveiculos.servicovendas.domain.model.venda.StatusVenda;
import com.revendaveiculos.servicovendas.domain.model.venda.Venda;
import com.revendaveiculos.servicovendas.domain.vo.Cpf;
import com.revendaveiculos.servicovendas.domain.vo.Preco;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SistemaPrincipalHttpAdapterTest {

    @Mock
    private RestTemplate restTemplate;

    private final String baseUrl = "http://localhost:8081";

    private Venda vendaComStatus(StatusVenda status) {
        return Venda.restaurar(
                1L,
                10L,
                Cpf.de("52998224725"),
                LocalDateTime.now(),
                Preco.de(new BigDecimal("95000.00")),
                status,
                "codigo-teste"
        );
    }

    @Test
    void deveNotificarStatusReservadoQuandoVendaPendente() {
        SistemaPrincipalHttpAdapter adapter = new SistemaPrincipalHttpAdapter(restTemplate, baseUrl);

        adapter.notificarResultadoVenda(vendaComStatus(StatusVenda.PENDENTE));

        ArgumentCaptor<HttpEntity<AtualizarStatusVeiculoPayload>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq(baseUrl + "/veiculos/10/status"), eq(HttpMethod.PATCH), captor.capture(), eq(Void.class));
        assertThat(captor.getValue().getBody().status()).isEqualTo("RESERVADO");
    }

    @Test
    void deveNotificarStatusVendidoQuandoPagamentoAprovado() {
        SistemaPrincipalHttpAdapter adapter = new SistemaPrincipalHttpAdapter(restTemplate, baseUrl);

        adapter.notificarResultadoVenda(vendaComStatus(StatusVenda.PAGAMENTO_APROVADO));

        ArgumentCaptor<HttpEntity<AtualizarStatusVeiculoPayload>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq(baseUrl + "/veiculos/10/status"), eq(HttpMethod.PATCH), captor.capture(), eq(Void.class));
        assertThat(captor.getValue().getBody().status()).isEqualTo("VENDIDO");
    }

    @Test
    void deveNotificarStatusDisponivelQuandoPagamentoCancelado() {
        SistemaPrincipalHttpAdapter adapter = new SistemaPrincipalHttpAdapter(restTemplate, baseUrl);

        adapter.notificarResultadoVenda(vendaComStatus(StatusVenda.PAGAMENTO_CANCELADO));

        ArgumentCaptor<HttpEntity<AtualizarStatusVeiculoPayload>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq(baseUrl + "/veiculos/10/status"), eq(HttpMethod.PATCH), captor.capture(), eq(Void.class));
        assertThat(captor.getValue().getBody().status()).isEqualTo("DISPONIVEL");
    }

    @Test
    void naoDeveLancarExcecaoQuandoChamadaHttpFalha() {
        SistemaPrincipalHttpAdapter adapter = new SistemaPrincipalHttpAdapter(restTemplate, baseUrl);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Void.class)))
                .thenThrow(new RestClientException("falha simulada"));

        assertThatCode(() -> adapter.notificarResultadoVenda(vendaComStatus(StatusVenda.PAGAMENTO_APROVADO)))
                .doesNotThrowAnyException();
    }
}