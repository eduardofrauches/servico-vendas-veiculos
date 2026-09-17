package com.revendaveiculos.servicovendas.domain.exception;

public class VendaNaoEncontradaException extends RuntimeException {

    public VendaNaoEncontradaException(String codigoPagamento) {
        super("Venda nao encontrada para o codigoPagamento: " + codigoPagamento);
    }
}
