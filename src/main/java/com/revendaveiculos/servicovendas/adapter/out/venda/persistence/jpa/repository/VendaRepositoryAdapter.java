package com.revendaveiculos.servicovendas.adapter.out.venda.persistence.jpa.repository;

import com.revendaveiculos.servicovendas.adapter.out.venda.persistence.jpa.entity.VendaEntity;
import com.revendaveiculos.servicovendas.adapter.out.venda.persistence.jpa.mapper.VendaEntityMapper;
import com.revendaveiculos.servicovendas.application.venda.port.out.VendaRepositoryPort;
import com.revendaveiculos.servicovendas.domain.model.venda.Venda;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class VendaRepositoryAdapter implements VendaRepositoryPort {

    private final VendaJpaRepository vendaJpaRepository;
    private final VendaEntityMapper vendaEntityMapper;

    public VendaRepositoryAdapter(VendaJpaRepository vendaJpaRepository, VendaEntityMapper vendaEntityMapper) {
        this.vendaJpaRepository = vendaJpaRepository;
        this.vendaEntityMapper = vendaEntityMapper;
    }

    @Override
    public Venda salvar(Venda venda) {
        VendaEntity entitySalva = vendaJpaRepository.save(vendaEntityMapper.paraEntity(venda));
        return vendaEntityMapper.paraDominio(entitySalva);
    }

    @Override
    public Optional<Venda> buscarPorCodigoPagamento(String codigoPagamento) {
        return vendaJpaRepository.findByCodigoPagamento(codigoPagamento).map(vendaEntityMapper::paraDominio);
    }
}
