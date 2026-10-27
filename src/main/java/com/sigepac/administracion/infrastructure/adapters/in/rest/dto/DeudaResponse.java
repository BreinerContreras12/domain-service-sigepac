package com.sigepac.administracion.infrastructure.adapters.in.rest.dto;

import com.sigepac.administracion.domain.model.Deuda;
import java.math.BigDecimal;

public record DeudaResponse(Long id, long negocioId, String concepto, int anio, int mes, BigDecimal valor,
                            BigDecimal pagado, BigDecimal saldo, String estado) {

    public static DeudaResponse de(Deuda d) {
        return new DeudaResponse(d.getId(), d.getNegocioId(), d.getConcepto().name(), d.getPeriodo().anio(),
                d.getPeriodo().mes(), d.getValor().valor(), d.getPagado().valor(), d.saldo().valor(), d.estado().name());
    }
}
