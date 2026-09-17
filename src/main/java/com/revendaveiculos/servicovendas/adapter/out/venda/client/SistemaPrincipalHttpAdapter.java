package com.revendaveiculos.servicovendas.adapter.out.venda.client;

import com.revendaveiculos.servicovendas.application.venda.port.out.SistemaPrincipalPort;
import com.revendaveiculos.servicovendas.domain.model.venda.Venda;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Implementa SistemaPrincipalPort chamando o sistema-principal-veiculos
 * via HTTP (PATCH /veiculos/{id}/status). A URL base vem de
 * application.yml (sistema-principal.base-url), nunca hardcoded.
 *
 * Mapeia cada status de Venda para o status de Veiculo correspondente no
 * sistema-principal: alem do resultado final (aprovado/cancelado), tambem
 * propaga a reserva (PENDENTE -> RESERVADO) assim que a venda e efetuada —
 * sem isso, o sistema-principal nunca saberia que o veiculo esta em
 * processo de venda e sua propria maquina de estados rejeitaria o salto
 * direto de DISPONIVEL para VENDIDO na hora de confirmar o pagamento.
 */
@Component
public class SistemaPrincipalHttpAdapter implements SistemaPrincipalPort {

    private static final Logger log = LoggerFactory.getLogger(SistemaPrincipalHttpAdapter.class);

    private final RestTemplate restTemplate;
    private final String sistemaPrincipalBaseUrl;

    public SistemaPrincipalHttpAdapter(RestTemplate restTemplate,
                                        @Value("${sistema-principal.base-url}") String sistemaPrincipalBaseUrl) {
        this.restTemplate = restTemplate;
        this.sistemaPrincipalBaseUrl = sistemaPrincipalBaseUrl;
    }

    @Override
    public void notificarResultadoVenda(Venda venda) {
        String statusVeiculo = switch (venda.getStatus()) {
            case PENDENTE -> "RESERVADO";
            case PAGAMENTO_APROVADO -> "VENDIDO";
            case PAGAMENTO_CANCELADO -> "DISPONIVEL";
        };
        AtualizarStatusVeiculoPayload payload = new AtualizarStatusVeiculoPayload(statusVeiculo);

        String url = sistemaPrincipalBaseUrl + "/veiculos/" + venda.getVeiculoId() + "/status";
        try {
            restTemplate.exchange(url, HttpMethod.PATCH, new HttpEntity<>(payload), Void.class);
        } catch (RestClientException e) {
            log.warn("Falha ao notificar o sistema-principal-veiculos sobre a venda {} (veiculo {}): {}",
                    venda.getId(), venda.getVeiculoId(), e.getMessage());
        }
    }
}
