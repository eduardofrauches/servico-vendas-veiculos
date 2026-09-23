package com.revendaveiculos.servicovendas.application.venda.port.in;

import com.revendaveiculos.servicovendas.application.venda.dto.request.EfetuarVendaRequest;
import com.revendaveiculos.servicovendas.domain.model.venda.Venda;

public interface EfetuarVendaInputPort {

    Venda efetuar(EfetuarVendaRequest request);
}
