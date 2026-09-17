package com.revendaveiculos.servicovendas.domain.vo;

import com.revendaveiculos.servicovendas.domain.exception.CpfInvalidoException;

import java.util.Objects;

/**
 * Value Object que representa um CPF valido. Imutavel. Aceita o numero
 * com ou sem mascara na entrada, mas armazena somente os 11 digitos.
 */
public final class Cpf {

    private final String numero;

    private Cpf(String numero) {
        this.numero = numero;
    }

    public static Cpf de(String valorInformado) {
        if (valorInformado == null) {
            throw new CpfInvalidoException("CPF nao pode ser nulo");
        }
        String digitos = valorInformado.replaceAll("[^0-9]", "");

        if (digitos.length() != 11) {
            throw new CpfInvalidoException("CPF deve ter 11 digitos: " + valorInformado);
        }
        if (todosDigitosIguais(digitos)) {
            throw new CpfInvalidoException("CPF invalido: " + valorInformado);
        }
        if (!digitosVerificadoresValidos(digitos)) {
            throw new CpfInvalidoException("CPF invalido: " + valorInformado);
        }
        return new Cpf(digitos);
    }

    private static boolean todosDigitosIguais(String digitos) {
        return digitos.chars().distinct().count() == 1;
    }

    private static boolean digitosVerificadoresValidos(String digitos) {
        int primeiroDigito = calcularDigitoVerificador(digitos.substring(0, 9), 10);
        int segundoDigito = calcularDigitoVerificador(digitos.substring(0, 9) + primeiroDigito, 11);
        return digitos.equals(digitos.substring(0, 9) + primeiroDigito + segundoDigito);
    }

    private static int calcularDigitoVerificador(String base, int pesoInicial) {
        int soma = 0;
        int peso = pesoInicial;
        for (char c : base.toCharArray()) {
            soma += Character.getNumericValue(c) * peso;
            peso--;
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    /** Numero sem mascara, somente digitos. */
    public String numero() {
        return numero;
    }

    public String formatado() {
        return numero.substring(0, 3) + "." + numero.substring(3, 6) + "."
                + numero.substring(6, 9) + "-" + numero.substring(9, 11);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cpf other)) return false;
        return numero.equals(other.numero);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numero);
    }

    @Override
    public String toString() {
        return formatado();
    }
}
