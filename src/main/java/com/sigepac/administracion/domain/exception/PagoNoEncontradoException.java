package com.sigepac.administracion.domain.exception;

public class PagoNoEncontradoException extends NoEncontradoException {

    public PagoNoEncontradoException(long id) {
        super("El pago " + id + " no existe");
    }
}
