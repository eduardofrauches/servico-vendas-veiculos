package com.revendaveiculos.servicovendas.adapter.out.veiculo.persistence.jpa.repository;

import com.revendaveiculos.servicovendas.adapter.out.veiculo.persistence.jpa.entity.VeiculoEntity;
import com.revendaveiculos.servicovendas.adapter.out.veiculo.persistence.jpa.mapper.VeiculoEntityMapper;
import com.revendaveiculos.servicovendas.application.veiculo.port.out.VeiculoRepositoryPort;
import com.revendaveiculos.servicovendas.domain.model.veiculo.StatusVeiculo;
import com.revendaveiculos.servicovendas.domain.model.veiculo.Veiculo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class VeiculoRepositoryAdapter implements VeiculoRepositoryPort {

    private final VeiculoJpaRepository veiculoJpaRepository;
    private final VeiculoEntityMapper veiculoEntityMapper;

    public VeiculoRepositoryAdapter(VeiculoJpaRepository veiculoJpaRepository, VeiculoEntityMapper veiculoEntityMapper) {
        this.veiculoJpaRepository = veiculoJpaRepository;
        this.veiculoEntityMapper = veiculoEntityMapper;
    }

    @Override
    public Veiculo salvar(Veiculo veiculo) {
        VeiculoEntity entitySalva = veiculoJpaRepository.save(veiculoEntityMapper.paraEntity(veiculo));
        return veiculoEntityMapper.paraDominio(entitySalva);
    }

    @Override
    public Optional<Veiculo> buscarPorId(Long id) {
        return veiculoJpaRepository.findById(id).map(veiculoEntityMapper::paraDominio);
    }

    @Override
    public List<Veiculo> listarDisponiveis() {
        return veiculoJpaRepository.findByStatus(StatusVeiculo.DISPONIVEL).stream()
                .map(veiculoEntityMapper::paraDominio)
                .toList();
    }

    @Override
    public List<Veiculo> listarVendidos() {
        return veiculoJpaRepository.findByStatus(StatusVeiculo.VENDIDO).stream()
                .map(veiculoEntityMapper::paraDominio)
                .toList();
    }
}
