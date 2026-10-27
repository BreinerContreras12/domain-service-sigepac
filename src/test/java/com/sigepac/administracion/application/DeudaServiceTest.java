package com.sigepac.administracion.application;

import static org.junit.jupiter.api.Assertions.*;

import com.sigepac.administracion.application.port.in.command.ActualizarDeudaCommand;
import com.sigepac.administracion.application.port.in.command.RegistrarDeudaCommand;
import com.sigepac.administracion.application.port.in.command.RegistrarPagoCommand;
import com.sigepac.administracion.domain.exception.*;
import com.sigepac.administracion.domain.model.Deuda;
import com.sigepac.administracion.domain.model.Negocio;
import com.sigepac.administracion.domain.vo.Dinero;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DeudaServiceTest {

    private final Escenario e = new Escenario();

    @Test
    void registraDeudaParaUnNegocioActivo() {
        Negocio n = e.negocio("Uno", "L-1", "1090123456");
        Deuda d = e.deuda(n.getId().longValue(), "alquiler", 10, 500000);
        assertNotNull(d.getId());
        assertEquals(Dinero.de(500000), d.saldo());
    }

    @Test
    @DisplayName("No se repite concepto + período para el mismo negocio")
    void noDuplicaConceptoPeriodo() {
        Negocio n = e.negocio("Uno", "L-1", "1090123456");
        e.deuda(n.getId().longValue(), "ALQUILER", 10, 500000);
        assertThrows(DeudaDuplicadaException.class, () -> e.deuda(n.getId().longValue(), "ALQUILER", 10, 1));
        e.deuda(n.getId().longValue(), "ADMINISTRACION", 10, 1);   // otro concepto, sí
        e.deuda(n.getId().longValue(), "ALQUILER", 11, 1);          // otro período, sí
    }

    @Test
    void negocioInexistenteOInactivo() {
        assertThrows(NegocioNoEncontradoException.class, () -> e.deuda(99, "ALQUILER", 10, 1));
        Negocio n = e.negocio("Uno", "L-1", "1090123456");
        e.negocioService.darDeBaja(n.getId().longValue());
        assertThrows(NegocioInactivoException.class, () -> e.deuda(n.getId().longValue(), "ALQUILER", 10, 1));
    }

    @Test
    void validaValorYConcepto() {
        Negocio n = e.negocio("Uno", "L-1", "1090123456");
        assertThrows(ValorInvalidoException.class, () -> e.deuda(n.getId().longValue(), "ALQUILER", 10, 0));
        assertThrows(ValorInvalidoException.class, () -> e.deuda(n.getId().longValue(), "MULTA", 10, 5));
        assertThrows(ValorInvalidoException.class, () -> e.deudaService.registrar(
                new RegistrarDeudaCommand(n.getId().longValue(), "ALQUILER", 2026, 13, BigDecimal.ONE)));
    }

    @Test
    void actualizaValorPeroNoPorDebajoDeLoPagado() {
        Negocio n = e.negocio("Uno", "L-1", "1090123456");
        Deuda d = e.deuda(n.getId().longValue(), "ALQUILER", 10, 100000);
        e.pagoService.registrar(new RegistrarPagoCommand(n.getId().longValue(), java.time.LocalDate.of(2026, 10, 20), BigDecimal.valueOf(60000)));
        Deuda act = e.deudaService.actualizar(new ActualizarDeudaCommand(d.getId().longValue(), BigDecimal.valueOf(150000)));
        assertEquals(Dinero.de(90000), act.saldo());
        assertThrows(ValorDeudaMenorQuePagadoException.class,
                () -> e.deudaService.actualizar(new ActualizarDeudaCommand(d.getId().longValue(), BigDecimal.valueOf(59999))));
        assertThrows(DeudaNoEncontradaException.class,
                () -> e.deudaService.actualizar(new ActualizarDeudaCommand(999, BigDecimal.ONE)));
    }
}
