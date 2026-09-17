package com.revendaveiculos.servicovendas.adapter.in.controller.veiculo;

import com.revendaveiculos.servicovendas.application.veiculo.dto.request.SincronizarVeiculoRequest;
import com.revendaveiculos.servicovendas.application.veiculo.dto.response.VeiculoResponse;
import com.revendaveiculos.servicovendas.application.veiculo.port.in.ListarVeiculosDisponiveisInputPort;
import com.revendaveiculos.servicovendas.application.veiculo.port.in.ListarVeiculosVendidosInputPort;
import com.revendaveiculos.servicovendas.application.veiculo.port.in.SincronizarVeiculoInputPort;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/veiculos")
public class VeiculoListingController {

    private final ListarVeiculosDisponiveisInputPort listarVeiculosDisponiveisInputPort;
    private final ListarVeiculosVendidosInputPort listarVeiculosVendidosInputPort;
    private final SincronizarVeiculoInputPort sincronizarVeiculoInputPort;

    public VeiculoListingController(ListarVeiculosDisponiveisInputPort listarVeiculosDisponiveisInputPort,
                                     ListarVeiculosVendidosInputPort listarVeiculosVendidosInputPort,
                                     SincronizarVeiculoInputPort sincronizarVeiculoInputPort) {
        this.listarVeiculosDisponiveisInputPort = listarVeiculosDisponiveisInputPort;
        this.listarVeiculosVendidosInputPort = listarVeiculosVendidosInputPort;
        this.sincronizarVeiculoInputPort = sincronizarVeiculoInputPort;
    }

    @GetMapping("/disponiveis")
    public ResponseEntity<List<VeiculoResponse>> listarDisponiveis() {
        return ResponseEntity.ok(listarVeiculosDisponiveisInputPort.listar());
    }

    @GetMapping("/vendidos")
    public ResponseEntity<List<VeiculoResponse>> listarVendidos() {
        return ResponseEntity.ok(listarVeiculosVendidosInputPort.listar());
    }

    /** Chamado pelo sistema-principal-veiculos a cada cadastro/edicao de veiculo. */
    @PostMapping("/sync")
    public ResponseEntity<VeiculoResponse> sincronizar(@Valid @RequestBody SincronizarVeiculoRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(sincronizarVeiculoInputPort.sincronizar(request));
    }
}
