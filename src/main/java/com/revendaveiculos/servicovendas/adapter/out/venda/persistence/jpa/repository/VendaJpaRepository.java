package com.revendaveiculos.servicovendas.adapter.out.venda.persistence.jpa.repository;

import com.revendaveiculos.servicovendas.adapter.out.venda.persistence.jpa.entity.VendaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VendaJpaRepository extends JpaRepository<VendaEntity, Long> {

    Optional<VendaEntity> findByCodigoPagamento(String codigoPagamento);
}
