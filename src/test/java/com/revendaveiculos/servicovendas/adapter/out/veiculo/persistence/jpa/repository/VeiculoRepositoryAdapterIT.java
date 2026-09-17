package com.revendaveiculos.servicovendas.adapter.out.veiculo.persistence.jpa.repository;

import com.revendaveiculos.servicovendas.adapter.out.veiculo.persistence.jpa.mapper.VeiculoEntityMapper;
import com.revendaveiculos.servicovendas.domain.model.veiculo.StatusVeiculo;
import com.revendaveiculos.servicovendas.domain.model.veiculo.Veiculo;
import com.revendaveiculos.servicovendas.domain.vo.Preco;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste de integracao real do adapter de persistencia: sobe um Postgres
 * de verdade via Testcontainers (nao H2) e exercita VeiculoRepositoryAdapter
 * ponta a ponta contra o banco, incluindo as consultas por status.
 */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({VeiculoEntityMapper.class, VeiculoRepositoryAdapter.class})
class VeiculoRepositoryAdapterIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private VeiculoRepositoryAdapter veiculoRepositoryAdapter;

    private Veiculo novoVeiculo(Long id, StatusVeiculo status) {
        return Veiculo.sincronizar(id, "Toyota", "Corolla", 2022, "Prata",
                Preco.de(BigDecimal.valueOf(95000)), status);
    }

    @Test
    void deveSalvarERecuperarVeiculoPorId() {
        veiculoRepositoryAdapter.salvar(novoVeiculo(1L, StatusVeiculo.DISPONIVEL));

        Optional<Veiculo> encontrado = veiculoRepositoryAdapter.buscarPorId(1L);

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getMarca()).isEqualTo("Toyota");
        assertThat(encontrado.get().getStatus()).isEqualTo(StatusVeiculo.DISPONIVEL);
    }

    @Test
    void deveRetornarVazioQuandoIdNaoExiste() {
        assertThat(veiculoRepositoryAdapter.buscarPorId(999_999L)).isEmpty();
    }

    @Test
    void deveListarApenasVeiculosDisponiveis() {
        veiculoRepositoryAdapter.salvar(novoVeiculo(1L, StatusVeiculo.DISPONIVEL));
        veiculoRepositoryAdapter.salvar(novoVeiculo(2L, StatusVeiculo.VENDIDO));
        veiculoRepositoryAdapter.salvar(novoVeiculo(3L, StatusVeiculo.DISPONIVEL));

        List<Veiculo> disponiveis = veiculoRepositoryAdapter.listarDisponiveis();

        assertThat(disponiveis).extracting(Veiculo::getId).containsExactlyInAnyOrder(1L, 3L);
    }

    @Test
    void deveListarApenasVeiculosVendidos() {
        veiculoRepositoryAdapter.salvar(novoVeiculo(1L, StatusVeiculo.DISPONIVEL));
        veiculoRepositoryAdapter.salvar(novoVeiculo(2L, StatusVeiculo.VENDIDO));

        List<Veiculo> vendidos = veiculoRepositoryAdapter.listarVendidos();

        assertThat(vendidos).extracting(Veiculo::getId).containsExactly(2L);
    }

    @Test
    void salvarDeveAtualizarQuandoIdJaExiste() {
        veiculoRepositoryAdapter.salvar(novoVeiculo(1L, StatusVeiculo.DISPONIVEL));
        veiculoRepositoryAdapter.salvar(novoVeiculo(1L, StatusVeiculo.RESERVADO));

        Optional<Veiculo> encontrado = veiculoRepositoryAdapter.buscarPorId(1L);

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getStatus()).isEqualTo(StatusVeiculo.RESERVADO);
    }
}
