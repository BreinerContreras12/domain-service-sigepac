package com.sigepac.administracion.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sigepac.administracion.domain.exception.ValorDeudaMenorQuePagadoException;
import com.sigepac.administracion.domain.exception.ValorInvalidoException;
import com.sigepac.administracion.domain.model.Deuda;
import com.sigepac.administracion.domain.model.EstadoDeuda;
import com.sigepac.administracion.domain.vo.ConceptoDeuda;
import com.sigepac.administracion.domain.vo.Dinero;
import com.sigepac.administracion.domain.vo.Periodo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DeudaTest {

    private Deuda nueva(long valor) {
        return Deuda.registrar(1, ConceptoDeuda.ADMINISTRACION, Periodo.de(2026, 10), Dinero.de(valor));
    }

    @Test
    @DisplayName("Una deuda nueva está PENDIENTE y su saldo es todo su valor")
    void deudaNueva() {
        Deuda d = nueva(200000);
        assertEquals(EstadoDeuda.PENDIENTE, d.estado());
        assertEquals(Dinero.de(200000), d.saldo());
        assertEquals(Dinero.CERO, d.getPagado());
    }

    @Test
    void elValorDebeSerMayorQueCero() {
        assertThrows(ValorInvalidoException.class, () -> nueva(0));
        assertThrows(ValorInvalidoException.class, () -> Deuda.registrar(1, ConceptoDeuda.ALQUILER, Periodo.de(2026, 1), null));
    }

    @Test
    void aplicarPagoReduceElSaldoYAlLlegarACeroQuedaPagada() {
        Deuda d = nueva(200000);
        d.aplicarPago(Dinero.de(50000));
        assertEquals(Dinero.de(150000), d.saldo());
        assertEquals(EstadoDeuda.PENDIENTE, d.estado());
        d.aplicarPago(Dinero.de(150000));
        assertEquals(EstadoDeuda.PAGADA, d.estado());
    }

    @Test
    void noSePuedeAplicarMasQueElSaldo() {
        Deuda d = nueva(100000);
        assertThrows(ValorInvalidoException.class, () -> d.aplicarPago(Dinero.de(100001)));
        assertEquals(Dinero.de(100000), d.saldo());
    }

    @Test
    @DisplayName("Actualizar el valor recalcula el saldo, pero nunca por debajo de lo ya pagado")
    void actualizarValor() {
        Deuda d = nueva(200000);
        d.aplicarPago(Dinero.de(80000));
        d.actualizarValor(Dinero.de(250000));
        assertEquals(Dinero.de(170000), d.saldo());
        assertThrows(ValorDeudaMenorQuePagadoException.class, () -> d.actualizarValor(Dinero.de(79999)));
        d.actualizarValor(Dinero.de(80000));
        assertEquals(EstadoDeuda.PAGADA, d.estado());
    }

    @Test
    void revertirPagoDevuelveElSaldo() {
        Deuda d = nueva(100000);
        d.aplicarPago(Dinero.de(60000));
        d.revertirPago(Dinero.de(60000));
        assertEquals(Dinero.de(100000), d.saldo());
        assertThrows(ValorInvalidoException.class, () -> d.revertirPago(Dinero.de(1)));
    }
}
