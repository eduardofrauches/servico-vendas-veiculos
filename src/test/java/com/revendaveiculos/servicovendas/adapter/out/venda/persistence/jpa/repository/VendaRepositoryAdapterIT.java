package com.revendaveiculos.servicovendas.adapter.out.venda.persistence.jpa.repository;

import com.revendaveiculos.servicovendas.adapter.out.venda.persistence.jpa.mapper.VendaEntityMapper;
import com.revendaveiculos.servicovendas.domain.model.venda.StatusVenda;
import com.revendaveiculos.servicovendas.domain.model.venda.Venda;
import com.revendaveiculos.servicovendas.domain.vo.Cpf;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste de integracao real do adapter de persistencia de Venda: sobe um
 * Postgres de verdade via Testcontainers (nao H2).
 */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({VendaEntityMapper.class, VendaRepositoryAdapter.class})
class VendaRepositoryAdapterIT {

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
    private VendaRepositoryAdapter vendaRepositoryAdapter;

    @Test
    void deveSalvarERecuperarVendaPorCodigoPagamento() {
        Venda venda = Venda.efetuar(1L, Cpf.de("529.982.247-25"), Preco.de(BigDecimal.valueOf(95000)));

        Venda salva = vendaRepositoryAdapter.salvar(venda);
        assertThat(salva.getId()).isNotNull();

        Optional<Venda> encontrada = vendaRepositoryAdapter.buscarPorCodigoPagamento(venda.getCodigoPagamento());

        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().getVeiculoId()).isEqualTo(1L);
        assertThat(encontrada.get().getStatus()).isEqualTo(StatusVenda.PENDENTE);
        assertThat(encontrada.get().getCpfComprador().numero()).isEqualTo("52998224725");
    }

    @Test
    void deveRetornarVazioQuandoCodigoPagamentoNaoExiste() {
        assertThat(vendaRepositoryAdapter.buscarPorCodigoPagamento("inexistente")).isEmpty();
    }

    @Test
    void deveAtualizarStatusDeVendaExistente() {
        Venda venda = Venda.efetuar(1L, Cpf.de("529.982.247-25"), Preco.de(BigDecimal.valueOf(95000)));
        Venda salva = vendaRepositoryAdapter.salvar(venda);

        salva.confirmarPagamento();
        vendaRepositoryAdapter.salvar(salva);

        Optional<Venda> encontrada = vendaRepositoryAdapter.buscarPorCodigoPagamento(venda.getCodigoPagamento());

        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().getStatus()).isEqualTo(StatusVenda.PAGAMENTO_APROVADO);
    }
}
