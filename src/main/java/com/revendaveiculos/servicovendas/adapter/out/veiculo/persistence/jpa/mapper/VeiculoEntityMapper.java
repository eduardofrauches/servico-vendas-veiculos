package com.revendaveiculos.servicovendas.adapter.out.veiculo.persistence.jpa.mapper;

import com.revendaveiculos.servicovendas.adapter.out.veiculo.persistence.jpa.entity.VeiculoEntity;
import com.revendaveiculos.servicovendas.domain.model.veiculo.Veiculo;
import com.revendaveiculos.servicovendas.domain.vo.Preco;
import org.springframework.stereotype.Component;

@Component
public class VeiculoEntityMapper {

    public VeiculoEntity paraEntity(Veiculo veiculo) {
        return new VeiculoEntity(
                veiculo.getId(),
                veiculo.getMarca(),
                veiculo.getModelo(),
                veiculo.getAno(),
                veiculo.getCor(),
                veiculo.getPreco().valor(),
                veiculo.getStatus()
        );
    }

    public Veiculo paraDominio(VeiculoEntity entity) {
        return Veiculo.restaurar(
                entity.getId(),
                entity.getMarca(),
                entity.getModelo(),
                entity.getAno(),
                entity.getCor(),
                Preco.de(entity.getPreco()),
                entity.getStatus()
        );
    }
}
