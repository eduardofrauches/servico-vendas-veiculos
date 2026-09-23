package com.revendaveiculos.servicovendas.application.veiculo.mapper;

import com.revendaveiculos.servicovendas.application.veiculo.dto.request.SincronizarVeiculoRequest;
import com.revendaveiculos.servicovendas.domain.model.veiculo.Veiculo;
import com.revendaveiculos.servicovendas.domain.vo.Preco;
import org.springframework.stereotype.Component;

@Component
public class VeiculoMapper {

    public Veiculo paraDominio(SincronizarVeiculoRequest request) {
        return Veiculo.sincronizar(
                request.id(),
                request.marca(),
                request.modelo(),
                request.ano(),
                request.cor(),
                Preco.de(request.preco()),
                request.status()
        );
    }
}
