package com.sigepac.administracion.domain.exception;

public class NegocioNoEncontradoException extends NoEncontradoException {

    public NegocioNoEncontradoException(long id) {
        super("El negocio " + id + " no existe: regístrelo antes de continuar");
    }
}
