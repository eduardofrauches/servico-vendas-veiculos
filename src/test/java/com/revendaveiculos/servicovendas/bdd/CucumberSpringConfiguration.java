package com.revendaveiculos.servicovendas.bdd;

import com.revendaveiculos.servicovendas.application.venda.port.out.SistemaPrincipalPort;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Contexto Spring compartilhado pelos steps do Cucumber. Sobe a aplicacao
 * inteira (controllers, usecases, adapters de persistencia) contra um
 * Postgres real via Testcontainers. O unico ponto simulado/mockado e a
 * comunicacao com o sistema-principal-veiculos (SistemaPrincipalPort) —
 * o cadastro do veiculo em si acontece no outro servico, entao aqui ele e
 * simulado diretamente via POST /veiculos/sync (o mesmo callback que o
 * sistema-principal chamaria de verdade).
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
public class CucumberSpringConfiguration {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @MockitoBean
    private SistemaPrincipalPort sistemaPrincipalPort;
}
