package com.revendaveiculos.servicovendas.adapter.in.presenter.veiculo;

import com.revendaveiculos.servicovendas.application.veiculo.dto.response.VeiculoResponse;
import com.revendaveiculos.servicovendas.domain.model.veiculo.Veiculo;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Unica responsavel por formatar a Entity de dominio Veiculo na resposta
 * HTTP (VeiculoResponse). Os UseCases devolvem a Entity (ou lista de
 * Entity); o Controller chama este Presenter antes de montar o
 * ResponseEntity.
 */
@Component
public class VeiculoPresenter {

    public VeiculoResponse apresentar(Veiculo veiculo) {
        return new VeiculoResponse(
                veiculo.getId(),
                veiculo.getMarca(),
                veiculo.getModelo(),
                veiculo.getAno(),
                veiculo.getCor(),
                veiculo.getPreco().valor(),
                veiculo.getStatus().name()
        );
    }

    public List<VeiculoResponse> apresentarLista(List<Veiculo> veiculos) {
        return veiculos.stream().map(this::apresentar).toList();
    }
}
