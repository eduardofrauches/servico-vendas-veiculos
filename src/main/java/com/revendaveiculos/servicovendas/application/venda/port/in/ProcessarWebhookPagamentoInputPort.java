package com.revendaveiculos.servicovendas.application.venda.port.in;

import com.revendaveiculos.servicovendas.application.venda.dto.request.WebhookPagamentoRequest;
import com.revendaveiculos.servicovendas.domain.model.venda.Venda;

public interface ProcessarWebhookPagamentoInputPort {

    Venda processar(WebhookPagamentoRequest request);
}
