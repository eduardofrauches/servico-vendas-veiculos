package com.revendaveiculos.servicovendas.application.venda.usecase;

import com.revendaveiculos.servicovendas.application.venda.dto.request.EfetuarVendaRequest;
import com.revendaveiculos.servicovendas.application.venda.dto.response.VendaResponse;
import com.revendaveiculos.servicovendas.application.venda.mapper.VendaMapper;
import com.revendaveiculos.servicovendas.application.venda.port.out.SistemaPrincipalPort;
import com.revendaveiculos.servicovendas.application.venda.port.out.VendaRepositoryPort;
import com.revendaveiculos.servicovendas.application.veiculo.port.out.VeiculoRepositoryPort;
import com.revendaveiculos.servicovendas.domain.exception.CpfInvalidoException;
import com.revendaveiculos.servicovendas.domain.exception.VeiculoNaoDisponivelException;
import com.revendaveiculos.servicovendas.domain.exception.VeiculoNaoEncontradoException;
import com.revendaveiculos.servicovendas.domain.model.veiculo.StatusVeiculo;
import com.revendaveiculos.servicovendas.domain.model.veiculo.Veiculo;
import com.revendaveiculos.servicovendas.domain.model.venda.Venda;
import com.revendaveiculos.servicovendas.domain.vo.Preco;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EfetuarVendaUseCaseTest {

    @Mock
    private VeiculoRepositoryPort veiculoRepositoryPort;

    @Mock
    private VendaRepositoryPort vendaRepositoryPort;

    @Mock
    private SistemaPrincipalPort sistemaPrincipalPort;

    private final VendaMapper vendaMapper = new VendaMapper();

    private EfetuarVendaUseCase useCase;

    private Veiculo veiculoDisponivel() {
        return Veiculo.restaurar(1L, "Toyota", "Corolla", 2022, "Prata",
                Preco.de(BigDecimal.valueOf(95000)), StatusVeiculo.DISPONIVEL);
    }

    @Test
    void deveEfetuarVendaReservarVeiculoENotificarSistemaPrincipal() {
        useCase = new EfetuarVendaUseCase(veiculoRepositoryPort, vendaRepositoryPort, sistemaPrincipalPort, vendaMapper);

        Veiculo veiculo = veiculoDisponivel();
        when(veiculoRepositoryPort.buscarPorId(1L)).thenReturn(Optional.of(veiculo));
        when(vendaRepositoryPort.salvar(any(Venda.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VendaResponse response = useCase.efetuar(new EfetuarVendaRequest(1L, "529.982.247-25"));

        assertThat(response.veiculoId()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo("PENDENTE");
        assertThat(response.codigoPagamento()).isNotBlank();

        ArgumentCaptor<Veiculo> veiculoCaptor = ArgumentCaptor.forClass(Veiculo.class);
        verify(veiculoRepositoryPort).salvar(veiculoCaptor.capture());
        assertThat(veiculoCaptor.getValue().getStatus()).isEqualTo(StatusVeiculo.RESERVADO);

        verify(sistemaPrincipalPort).notificarResultadoVenda(any(Venda.class));
    }

    @Test
    void deveLancarExcecaoQuandoVeiculoNaoEncontrado() {
        useCase = new EfetuarVendaUseCase(veiculoRepositoryPort, vendaRepositoryPort, sistemaPrincipalPort, vendaMapper);

        when(veiculoRepositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.efetuar(new EfetuarVendaRequest(99L, "529.982.247-25")))
                .isInstanceOf(VeiculoNaoEncontradoException.class);

        verifyNoInteractions(vendaRepositoryPort, sistemaPrincipalPort);
    }

    @Test
    void deveLancarExcecaoQuandoVeiculoNaoEstaDisponivel() {
        useCase = new EfetuarVendaUseCase(veiculoRepositoryPort, vendaRepositoryPort, sistemaPrincipalPort, vendaMapper);

        Veiculo jaVendido = Veiculo.restaurar(1L, "Toyota", "Corolla", 2022, "Prata",
                Preco.de(BigDecimal.valueOf(95000)), StatusVeiculo.VENDIDO);
        when(veiculoRepositoryPort.buscarPorId(1L)).thenReturn(Optional.of(jaVendido));

        assertThatThrownBy(() -> useCase.efetuar(new EfetuarVendaRequest(1L, "529.982.247-25")))
                .isInstanceOf(VeiculoNaoDisponivelException.class);

        verifyNoInteractions(vendaRepositoryPort, sistemaPrincipalPort);
    }

    @Test
    void deveLancarExcecaoQuandoCpfInvalido() {
        useCase = new EfetuarVendaUseCase(veiculoRepositoryPort, vendaRepositoryPort, sistemaPrincipalPort, vendaMapper);

        // CPF e validado antes de tocar no veiculo, entao nem buscarPorId chega a ser chamado.
        assertThatThrownBy(() -> useCase.efetuar(new EfetuarVendaRequest(1L, "111.111.111-11")))
                .isInstanceOf(CpfInvalidoException.class);

        verifyNoInteractions(veiculoRepositoryPort, vendaRepositoryPort, sistemaPrincipalPort);
    }
}
