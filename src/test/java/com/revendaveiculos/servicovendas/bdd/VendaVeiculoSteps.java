package com.revendaveiculos.servicovendas.bdd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Steps do fluxo de venda. O cadastro do veiculo (que na arquitetura real
 * acontece no sistema-principal-veiculos) e simulado aqui diretamente via
 * POST /veiculos/sync — o mesmo callback que o outro servico chamaria.
 */
public class VendaVeiculoSteps {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String codigoPagamento;

    @Dado("que o veiculo de id {int} esta cadastrado e disponivel para venda")
    public void veiculoCadastradoEDisponivel(int veiculoId) throws Exception {
        String payload = """
                {
                  "id": %d,
                  "marca": "Toyota",
                  "modelo": "Corolla",
                  "ano": 2022,
                  "cor": "Prata",
                  "preco": 95000.00,
                  "status": "DISPONIVEL"
                }
                """.formatted(veiculoId);

        mockMvc.perform(post("/veiculos/sync")
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isOk());
    }

    @Quando("o comprador de CPF {string} efetua a compra do veiculo {int}")
    public void efetuarCompra(String cpf, int veiculoId) throws Exception {
        String payload = """
                {
                  "veiculoId": %d,
                  "cpfComprador": "%s"
                }
                """.formatted(veiculoId, cpf);

        MvcResult result = mockMvc.perform(post("/vendas")
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode venda = objectMapper.readTree(result.getResponse().getContentAsString());
        codigoPagamento = venda.get("codigoPagamento").asText();
        assertThat(codigoPagamento).isNotBlank();
    }

    @E("o pagamento da venda e aprovado")
    public void pagamentoAprovado() throws Exception {
        processarWebhook("APROVADO");
    }

    @E("o pagamento da venda e cancelado")
    public void pagamentoCancelado() throws Exception {
        processarWebhook("CANCELADO");
    }

    private void processarWebhook(String resultado) throws Exception {
        String payload = """
                {
                  "codigoPagamento": "%s",
                  "resultado": "%s"
                }
                """.formatted(codigoPagamento, resultado);

        mockMvc.perform(post("/webhooks/pagamento")
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isOk());
    }

    @Entao("o veiculo {int} deve aparecer na listagem de veiculos vendidos")
    public void veiculoDeveApareceComoVendido(int veiculoId) throws Exception {
        MvcResult result = mockMvc.perform(get("/veiculos/vendidos"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode veiculos = objectMapper.readTree(result.getResponse().getContentAsString());
        boolean encontrado = idPresenteNaLista(veiculos, veiculoId);

        assertThat(encontrado)
                .as("veiculo %d deveria aparecer em /veiculos/vendidos: %s", veiculoId, veiculos)
                .isTrue();
    }

    @Entao("o veiculo {int} deve aparecer na listagem de veiculos disponiveis")
    public void veiculoDeveApareceComoDisponivel(int veiculoId) throws Exception {
        MvcResult result = mockMvc.perform(get("/veiculos/disponiveis"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode veiculos = objectMapper.readTree(result.getResponse().getContentAsString());
        boolean encontrado = idPresenteNaLista(veiculos, veiculoId);

        assertThat(encontrado)
                .as("veiculo %d deveria aparecer em /veiculos/disponiveis: %s", veiculoId, veiculos)
                .isTrue();
    }

    private boolean idPresenteNaLista(JsonNode lista, int veiculoId) {
        for (JsonNode veiculo : lista) {
            if (veiculo.get("id").asLong() == veiculoId) {
                return true;
            }
        }
        return false;
    }
}
