package com.revendaveiculos.servicovendas.adapter.in.controller.venda;

import com.revendaveiculos.servicovendas.application.venda.dto.request.EfetuarVendaRequest;
import com.revendaveiculos.servicovendas.application.venda.dto.response.VendaResponse;
import com.revendaveiculos.servicovendas.application.venda.port.in.EfetuarVendaInputPort;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vendas")
public class VendaController {

    private final EfetuarVendaInputPort efetuarVendaInputPort;

    public VendaController(EfetuarVendaInputPort efetuarVendaInputPort) {
        this.efetuarVendaInputPort = efetuarVendaInputPort;
    }

    @PostMapping
    public ResponseEntity<VendaResponse> efetuar(@Valid @RequestBody EfetuarVendaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(efetuarVendaInputPort.efetuar(request));
    }
}
