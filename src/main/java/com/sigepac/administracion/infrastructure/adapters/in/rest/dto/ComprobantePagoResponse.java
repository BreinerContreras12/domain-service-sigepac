package com.sigepac.administracion.infrastructure.adapters.in.rest.dto;

import com.sigepac.administracion.application.port.in.result.ComprobantePago;
import java.math.BigDecimal;

public record ComprobantePagoResponse(PagoResponse pago, BigDecimal saldoAnterior, BigDecimal saldoNuevo) {

    public static ComprobantePagoResponse de(ComprobantePago c) {
        return new ComprobantePagoResponse(PagoResponse.de(c.pago()), c.saldoAnterior().valor(), c.saldoNuevo().valor());
    }
}
