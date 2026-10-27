package com.sigepac.administracion.infrastructure.adapters.in.rest.dto;

import com.sigepac.administracion.application.port.in.result.ResumenNegocio;
import java.math.BigDecimal;

public record ResumenNegocioResponse(NegocioResponse negocio, BigDecimal saldoPendiente) {

    public static ResumenNegocioResponse de(ResumenNegocio r) {
        return new ResumenNegocioResponse(NegocioResponse.de(r.negocio()), r.saldoPendiente().valor());
    }
}
