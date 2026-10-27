package com.sigepac.administracion.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sigepac.administracion.domain.exception.PagoYaAnuladoException;
import com.sigepac.administracion.domain.exception.ValorInvalidoException;
import com.sigepac.administracion.domain.model.AplicacionPago;
import com.sigepac.administracion.domain.model.EstadoPago;
import com.sigepac.administracion.domain.model.Pago;
import com.sigepac.administracion.domain.vo.Dinero;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PagoTest {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 20);

    @Test
    void pagoNuevoEstaVigente() {
        Pago p = Pago.nuevo(1, HOY, Dinero.de(50000), HOY);
        assertEquals(EstadoPago.VIGENTE, p.getEstado());
        assertTrue(p.estaVigente());
    }

    @Test
    @DisplayName("La fecha no puede ser futura y el monto debe ser mayor que cero")
    void validaFechaYMonto() {
        assertThrows(ValorInvalidoException.class, () -> Pago.nuevo(1, HOY.plusDays(1), Dinero.de(1), HOY));
        assertThrows(ValorInvalidoException.class, () -> Pago.nuevo(1, null, Dinero.de(1), HOY));
        assertThrows(ValorInvalidoException.class, () -> Pago.nuevo(1, HOY, Dinero.CERO, HOY));
        assertThrows(ValorInvalidoException.class, () -> Pago.nuevo(1, HOY, null, HOY));
        Pago.nuevo(1, HOY, Dinero.de(1), HOY);   // hoy sí vale
    }

    @Test
    void lasAplicacionesDebenSumarElMontoYAsignarseUnaSolaVez() {
        Pago p = Pago.nuevo(1, HOY, Dinero.de(100000), HOY);
        assertThrows(ValorInvalidoException.class, () -> p.asignarAplicaciones(List.of(new AplicacionPago(1, Dinero.de(90000)))));
        p.asignarAplicaciones(List.of(new AplicacionPago(1, Dinero.de(60000)), new AplicacionPago(2, Dinero.de(40000))));
        assertEquals(2, p.getAplicaciones().size());
        assertThrows(ValorInvalidoException.class, () -> p.asignarAplicaciones(List.of(new AplicacionPago(3, Dinero.de(100000)))));
    }

    @Test
    void anularUnaSolaVez() {
        Pago p = Pago.reconstruir(7, 1, HOY, Dinero.de(10), EstadoPago.VIGENTE, List.of(new AplicacionPago(1, Dinero.de(10))));
        p.anular();
        assertEquals(EstadoPago.ANULADO, p.getEstado());
        assertThrows(PagoYaAnuladoException.class, p::anular);
    }
}
