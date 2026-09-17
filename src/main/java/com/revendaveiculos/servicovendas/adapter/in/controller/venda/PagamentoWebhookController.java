package com.revendaveiculos.servicovendas.adapter.in.controller.venda;

import com.revendaveiculos.servicovendas.application.venda.dto.request.WebhookPagamentoRequest;
import com.revendaveiculos.servicovendas.application.venda.dto.response.VendaResponse;
import com.revendaveiculos.servicovendas.application.venda.port.in.ProcessarWebhookPagamentoInputPort;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Recebe o callback da entidade de pagamento externa (simulada). */
@RestController
@RequestMapping("/webhooks/pagamento")
public class PagamentoWebhookController {

    private final ProcessarWebhookPagamentoInputPort processarWebhookPagamentoInputPort;

    public PagamentoWebhookController(ProcessarWebhookPagamentoInputPort processarWebhookPagamentoInputPort) {
        this.processarWebhookPagamentoInputPort = processarWebhookPagamentoInputPort;
    }

    @PostMapping
    public ResponseEntity<VendaResponse> processar(@Valid @RequestBody WebhookPagamentoRequest request) {
        return ResponseEntity.ok(processarWebhookPagamentoInputPort.processar(request));
    }
}
