package com.revendaveiculos.servicovendas.adapter.in.exception;

import com.revendaveiculos.servicovendas.domain.exception.CpfInvalidoException;
import com.revendaveiculos.servicovendas.domain.exception.PrecoInvalidoException;
import com.revendaveiculos.servicovendas.domain.exception.TransicaoStatusInvalidaException;
import com.revendaveiculos.servicovendas.domain.exception.VeiculoNaoDisponivelException;
import com.revendaveiculos.servicovendas.domain.exception.VeiculoNaoEncontradoException;
import com.revendaveiculos.servicovendas.domain.exception.VendaNaoEncontradaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Traduz exceptions de dominio/validacao em respostas HTTP consistentes.
 * Nao depende de nenhum adapter/porta especifica — so de exceptions.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> tratarValidacao(MethodArgumentNotValidException ex) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.de(HttpStatus.BAD_REQUEST.value(), "Requisicao invalida",
                        "Um ou mais campos sao invalidos", detalhes));
    }

    @ExceptionHandler({PrecoInvalidoException.class, CpfInvalidoException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> tratarRequisicaoInvalida(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.de(HttpStatus.BAD_REQUEST.value(), "Requisicao invalida", ex.getMessage()));
    }

    @ExceptionHandler({VeiculoNaoEncontradoException.class, VendaNaoEncontradaException.class})
    public ResponseEntity<ErrorResponse> tratarNaoEncontrado(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.de(HttpStatus.NOT_FOUND.value(), "Recurso nao encontrado", ex.getMessage()));
    }

    @ExceptionHandler({TransicaoStatusInvalidaException.class, VeiculoNaoDisponivelException.class})
    public ResponseEntity<ErrorResponse> tratarConflitoDeEstado(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.de(HttpStatus.CONFLICT.value(), "Conflito de estado", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> tratarErroInesperado(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.de(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Erro interno",
                        "Ocorreu um erro inesperado"));
    }
}
