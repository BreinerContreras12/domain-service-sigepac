package com.sigepac.administracion.application.port.in;

import com.sigepac.administracion.domain.model.Pago;

public interface AnularPagoUseCase {
    Pago anular(long pagoId);
}
