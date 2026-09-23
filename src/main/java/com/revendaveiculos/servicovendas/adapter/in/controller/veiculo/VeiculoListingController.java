package com.revendaveiculos.servicovendas.adapter.in.controller.veiculo;

import com.revendaveiculos.servicovendas.adapter.in.presenter.veiculo.VeiculoPresenter;
import com.revendaveiculos.servicovendas.application.veiculo.dto.request.SincronizarVeiculoRequest;
import com.revendaveiculos.servicovendas.application.veiculo.dto.response.VeiculoResponse;
import com.revendaveiculos.servicovendas.application.veiculo.port.in.ListarVeiculosDisponiveisInputPort;
import com.revendaveiculos.servicovendas.application.veiculo.port.in.ListarVeiculosVendidosInputPort;
import com.revendaveiculos.servicovendas.application.veiculo.port.in.SincronizarVeiculoInputPort;
import com.revendaveiculos.servicovendas.domain.model.veiculo.Veiculo;
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
    private final VeiculoPresenter veiculoPresenter;

    public VeiculoListingController(ListarVeiculosDisponiveisInputPort listarVeiculosDisponiveisInputPort,
                                     ListarVeiculosVendidosInputPort listarVeiculosVendidosInputPort,
                                     SincronizarVeiculoInputPort sincronizarVeiculoInputPort,
                                     VeiculoPresenter veiculoPresenter) {
        this.listarVeiculosDisponiveisInputPort = listarVeiculosDisponiveisInputPort;
        this.listarVeiculosVendidosInputPort = listarVeiculosVendidosInputPort;
        this.sincronizarVeiculoInputPort = sincronizarVeiculoInputPort;
        this.veiculoPresenter = veiculoPresenter;
    }

    @GetMapping("/disponiveis")
    public ResponseEntity<List<VeiculoResponse>> listarDisponiveis() {
        List<Veiculo> veiculos = listarVeiculosDisponiveisInputPort.listar();
        return ResponseEntity.ok(veiculoPresenter.apresentarLista(veiculos));
    }

    @GetMapping("/vendidos")
    public ResponseEntity<List<VeiculoResponse>> listarVendidos() {
        List<Veiculo> veiculos = listarVeiculosVendidosInputPort.listar();
        return ResponseEntity.ok(veiculoPresenter.apresentarLista(veiculos));
    }

    /** Chamado pelo sistema-principal-veiculos a cada cadastro/edicao de veiculo. */
    @PostMapping("/sync")
    public ResponseEntity<VeiculoResponse> sincronizar(@Valid @RequestBody SincronizarVeiculoRequest request) {
        Veiculo veiculo = sincronizarVeiculoInputPort.sincronizar(request);
        return ResponseEntity.status(HttpStatus.OK).body(veiculoPresenter.apresentar(veiculo));
    }
}
