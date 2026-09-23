package com.revendaveiculos.servicovendas.adapter.in.presenter.venda;

import com.revendaveiculos.servicovendas.application.venda.dto.response.VendaResponse;
import com.revendaveiculos.servicovendas.domain.model.venda.Venda;
import org.springframework.stereotype.Component;

/**
 * Unica responsavel por formatar a Entity de dominio Venda na resposta HTTP
 * (VendaResponse). Os UseCases devolvem a Entity; o Controller chama este
 * Presenter antes de montar o ResponseEntity.
 */
@Component
public class VendaPresenter {

    public VendaResponse apresentar(Venda venda) {
        return new VendaResponse(
                venda.getId(),
                venda.getVeiculoId(),
                venda.getCpfComprador().numero(),
                venda.getDataVenda(),
                venda.getValorVenda().valor(),
                venda.getStatus().name(),
                venda.getCodigoPagamento()
        );
    }
}
