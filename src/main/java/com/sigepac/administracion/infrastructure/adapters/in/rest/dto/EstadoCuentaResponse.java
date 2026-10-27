package com.sigepac.administracion.infrastructure.adapters.in.rest.dto;

import com.sigepac.administracion.domain.service.EstadoCuenta;
import java.math.BigDecimal;
import java.util.List;

public record EstadoCuentaResponse(NegocioResponse negocio, List<DeudaResponse> deudas, List<PagoResponse> pagos,
                                   BigDecimal totalDeudas, BigDecimal totalPagado, BigDecimal saldo) {

    public static EstadoCuentaResponse de(EstadoCuenta e) {
        return new EstadoCuentaResponse(NegocioResponse.de(e.negocio()),
                e.deudas().stream().map(DeudaResponse::de).toList(),
                e.pagos().stream().map(PagoResponse::de).toList(),
                e.totalDeudas().valor(), e.totalPagado().valor(), e.saldo().valor());
    }
}
