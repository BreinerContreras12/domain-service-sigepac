package com.sigepac.administracion.infrastructure.adapters.in.rest.dto;

import com.sigepac.administracion.domain.model.Pago;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PagoResponse(Long id, long negocioId, LocalDate fecha, BigDecimal monto, String estado,
                           List<AplicacionResponse> aplicaciones) {

    public record AplicacionResponse(long deudaId, BigDecimal monto) { }

    public static PagoResponse de(Pago p) {
        return new PagoResponse(p.getId(), p.getNegocioId(), p.getFecha(), p.getMonto().valor(), p.getEstado().name(),
                p.getAplicaciones().stream().map(a -> new AplicacionResponse(a.deudaId(), a.monto().valor())).toList());
    }
}
