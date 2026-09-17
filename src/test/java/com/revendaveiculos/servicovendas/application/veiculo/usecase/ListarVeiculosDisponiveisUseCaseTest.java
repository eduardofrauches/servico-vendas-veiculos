package com.revendaveiculos.servicovendas.application.veiculo.usecase;

import com.revendaveiculos.servicovendas.application.veiculo.dto.response.VeiculoResponse;
import com.revendaveiculos.servicovendas.application.veiculo.mapper.VeiculoMapper;
import com.revendaveiculos.servicovendas.application.veiculo.port.out.VeiculoRepositoryPort;
import com.revendaveiculos.servicovendas.domain.model.veiculo.StatusVeiculo;
import com.revendaveiculos.servicovendas.domain.model.veiculo.Veiculo;
import com.revendaveiculos.servicovendas.domain.vo.Preco;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarVeiculosDisponiveisUseCaseTest {

    @Mock
    private VeiculoRepositoryPort veiculoRepositoryPort;

    private final VeiculoMapper veiculoMapper = new VeiculoMapper();

    @Test
    void deveListarVeiculosDisponiveis() {
        ListarVeiculosDisponiveisUseCase useCase =
                new ListarVeiculosDisponiveisUseCase(veiculoRepositoryPort, veiculoMapper);

        Veiculo veiculo = Veiculo.restaurar(1L, "Toyota", "Corolla", 2022, "Prata",
                Preco.de(BigDecimal.valueOf(95000)), StatusVeiculo.DISPONIVEL);
        when(veiculoRepositoryPort.listarDisponiveis()).thenReturn(List.of(veiculo));

        List<VeiculoResponse> resultado = useCase.listar();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).id()).isEqualTo(1L);
        assertThat(resultado.get(0).status()).isEqualTo("DISPONIVEL");
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaVeiculosDisponiveis() {
        ListarVeiculosDisponiveisUseCase useCase =
                new ListarVeiculosDisponiveisUseCase(veiculoRepositoryPort, veiculoMapper);

        when(veiculoRepositoryPort.listarDisponiveis()).thenReturn(List.of());

        assertThat(useCase.listar()).isEmpty();
    }
}
