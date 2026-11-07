package com.sigepac.administracion.infrastructure.adapters.out.persistence.mapper;

import com.sigepac.administracion.domain.model.AccionAuditada;
import com.sigepac.administracion.domain.model.RegistroAuditoria;
import com.sigepac.administracion.infrastructure.adapters.out.persistence.entity.RegistroAuditoriaEntity;

public final class AuditoriaMapper {

    private AuditoriaMapper() { }

    public static RegistroAuditoria aDominio(RegistroAuditoriaEntity e) {
        return RegistroAuditoria.reconstruir(e.getId(), AccionAuditada.valueOf(e.getAccion()), e.getEntidadId(),
                e.getNegocioId(), e.getUsuario(), e.getFechaHora(), e.getDetalle());
    }

    public static RegistroAuditoriaEntity aEntidad(RegistroAuditoria r) {
        RegistroAuditoriaEntity e = RegistroAuditoriaEntity.nueva();
        e.setAccion(r.getAccion().name());
        e.setEntidadId(r.getEntidadId());
        e.setNegocioId(r.getNegocioId());
        e.setUsuario(r.getUsuario());
        e.setFechaHora(r.getFechaHora());
        e.setDetalle(r.getDetalle());
        return e;
    }
}
