package com.revendaveiculos.servicovendas.infrastructure.config.venda;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    /**
     * Usa JdkClientHttpRequestFactory (java.net.http.HttpClient) em vez do
     * SimpleClientHttpRequestFactory padrao, pois este ultimo nao suporta o
     * metodo PATCH usado pelo SistemaPrincipalHttpAdapter. O timeout de
     * conexao e configurado no proprio HttpClient (RestTemplateBuilder nao
     * consegue configura-lo via reflection para essa factory).
     */
    @Bean
    public RestTemplate restTemplate() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        return new RestTemplate(requestFactory);
    }
}
