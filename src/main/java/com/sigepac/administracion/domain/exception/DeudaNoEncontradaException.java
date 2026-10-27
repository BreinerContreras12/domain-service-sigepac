package com.sigepac.administracion.domain.exception;

public class DeudaNoEncontradaException extends NoEncontradoException {

    public DeudaNoEncontradaException(long id) {
        super("La deuda " + id + " no existe");
    }
}
