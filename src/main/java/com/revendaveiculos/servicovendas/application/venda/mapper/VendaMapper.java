package com.revendaveiculos.servicovendas.application.venda.mapper;

import com.revendaveiculos.servicovendas.application.venda.dto.response.VendaResponse;
import com.revendaveiculos.servicovendas.domain.model.venda.Venda;
import org.springframework.stereotype.Component;

@Component
public class VendaMapper {

    public VendaResponse paraResponse(Venda venda) {
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
