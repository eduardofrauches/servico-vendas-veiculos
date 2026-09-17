package com.revendaveiculos.servicovendas.adapter.out.veiculo.persistence.jpa.repository;

import com.revendaveiculos.servicovendas.adapter.out.veiculo.persistence.jpa.entity.VeiculoEntity;
import com.revendaveiculos.servicovendas.domain.model.veiculo.StatusVeiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VeiculoJpaRepository extends JpaRepository<VeiculoEntity, Long> {

    List<VeiculoEntity> findByStatus(StatusVeiculo status);
}
