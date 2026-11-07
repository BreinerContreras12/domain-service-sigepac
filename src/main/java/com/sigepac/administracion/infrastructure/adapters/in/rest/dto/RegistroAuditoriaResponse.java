package com.sigepac.administracion.infrastructure.adapters.in.rest.dto;

import com.sigepac.administracion.domain.model.RegistroAuditoria;
import java.time.LocalDateTime;

public record RegistroAuditoriaResponse(Long id, String accion, String entidad, long entidadId, long negocioId,
                                        String usuario, LocalDateTime fechaHora, String detalle) {

    public static RegistroAuditoriaResponse de(RegistroAuditoria r) {
        return new RegistroAuditoriaResponse(r.getId(), r.getAccion().name(), r.getEntidad(), r.getEntidadId(),
                r.getNegocioId(), r.getUsuario(), r.getFechaHora(), r.getDetalle());
    }
}
