package com.sigepac.administracion.domain.exception;

import com.sigepac.administracion.domain.vo.ConceptoDeuda;
import com.sigepac.administracion.domain.vo.Periodo;

public class DeudaDuplicadaException extends DuplicadoException {

    public DeudaDuplicadaException(ConceptoDeuda concepto, Periodo periodo) {
        super("Ya existe una deuda de " + concepto + " para el período " + periodo + " en este negocio");
    }
}
