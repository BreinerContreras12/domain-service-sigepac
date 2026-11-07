package com.sigepac.administracion.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sigepac.administracion.application.port.in.command.ActualizarDeudaCommand;
import com.sigepac.administracion.application.port.in.command.EditarNegocioCommand;
import com.sigepac.administracion.application.port.in.command.RegistrarPagoCommand;
import com.sigepac.administracion.domain.exception.NegocioNoEncontradoException;
import com.sigepac.administracion.domain.exception.PagoExcedeSaldoException;
import com.sigepac.administracion.domain.model.AccionAuditada;
import com.sigepac.administracion.domain.model.RegistroAuditoria;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** HU-12: cada cambio deja quién lo hizo (usuario del token) y cuándo (reloj inyectado). */
class AuditoriaServiceTest {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 20);
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 20, 10, 0); // reloj fijo, hora de Bogotá
    private final Escenario e = new Escenario();

    private List<AccionAuditada> acciones(long negocioId) {
        return e.auditoriaService.deNegocio(negocioId).stream().map(RegistroAuditoria::getAccion).toList();
    }

    @Test
    @DisplayName("Registrar negocio, deuda, pago y anular el pago deja cuatro registros, en orden")
    void cadaCambioQuedaRegistrado() {
        long id = e.negocio("Uno", "L-1", "1090123456").getId();
        long deudaId = e.deuda(id, "ADMINISTRACION", 10, 100000).getId();
        long pagoId = e.pagoService.registrar(new RegistrarPagoCommand(id, HOY, BigDecimal.valueOf(40000)))
                .pago().getId();
        e.pagoService.anular(pagoId);

        assertEquals(List.of(AccionAuditada.NEGOCIO_REGISTRADO, AccionAuditada.DEUDA_REGISTRADA,
                AccionAuditada.PAGO_REGISTRADO, AccionAuditada.PAGO_ANULADO), acciones(id));

        RegistroAuditoria anulacion = e.auditoriaService.deNegocio(id).get(3);
        assertEquals("admin.prueba", anulacion.getUsuario());
        assertEquals(AHORA, anulacion.getFechaHora());
        assertEquals(pagoId, anulacion.getEntidadId());
        assertEquals(deudaId, e.auditoriaService.deNegocio(id).get(1).getEntidadId());
    }

    @Test
    @DisplayName("Editar un negocio, corregir una deuda y dar de baja también quedan registrados")
    void edicionCorreccionYBaja() {
        long id = e.negocio("Dos", "L-2", "1090123457").getId();
        e.negocioService.editar(new EditarNegocioCommand(id, "Dos y Más", "L-2", "1090123457", "Dueño 1090123457", null));
        long deudaId = e.deuda(id, "ALQUILER", 9, 50000).getId();
        e.deudaService.actualizar(new ActualizarDeudaCommand(deudaId, BigDecimal.valueOf(60000)));
        e.pagoService.registrar(new RegistrarPagoCommand(id, HOY, BigDecimal.valueOf(60000)));
        e.negocioService.darDeBaja(id);

        List<AccionAuditada> acciones = acciones(id);
        assertTrue(acciones.contains(AccionAuditada.NEGOCIO_EDITADO));
        assertTrue(acciones.contains(AccionAuditada.DEUDA_ACTUALIZADA));
        assertEquals(AccionAuditada.NEGOCIO_DADO_DE_BAJA, acciones.get(acciones.size() - 1));
        RegistroAuditoria correccion = e.auditoriaService.deNegocio(id).stream()
                .filter(r -> r.getAccion() == AccionAuditada.DEUDA_ACTUALIZADA).findFirst().orElseThrow();
        assertTrue(correccion.getDetalle().contains("50000") && correccion.getDetalle().contains("60000"),
                correccion.getDetalle());
    }

    @Test
    @DisplayName("Una operación rechazada no deja auditoría")
    void operacionRechazadaNoSeAudita() {
        long id = e.negocio("Tres", "L-3", "1090123458").getId();
        e.deuda(id, "ADMINISTRACION", 10, 100000);
        int antes = e.auditoria.todos().size();

        assertThrows(PagoExcedeSaldoException.class,
                () -> e.pagoService.registrar(new RegistrarPagoCommand(id, HOY, BigDecimal.valueOf(100001))));

        assertEquals(antes, e.auditoria.todos().size());
    }

    @Test
    @DisplayName("Consultar la auditoría de un negocio inexistente es 404")
    void negocioInexistente() {
        assertThrows(NegocioNoEncontradoException.class, () -> e.auditoriaService.deNegocio(999));
    }
}
