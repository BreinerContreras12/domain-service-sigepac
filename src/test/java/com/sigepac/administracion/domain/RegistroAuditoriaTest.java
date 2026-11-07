package com.sigepac.administracion.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sigepac.administracion.domain.exception.ValorInvalidoException;
import com.sigepac.administracion.domain.model.AccionAuditada;
import com.sigepac.administracion.domain.model.RegistroAuditoria;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RegistroAuditoriaTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 20, 10, 0);

    @Test
    @DisplayName("Un registro guarda quién, qué, sobre qué y cuándo")
    void guardaLosDatos() {
        RegistroAuditoria r = RegistroAuditoria.registrar(AccionAuditada.PAGO_ANULADO, 5, 2, " ana ", AHORA, "motivo");
        assertEquals("PAGO", r.getEntidad());
        assertEquals(5, r.getEntidadId());
        assertEquals(2, r.getNegocioId());
        assertEquals("ana", r.getUsuario());
        assertEquals(AHORA, r.getFechaHora());
    }

    @Test
    @DisplayName("Sin usuario, sin fecha o sin acción no hay auditoría")
    void datosObligatorios() {
        assertThrows(ValorInvalidoException.class,
                () -> RegistroAuditoria.registrar(AccionAuditada.NEGOCIO_REGISTRADO, 1, 1, " ", AHORA, null));
        assertThrows(ValorInvalidoException.class,
                () -> RegistroAuditoria.registrar(AccionAuditada.NEGOCIO_REGISTRADO, 1, 1, null, AHORA, null));
        assertThrows(ValorInvalidoException.class,
                () -> RegistroAuditoria.registrar(AccionAuditada.NEGOCIO_REGISTRADO, 1, 1, "ana", null, null));
        assertThrows(ValorInvalidoException.class,
                () -> RegistroAuditoria.registrar(null, 1, 1, "ana", AHORA, null));
    }
}
