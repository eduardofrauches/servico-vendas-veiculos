package com.revendaveiculos.servicovendas.domain.exception;

public class VeiculoNaoEncontradoException extends RuntimeException {

    public VeiculoNaoEncontradoException(Long id) {
        super("Veiculo nao encontrado: " + id);
    }
}
