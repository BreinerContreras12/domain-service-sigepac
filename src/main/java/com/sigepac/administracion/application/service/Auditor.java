package com.sigepac.administracion.application.service;

import com.sigepac.administracion.application.port.out.AuditoriaRepositoryPort;
import com.sigepac.administracion.application.port.out.UsuarioActualPort;
import com.sigepac.administracion.domain.model.AccionAuditada;
import com.sigepac.administracion.domain.model.RegistroAuditoria;
import java.time.Clock;
import java.time.LocalDateTime;

/**
 * HU-12: escribe una línea de auditoría con el usuario del token y la hora del reloj inyectado.
 * Los casos de uso lo llaman DENTRO de su transacción: si la operación falla, la auditoría tampoco queda.
 */
public class Auditor {

    private final AuditoriaRepositoryPort auditoria;
    private final UsuarioActualPort usuarioActual;
    private final Clock reloj;

    public Auditor(AuditoriaRepositoryPort auditoria, UsuarioActualPort usuarioActual, Clock reloj) {
        this.auditoria = auditoria;
        this.usuarioActual = usuarioActual;
        this.reloj = reloj;
    }

    public void registrar(AccionAuditada accion, long entidadId, long negocioId, String detalle) {
        auditoria.guardar(RegistroAuditoria.registrar(accion, entidadId, negocioId,
                usuarioActual.nombreUsuario(), LocalDateTime.now(reloj), detalle));
    }
}
