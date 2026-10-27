package com.sigepac.administracion.domain.model;

import com.sigepac.administracion.domain.exception.ValorInvalidoException;
import com.sigepac.administracion.domain.vo.Dinero;

/** Cuánto de un pago se aplicó a una deuda. Se guarda para poder anular el pago devolviendo exactamente lo aplicado. */
public record AplicacionPago(long deudaId, Dinero monto) {

    public AplicacionPago {
        if (monto == null || !monto.esMayorQueCero()) {
            throw new ValorInvalidoException("El monto aplicado debe ser mayor que cero");
        }
    }
}
