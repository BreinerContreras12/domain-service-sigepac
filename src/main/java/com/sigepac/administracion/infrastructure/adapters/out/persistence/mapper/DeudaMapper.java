package com.sigepac.administracion.infrastructure.adapters.out.persistence.mapper;

import com.sigepac.administracion.domain.model.Deuda;
import com.sigepac.administracion.domain.vo.ConceptoDeuda;
import com.sigepac.administracion.domain.vo.Dinero;
import com.sigepac.administracion.domain.vo.Periodo;
import com.sigepac.administracion.infrastructure.adapters.out.persistence.entity.DeudaEntity;

public final class DeudaMapper {

    private DeudaMapper() { }

    public static Deuda aDominio(DeudaEntity e) {
        return Deuda.reconstruir(e.getId(), e.getNegocioId(), ConceptoDeuda.valueOf(e.getConcepto()),
                Periodo.de(e.getAnio(), e.getMes()), Dinero.de(e.getValor()), Dinero.de(e.getPagado()));
    }

    public static void copiarA(DeudaEntity e, Deuda d) {
        e.setNegocioId(d.getNegocioId());
        e.setConcepto(d.getConcepto().name());
        e.setAnio(d.getPeriodo().anio());
        e.setMes(d.getPeriodo().mes());
        e.setValor(d.getValor().valor());
        e.setPagado(d.getPagado().valor());
    }
}
