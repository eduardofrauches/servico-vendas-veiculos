package com.revendaveiculos.servicovendas.domain.exception;

public class CpfInvalidoException extends RuntimeException {

    public CpfInvalidoException(String message) {
        super(message);
    }
}
