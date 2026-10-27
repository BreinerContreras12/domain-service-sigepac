package com.sigepac.administracion.domain.exception;

public class PagoYaAnuladoException extends DominioException {

    public PagoYaAnuladoException(Long id) {
        super("El pago " + id + " ya fue anulado");
    }
}
