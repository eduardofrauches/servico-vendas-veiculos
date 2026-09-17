package com.revendaveiculos.servicovendas.application.veiculo.usecase;

import com.revendaveiculos.servicovendas.application.veiculo.dto.request.SincronizarVeiculoRequest;
import com.revendaveiculos.servicovendas.application.veiculo.dto.response.VeiculoResponse;
import com.revendaveiculos.servicovendas.application.veiculo.mapper.VeiculoMapper;
import com.revendaveiculos.servicovendas.application.veiculo.port.out.VeiculoRepositoryPort;
import com.revendaveiculos.servicovendas.domain.exception.PrecoInvalidoException;
import com.revendaveiculos.servicovendas.domain.model.veiculo.StatusVeiculo;
import com.revendaveiculos.servicovendas.domain.model.veiculo.Veiculo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SincronizarVeiculoUseCaseTest {

    @Mock
    private VeiculoRepositoryPort veiculoRepositoryPort;

    private final VeiculoMapper veiculoMapper = new VeiculoMapper();

    @Test
    void deveSincronizarVeiculoRecebidoDoSistemaPrincipal() {
        SincronizarVeiculoUseCase useCase = new SincronizarVeiculoUseCase(veiculoRepositoryPort, veiculoMapper);

        SincronizarVeiculoRequest request = new SincronizarVeiculoRequest(
                1L, "Toyota", "Corolla", 2022, "Prata", BigDecimal.valueOf(95000), StatusVeiculo.DISPONIVEL);

        Veiculo salvo = Veiculo.restaurar(1L, "Toyota", "Corolla", 2022, "Prata",
                com.revendaveiculos.servicovendas.domain.vo.Preco.de(BigDecimal.valueOf(95000)),
                StatusVeiculo.DISPONIVEL);
        when(veiculoRepositoryPort.salvar(any(Veiculo.class))).thenReturn(salvo);

        VeiculoResponse response = useCase.sincronizar(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo("DISPONIVEL");
    }

    @Test
    void deveLancarExcecaoParaPrecoInvalido() {
        SincronizarVeiculoUseCase useCase = new SincronizarVeiculoUseCase(veiculoRepositoryPort, veiculoMapper);

        SincronizarVeiculoRequest request = new SincronizarVeiculoRequest(
                1L, "Toyota", "Corolla", 2022, "Prata", BigDecimal.valueOf(-10), StatusVeiculo.DISPONIVEL);

        assertThatThrownBy(() -> useCase.sincronizar(request))
                .isInstanceOf(PrecoInvalidoException.class);

        verifyNoInteractions(veiculoRepositoryPort);
    }
}
