package com.sigepac.administracion.domain.exception;

public class NegocioInactivoException extends DominioException {

    public NegocioInactivoException(Long id) {
        super("El negocio " + id + " está INACTIVO y no admite esta operación");
    }
}
