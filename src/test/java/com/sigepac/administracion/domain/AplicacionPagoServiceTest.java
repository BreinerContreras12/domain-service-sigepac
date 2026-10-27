package com.sigepac.administracion.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sigepac.administracion.domain.exception.PagoExcedeSaldoException;
import com.sigepac.administracion.domain.exception.ValorInvalidoException;
import com.sigepac.administracion.domain.model.AplicacionPago;
import com.sigepac.administracion.domain.model.Deuda;
import com.sigepac.administracion.domain.service.AplicacionPagoService;
import com.sigepac.administracion.domain.vo.ConceptoDeuda;
import com.sigepac.administracion.domain.vo.Dinero;
import com.sigepac.administracion.domain.vo.Periodo;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AplicacionPagoServiceTest {

    private final AplicacionPagoService servicio = new AplicacionPagoService();

    private Deuda deuda(long id, int mes, long valor) {
        return Deuda.reconstruir(id, 1, ConceptoDeuda.ADMINISTRACION, Periodo.de(2026, mes), Dinero.de(valor), Dinero.CERO);
    }

    @Test
    @DisplayName("El pago cubre primero la deuda más antigua, aunque venga desordenada")
    void pagaLaMasAntiguaPrimero() {
        Deuda octubre = deuda(3, 10, 100000);
        Deuda agosto = deuda(1, 8, 100000);
        Deuda septiembre = deuda(2, 9, 100000);

        List<AplicacionPago> reparto = servicio.aplicar(Dinero.de(150000), List.of(octubre, agosto, septiembre));

        assertEquals(2, reparto.size());
        assertEquals(1, reparto.get(0).deudaId());
        assertEquals(Dinero.de(100000), reparto.get(0).monto());
        assertEquals(2, reparto.get(1).deudaId());
        assertEquals(Dinero.de(50000), reparto.get(1).monto());
        assertEquals(Dinero.CERO, agosto.saldo());
        assertEquals(Dinero.de(50000), septiembre.saldo());
        assertEquals(Dinero.de(100000), octubre.saldo());
    }

    @Test
    void ignoraLasDeudasYaPagadas() {
        Deuda pagada = deuda(1, 8, 100000);
        pagada.aplicarPago(Dinero.de(100000));
        Deuda pendiente = deuda(2, 9, 100000);

        List<AplicacionPago> reparto = servicio.aplicar(Dinero.de(30000), List.of(pagada, pendiente));

        assertEquals(1, reparto.size());
        assertEquals(2, reparto.get(0).deudaId());
    }

    @Test
    @DisplayName("El pago exacto deja el saldo en cero")
    void pagoExacto() {
        Deuda a = deuda(1, 9, 70000);
        Deuda b = deuda(2, 10, 30000);
        servicio.aplicar(Dinero.de(100000), List.of(a, b));
        assertEquals(Dinero.CERO, a.saldo());
        assertEquals(Dinero.CERO, b.saldo());
    }

    @Test
    @DisplayName("Un pago mayor que el saldo falla y NO modifica ninguna deuda")
    void excedeElSaldo() {
        Deuda a = deuda(1, 9, 70000);
        Deuda b = deuda(2, 10, 30000);
        PagoExcedeSaldoException e = assertThrows(PagoExcedeSaldoException.class,
                () -> servicio.aplicar(Dinero.de(100001), List.of(a, b)));
        assertEquals(Dinero.de(100000), e.getSaldo());
        assertEquals(Dinero.de(70000), a.saldo());
        assertEquals(Dinero.de(30000), b.saldo());
    }

    @Test
    void sinDeudasNoSePuedePagar() {
        assertThrows(PagoExcedeSaldoException.class, () -> servicio.aplicar(Dinero.de(1), List.of()));
        assertThrows(ValorInvalidoException.class, () -> servicio.aplicar(Dinero.CERO, List.of(deuda(1, 1, 10))));
        assertTrue(true);
    }
}
