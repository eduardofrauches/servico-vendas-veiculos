package com.revendaveiculos.servicovendas.application.venda.usecase;

import com.revendaveiculos.servicovendas.application.venda.dto.request.WebhookPagamentoRequest;
import com.revendaveiculos.servicovendas.application.venda.dto.response.VendaResponse;
import com.revendaveiculos.servicovendas.application.venda.mapper.VendaMapper;
import com.revendaveiculos.servicovendas.application.venda.port.out.SistemaPrincipalPort;
import com.revendaveiculos.servicovendas.application.venda.port.out.VendaRepositoryPort;
import com.revendaveiculos.servicovendas.application.veiculo.port.out.VeiculoRepositoryPort;
import com.revendaveiculos.servicovendas.domain.exception.TransicaoStatusInvalidaException;
import com.revendaveiculos.servicovendas.domain.exception.VeiculoNaoEncontradoException;
import com.revendaveiculos.servicovendas.domain.exception.VendaNaoEncontradaException;
import com.revendaveiculos.servicovendas.domain.model.veiculo.StatusVeiculo;
import com.revendaveiculos.servicovendas.domain.model.veiculo.Veiculo;
import com.revendaveiculos.servicovendas.domain.model.venda.ResultadoPagamento;
import com.revendaveiculos.servicovendas.domain.model.venda.StatusVenda;
import com.revendaveiculos.servicovendas.domain.model.venda.Venda;
import com.revendaveiculos.servicovendas.domain.vo.Cpf;
import com.revendaveiculos.servicovendas.domain.vo.Preco;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessarWebhookPagamentoUseCaseTest {

    private static final String CODIGO_PAGAMENTO = "codigo-teste-123";

    @Mock
    private VendaRepositoryPort vendaRepositoryPort;

    @Mock
    private VeiculoRepositoryPort veiculoRepositoryPort;

    @Mock
    private SistemaPrincipalPort sistemaPrincipalPort;

    private final VendaMapper vendaMapper = new VendaMapper();

    private ProcessarWebhookPagamentoUseCase useCase;

    private Venda vendaPendente() {
        return Venda.restaurar(10L, 1L, Cpf.de("529.982.247-25"), LocalDateTime.now(),
                Preco.de(BigDecimal.valueOf(95000)), StatusVenda.PENDENTE, CODIGO_PAGAMENTO);
    }

    private Veiculo veiculoReservado() {
        return Veiculo.restaurar(1L, "Toyota", "Corolla", 2022, "Prata",
                Preco.de(BigDecimal.valueOf(95000)), StatusVeiculo.RESERVADO);
    }

    @Test
    void deveAprovarPagamentoEMarcarVeiculoComoVendido() {
        useCase = new ProcessarWebhookPagamentoUseCase(vendaRepositoryPort, veiculoRepositoryPort, sistemaPrincipalPort, vendaMapper);

        when(vendaRepositoryPort.buscarPorCodigoPagamento(CODIGO_PAGAMENTO)).thenReturn(Optional.of(vendaPendente()));
        when(veiculoRepositoryPort.buscarPorId(1L)).thenReturn(Optional.of(veiculoReservado()));
        when(vendaRepositoryPort.salvar(any(Venda.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VendaResponse response = useCase.processar(new WebhookPagamentoRequest(CODIGO_PAGAMENTO, ResultadoPagamento.APROVADO));

        assertThat(response.status()).isEqualTo("PAGAMENTO_APROVADO");

        ArgumentCaptor<Veiculo> veiculoCaptor = ArgumentCaptor.forClass(Veiculo.class);
        verify(veiculoRepositoryPort).salvar(veiculoCaptor.capture());
        assertThat(veiculoCaptor.getValue().getStatus()).isEqualTo(StatusVeiculo.VENDIDO);

        verify(sistemaPrincipalPort).notificarResultadoVenda(any(Venda.class));
    }

    @Test
    void deveCancelarPagamentoEDevolverVeiculoParaDisponivel() {
        useCase = new ProcessarWebhookPagamentoUseCase(vendaRepositoryPort, veiculoRepositoryPort, sistemaPrincipalPort, vendaMapper);

        when(vendaRepositoryPort.buscarPorCodigoPagamento(CODIGO_PAGAMENTO)).thenReturn(Optional.of(vendaPendente()));
        when(veiculoRepositoryPort.buscarPorId(1L)).thenReturn(Optional.of(veiculoReservado()));
        when(vendaRepositoryPort.salvar(any(Venda.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VendaResponse response = useCase.processar(new WebhookPagamentoRequest(CODIGO_PAGAMENTO, ResultadoPagamento.CANCELADO));

        assertThat(response.status()).isEqualTo("PAGAMENTO_CANCELADO");

        ArgumentCaptor<Veiculo> veiculoCaptor = ArgumentCaptor.forClass(Veiculo.class);
        verify(veiculoRepositoryPort).salvar(veiculoCaptor.capture());
        assertThat(veiculoCaptor.getValue().getStatus()).isEqualTo(StatusVeiculo.DISPONIVEL);
    }

    @Test
    void deveLancarExcecaoQuandoVendaNaoEncontrada() {
        useCase = new ProcessarWebhookPagamentoUseCase(vendaRepositoryPort, veiculoRepositoryPort, sistemaPrincipalPort, vendaMapper);

        when(vendaRepositoryPort.buscarPorCodigoPagamento("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.processar(new WebhookPagamentoRequest("inexistente", ResultadoPagamento.APROVADO)))
                .isInstanceOf(VendaNaoEncontradaException.class);

        verifyNoInteractions(veiculoRepositoryPort, sistemaPrincipalPort);
    }

    @Test
    void deveLancarExcecaoQuandoVeiculoDaVendaNaoEncontrado() {
        useCase = new ProcessarWebhookPagamentoUseCase(vendaRepositoryPort, veiculoRepositoryPort, sistemaPrincipalPort, vendaMapper);

        when(vendaRepositoryPort.buscarPorCodigoPagamento(CODIGO_PAGAMENTO)).thenReturn(Optional.of(vendaPendente()));
        when(veiculoRepositoryPort.buscarPorId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.processar(new WebhookPagamentoRequest(CODIGO_PAGAMENTO, ResultadoPagamento.APROVADO)))
                .isInstanceOf(VeiculoNaoEncontradoException.class);

        verifyNoInteractions(sistemaPrincipalPort);
        verify(vendaRepositoryPort, never()).salvar(any());
    }

    @Test
    void deveLancarExcecaoQuandoVendaJaFoiProcessada() {
        useCase = new ProcessarWebhookPagamentoUseCase(vendaRepositoryPort, veiculoRepositoryPort, sistemaPrincipalPort, vendaMapper);

        Venda vendaJaAprovada = Venda.restaurar(10L, 1L, Cpf.de("529.982.247-25"), LocalDateTime.now(),
                Preco.de(BigDecimal.valueOf(95000)), StatusVenda.PAGAMENTO_APROVADO, CODIGO_PAGAMENTO);
        when(vendaRepositoryPort.buscarPorCodigoPagamento(CODIGO_PAGAMENTO)).thenReturn(Optional.of(vendaJaAprovada));
        when(veiculoRepositoryPort.buscarPorId(1L)).thenReturn(Optional.of(veiculoReservado()));

        assertThatThrownBy(() -> useCase.processar(new WebhookPagamentoRequest(CODIGO_PAGAMENTO, ResultadoPagamento.APROVADO)))
                .isInstanceOf(TransicaoStatusInvalidaException.class);

        verifyNoInteractions(sistemaPrincipalPort);
    }
}
