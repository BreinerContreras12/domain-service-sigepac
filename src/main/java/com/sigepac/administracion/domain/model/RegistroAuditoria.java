package com.sigepac.administracion.domain.model;

import com.sigepac.administracion.domain.exception.ValorInvalidoException;
import java.time.LocalDateTime;

/**
 * HU-12: una línea de la bitácora de auditoría: quién hizo qué cambio, sobre qué registro y cuándo.
 * Es inmutable: una vez escrita no se edita ni se borra (igual que un pago, que se anula pero no se borra).
 */
public final class RegistroAuditoria {

    private final Long id;
    private final AccionAuditada accion;
    private final long entidadId;
    private final long negocioId;
    private final String usuario;
    private final LocalDateTime fechaHora;
    private final String detalle;

    private RegistroAuditoria(Long id, AccionAuditada accion, long entidadId, long negocioId, String usuario,
                              LocalDateTime fechaHora, String detalle) {
        if (accion == null) {
            throw new ValorInvalidoException("La acción auditada es obligatoria");
        }
        if (usuario == null || usuario.isBlank()) {
            throw new ValorInvalidoException("La auditoría necesita el usuario que hizo el cambio");
        }
        if (fechaHora == null) {
            throw new ValorInvalidoException("La auditoría necesita la fecha y hora del cambio");
        }
        this.id = id;
        this.accion = accion;
        this.entidadId = entidadId;
        this.negocioId = negocioId;
        this.usuario = usuario.trim();
        this.fechaHora = fechaHora;
        this.detalle = detalle == null ? "" : detalle;
    }

    /** `fechaHora` llega desde fuera (reloj inyectado) y `usuario` sale del token: el dominio no los inventa. */
    public static RegistroAuditoria registrar(AccionAuditada accion, long entidadId, long negocioId, String usuario,
                                              LocalDateTime fechaHora, String detalle) {
        return new RegistroAuditoria(null, accion, entidadId, negocioId, usuario, fechaHora, detalle);
    }

    public static RegistroAuditoria reconstruir(long id, AccionAuditada accion, long entidadId, long negocioId,
                                                String usuario, LocalDateTime fechaHora, String detalle) {
        return new RegistroAuditoria(id, accion, entidadId, negocioId, usuario, fechaHora, detalle);
    }

    public Long getId() { return id; }
    public AccionAuditada getAccion() { return accion; }
    public String getEntidad() { return accion.entidad(); }
    public long getEntidadId() { return entidadId; }
    public long getNegocioId() { return negocioId; }
    public String getUsuario() { return usuario; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public String getDetalle() { return detalle; }
}
