package com.revendaveiculos.servicovendas.application.venda.port.in;

import com.revendaveiculos.servicovendas.application.venda.dto.request.EfetuarVendaRequest;
import com.revendaveiculos.servicovendas.application.venda.dto.response.VendaResponse;

public interface EfetuarVendaInputPort {

    VendaResponse efetuar(EfetuarVendaRequest request);
}
