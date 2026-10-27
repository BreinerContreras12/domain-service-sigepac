package com.sigepac.administracion.infrastructure.adapters.out.persistence.mapper;

import com.sigepac.administracion.domain.model.AplicacionPago;
import com.sigepac.administracion.domain.model.EstadoPago;
import com.sigepac.administracion.domain.model.Pago;
import com.sigepac.administracion.domain.vo.Dinero;
import com.sigepac.administracion.infrastructure.adapters.out.persistence.entity.AplicacionPagoEmbeddable;
import com.sigepac.administracion.infrastructure.adapters.out.persistence.entity.PagoEntity;

public final class PagoMapper {

    private PagoMapper() { }

    public static Pago aDominio(PagoEntity e) {
        var aplicaciones = e.getAplicaciones().stream()
                .map(a -> new AplicacionPago(a.getDeudaId(), Dinero.de(a.getMonto()))).toList();
        return Pago.reconstruir(e.getId(), e.getNegocioId(), e.getFecha(), Dinero.de(e.getMonto()),
                EstadoPago.valueOf(e.getEstado()), aplicaciones);
    }

    public static void copiarA(PagoEntity e, Pago p) {
        e.setNegocioId(p.getNegocioId());
        e.setFecha(p.getFecha());
        e.setMonto(p.getMonto().valor());
        e.setEstado(p.getEstado().name());
        if (e.getAplicaciones().isEmpty()) {   // las aplicaciones no cambian después de creado el pago (solo se anula)
            p.getAplicaciones().forEach(a -> e.getAplicaciones().add(
                    new AplicacionPagoEmbeddable(a.deudaId(), a.monto().valor())));
        }
    }
}
