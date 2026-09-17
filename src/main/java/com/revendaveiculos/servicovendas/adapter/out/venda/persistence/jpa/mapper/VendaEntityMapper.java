package com.revendaveiculos.servicovendas.adapter.out.venda.persistence.jpa.mapper;

import com.revendaveiculos.servicovendas.adapter.out.venda.persistence.jpa.entity.VendaEntity;
import com.revendaveiculos.servicovendas.domain.model.venda.Venda;
import com.revendaveiculos.servicovendas.domain.vo.Cpf;
import com.revendaveiculos.servicovendas.domain.vo.Preco;
import org.springframework.stereotype.Component;

@Component
public class VendaEntityMapper {

    public VendaEntity paraEntity(Venda venda) {
        return new VendaEntity(
                venda.getId(),
                venda.getVeiculoId(),
                venda.getCpfComprador().numero(),
                venda.getDataVenda(),
                venda.getValorVenda().valor(),
                venda.getStatus(),
                venda.getCodigoPagamento()
        );
    }

    public Venda paraDominio(VendaEntity entity) {
        return Venda.restaurar(
                entity.getId(),
                entity.getVeiculoId(),
                Cpf.de(entity.getCpfComprador()),
                entity.getDataVenda(),
                Preco.de(entity.getValorVenda()),
                entity.getStatus(),
                entity.getCodigoPagamento()
        );
    }
}
