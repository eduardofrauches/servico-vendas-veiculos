package com.revendaveiculos.servicovendas.domain.exception;

public class TransicaoStatusInvalidaException extends RuntimeException {

    public TransicaoStatusInvalidaException(String message) {
        super(message);
    }
}
