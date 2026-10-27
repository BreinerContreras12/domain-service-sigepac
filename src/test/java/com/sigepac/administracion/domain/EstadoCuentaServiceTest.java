package com.sigepac.administracion.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sigepac.administracion.domain.model.AplicacionPago;
import com.sigepac.administracion.domain.model.Deuda;
import com.sigepac.administracion.domain.model.EstadoPago;
import com.sigepac.administracion.domain.model.Pago;
import com.sigepac.administracion.domain.service.AplicacionPagoService;
import com.sigepac.administracion.domain.service.EstadoCuenta;
import com.sigepac.administracion.domain.service.EstadoCuentaService;
import com.sigepac.administracion.domain.vo.ConceptoDeuda;
import com.sigepac.administracion.domain.vo.Dinero;
import com.sigepac.administracion.domain.vo.Periodo;
import com.sigepac.administracion.testsupport.Datos;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EstadoCuentaServiceTest {

    private final EstadoCuentaService servicio = new EstadoCuentaService();

    private Deuda deuda(long id, long negocio, int mes, long valor) {
        return Deuda.reconstruir(id, negocio, ConceptoDeuda.ALQUILER, Periodo.de(2026, mes), Dinero.de(valor), Dinero.CERO);
    }

    @Test
    @DisplayName("El saldo siempre es: total de deudas menos total de pagos vigentes")
    void saldoCuadraConDeudasMenosPagos() {
        Deuda d1 = deuda(1, 1, 9, 100000);
        Deuda d2 = deuda(2, 1, 10, 100000);
        var reparto = new AplicacionPagoService().aplicar(Dinero.de(130000), List.of(d1, d2));
        Pago pago = Pago.reconstruir(1, 1, LocalDate.of(2026, 10, 5), Dinero.de(130000), EstadoPago.VIGENTE, reparto);

        EstadoCuenta ec = servicio.calcular(Datos.panaderia("L-1"), List.of(d2, d1), List.of(pago));

        assertEquals(Dinero.de(200000), ec.totalDeudas());
        assertEquals(Dinero.de(130000), ec.totalPagado());
        assertEquals(Dinero.de(70000), ec.saldo());
        assertEquals(ec.totalDeudas().valor().subtract(ec.totalPagado().valor()), ec.saldo().valor());
        assertEquals(1L, ec.deudas().get(0).getId().longValue());   // ordenadas por período
    }

    @Test
    void losPagosAnuladosNoCuentan() {
        Deuda d = deuda(1, 1, 9, 100000);
        Pago anulado = Pago.reconstruir(1, 1, LocalDate.of(2026, 10, 5), Dinero.de(40000), EstadoPago.ANULADO,
                List.of(new AplicacionPago(1, Dinero.de(40000))));
        EstadoCuenta ec = servicio.calcular(Datos.panaderia("L-1"), List.of(d), List.of(anulado));
        assertEquals(Dinero.CERO, ec.totalPagado());
        assertEquals(Dinero.de(100000), ec.saldo());
    }

    @Test
    void saldosPorNegocio() {
        Map<Long, Dinero> saldos = servicio.saldosPorNegocio(List.of(deuda(1, 1, 9, 10), deuda(2, 1, 10, 20), deuda(3, 2, 10, 5)));
        assertEquals(Dinero.de(30), saldos.get(1L));
        assertEquals(Dinero.de(5), saldos.get(2L));
    }
}
