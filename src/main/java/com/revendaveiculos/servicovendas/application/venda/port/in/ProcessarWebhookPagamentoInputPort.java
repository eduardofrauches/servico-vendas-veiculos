package com.revendaveiculos.servicovendas.application.venda.port.in;

import com.revendaveiculos.servicovendas.application.venda.dto.request.WebhookPagamentoRequest;
import com.revendaveiculos.servicovendas.application.venda.dto.response.VendaResponse;

public interface ProcessarWebhookPagamentoInputPort {

    VendaResponse processar(WebhookPagamentoRequest request);
}
