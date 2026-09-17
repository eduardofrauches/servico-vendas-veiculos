package com.revendaveiculos.servicovendas.domain.exception;

public class VeiculoNaoDisponivelException extends RuntimeException {

    public VeiculoNaoDisponivelException(String message) {
        super(message);
    }
}
