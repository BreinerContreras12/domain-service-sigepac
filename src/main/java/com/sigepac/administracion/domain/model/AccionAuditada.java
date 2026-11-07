package com.sigepac.administracion.domain.model;

/** HU-12: los cambios que quedan registrados en la auditoría y sobre qué tipo de registro ocurren. */
public enum AccionAuditada {
    NEGOCIO_REGISTRADO("NEGOCIO"),
    NEGOCIO_EDITADO("NEGOCIO"),
    NEGOCIO_DADO_DE_BAJA("NEGOCIO"),
    DEUDA_REGISTRADA("DEUDA"),
    DEUDA_ACTUALIZADA("DEUDA"),
    PAGO_REGISTRADO("PAGO"),
    PAGO_ANULADO("PAGO");

    private final String entidad;

    AccionAuditada(String entidad) {
        this.entidad = entidad;
    }

    public String entidad() {
        return entidad;
    }
}
