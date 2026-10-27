package com.sigepac.administracion.domain.exception;

import com.sigepac.administracion.domain.vo.CodigoLocal;

public class LocalOcupadoException extends DuplicadoException {

    public LocalOcupadoException(CodigoLocal local, String nombreNegocio) {
        super("El local " + local + " ya está ocupado por el negocio '" + nombreNegocio + "'");
    }
}
